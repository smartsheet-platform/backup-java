/**
   Copyright 2013 Smartsheet.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

**/
package com.smartsheet.tools;

import static com.smartsheet.utils.FileUtils.deleteFolder;
import static com.smartsheet.utils.FileUtils.folderNameExistsInParentFolder;
import static com.smartsheet.utils.FileUtils.stripExtension;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.smartsheet.exceptions.CreateFileSystemItemException;
import com.smartsheet.exceptions.FileSystemItemException;
import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetDiscussion;
import com.smartsheet.restapi.model.SmartsheetFolder;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetRow;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;
import com.smartsheet.restapi.model.SmartsheetWorkspace;
import com.smartsheet.restapi.service.SmartsheetService;
import com.smartsheet.utils.ErrorHandler;
import com.smartsheet.utils.ProgressWatcher;

/**
 * Backs up the Smartsheet sheets of either the current user or all users to a
 * local directory, maintaining the folder / workspace hierarchy from Smartsheet.
 */
public class SmartsheetBackupService {

    // Smartsheet API constants
    private static final String USER_ACTIVE_STATUS = "ACTIVE";
    private static final String OWNER_ACCESS = "OWNER";
    private static final String FILE_ATTACHMENT_TYPE = "FILE";

    private final SmartsheetService apiService;
    private final SheetSaver sheetSaver;

    public SmartsheetBackupService(
            SmartsheetService apiService, ParallelDownloadService parallelDownloadService) {
        this.apiService = apiService;
        this.sheetSaver = new SheetSaver(apiService, parallelDownloadService);
    }

    /**
     * Backs up the sheets of all users in the organization to a local directory.
     * Requires an access token from an account administrator. Only active users
     * are backed up, not pending or other inactive users (such users cannot be
     * accessed through an access token, even an admin access token).
     *
     * @param backupFolder
     *          The folder to backup to. Created if it doesn't exist. Warning:
     *          contents are overwritten. A sub folder will be created under
     *          this folder for each user. The sub folder will be named with the
     *          user's email address. Note that email addresses are valid as
     *          folder names on all mainstream operating systems.
     *
     * @return
     *          The number of users whose sheets were backed up (hence pending
     *          and other inactive users are excluded from the number returned).
     *
     * @throws Exception
     */
    public int backupOrgTo(File backupFolder) throws Exception {
        // get all users in the organization and prepare the backup folder
        List<SmartsheetUser> users = apiService.getUsers();
        prepareBackupFolder(backupFolder, true);

        // iterate through the users, backing up the active ones
        int numberUsers = users.size();
        int skippedUsers = 0;
        try {
            for (int i = 0; i < numberUsers; i++) {
                SmartsheetUser user = users.get(i);
                String email = user.getEmail();
                String status = user.getStatus();

                // for each active user, assume the identity of the user to
                // backup that user's sheets in the user's context (e.g., what
                // sheets they own, the hierarchy they see in Smartsheet, etc.)
                if (status.equals(USER_ACTIVE_STATUS)) {

                    ProgressWatcher.getInstance().notify(String.format(
                        "--------------------Start backup for user [%d of %d]: %s--------------------",
                        i+1, numberUsers, email));
                    try {
                        assumeUserAndBackup(backupFolder, email);

                    } catch (Exception e) {
                        ErrorHandler.handle(e, email);
                        skippedUsers++;
                    }

                } else {
                    // user not active yet and will result in 401 (Unauthorized)
                    // if try to assume their identity, so skip...
                    ProgressWatcher.getInstance().notify(String.format(
                        "--------------------SKIP backup for user [%d of %d]: %s (%s)--------------------",
                        i+1, numberUsers, email, status.toLowerCase()));
                    skippedUsers++;
                }
            }

        } finally {
            apiService.assumeUser(null); // revert to self before returning
        }

        // return the number of users backed up, excluding skipped inactive users
        return numberUsers - skippedUsers;
    }

    /**
     * Assume the identity of a specified user and backup the user's sheets in
     * the user's context. A sub folder will be created under the specified
     * folder and named with the user's email address. Note that email
     * addresses are valid as folder names on all mainstream operating systems.
     * Hence no replacement of characters in the email address is required for
     * use as a folder name.
     */
    private void assumeUserAndBackup(File backupFolder, String userEmail) throws Exception {
        File userFolder = createNewFolderQuietly(backupFolder, userEmail);
        apiService.assumeUser(userEmail);
        backupTo(userFolder);
    }

    /**
     * Backs up the sheets of the current user to a local directory.
     *
     * @param backupFolder
     *          The folder to backup to. Created if it doesn't exist.
     *          Warning: contents are overwritten.
     *
     * @throws Exception
     */
    public void backupTo(File backupFolder) throws Exception {
        SmartsheetHome home = apiService.getHome();
        List<SmartsheetSheet> sheets = home.getSheets();
        List<SmartsheetFolder> folders = home.getFolders();
        List<SmartsheetWorkspace> workspaces = home.getWorkspaces();

        prepareBackupFolder(backupFolder, false);

        // first create the two "root" folders of the Smartsheet hierarchy to mimic the Home UI
        File sheetsRoot = createNewFolder(backupFolder, "Sheets");
        File workspacesRoot = createNewFolder(backupFolder, "Workspaces");

        // and save the top-level sheets
        for (SmartsheetSheet sheet : sheets) {
            saveSheetToFolder(sheet, sheetsRoot);
        }

        // then create and save the rest of the hierarchy with contained sheets and attachments
        createFoldersRecursively(sheetsRoot, folders);
        createFoldersRecursively(workspacesRoot, workspaces);
    }

    /**
     * Prepares the backup folder by creating it if it doesn't exist, and
     * clearing its contents if it does.
     *
     * @param backupFolder
     * @throws FileSystemItemException
     */
    private static void prepareBackupFolder(File backupFolder, boolean isRootFolder) throws FileSystemItemException {
        if (backupFolder.exists() && !backupFolder.isDirectory())
            throw new IllegalArgumentException(backupFolder.getAbsolutePath() + " is not a directory");

        if (backupFolder.exists()) {
        	if (isRootFolder) {
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh_mm_ss");
            	File renamedFolder = new File(backupFolder.getAbsolutePath() + "-" + sdf.format(new Date(backupFolder.lastModified())));
            	ProgressWatcher.getInstance().notify(String.format("Renaming previous output from [%s] to [%s]", backupFolder.getAbsolutePath(), renamedFolder.getAbsolutePath()));
            	backupFolder.renameTo(renamedFolder);
        	} else {
        		deleteFolder(backupFolder);

        	}
        }

        if (!backupFolder.mkdirs())
            throw new CreateFileSystemItemException(backupFolder);
    }

    private void saveSheetToFolder(SmartsheetSheet sheet, File folder) throws Exception {
        // only sheets owned by the current user are backed up
        if (!sheet.getAccessLevel().equals(OWNER_ACCESS))
            return;

        File sheetFile = sheetSaver.save(sheet, folder);
        ProgressWatcher.getInstance().notify(String.format("Sheet [%s] saved as [%s]", sheet.getName(), sheetFile.getAbsolutePath()));

        // get sheet details and...
        sheet = this.apiService.getSheetDetails(sheet.getName(), sheet.getId());
        // 1. collect sheet attachments
        List<SmartsheetAttachment> attachments = new ArrayList<SmartsheetAttachment>(sheet.getAttachments());
        // 2. collect sheet discussion attachments
        for (SmartsheetDiscussion discussion : sheet.getDiscussions()) {
            attachments.addAll(discussion.getCommentAttachments());
        }

        // iterate rows and...
        for (SmartsheetRow row : sheet.getRows()) {
            // 1. collect row attachments
            attachments.addAll(row.getAttachments());
            // 2. collect row discussion attachments
            for (SmartsheetDiscussion discussion : row.getDiscussions()) {
                attachments.addAll(discussion.getCommentAttachments());
            }
        }

        // create a new folder for attachments, if any
        if (!attachments.isEmpty())
            folder = createNewFolder(folder, stripExtension(sheetFile.getName()) + " - attachments");

        // save each attachment appropriately, either as a file or as a summary for non-files
        for (SmartsheetAttachment attachment : attachments) {
            String attachmentType = attachment.getAttachmentType();
            if (attachmentType.equals(FILE_ATTACHMENT_TYPE)) {
                sheetSaver.saveAsynchronously(attachment, folder, sheet.getName(), apiService.getAssumedUser());
            } else {
                File summariesFile = sheetSaver.saveSummary(attachment, sheet, folder);
                ProgressWatcher.getInstance().notify(String.format("%s Attachment [%s] recorded in [%s]", attachmentType, attachment.getName(), summariesFile.getAbsolutePath()));
            }
        }
    }

    // The following are helper methods for creating local folders, with and
    // without notification (logging).

    private static File createNewFolder(File parentFolder, String newFolderName)
            throws FileSystemItemException {
        return createNewFolder(parentFolder, newFolderName, null);
    }

    private static File createNewFolder(File parentFolder, String newFolderName, String origFolderName)
            throws FileSystemItemException {
        File newFolder = createNewFolderQuietly(parentFolder, newFolderName);

        if (origFolderName == null)
            origFolderName = newFolderName;

        ProgressWatcher.getInstance().notify(String.format("Folder [%s] created as [%s]", origFolderName, newFolder.getAbsolutePath()));
        return newFolder;
    }

    private static File createNewFolderQuietly(File parentFolder, String newFolderName) throws CreateFileSystemItemException {
        File newFolder = new File(parentFolder, SheetSaver.scrubName(newFolderName));

        if (!newFolder.mkdir())
            throw new CreateFileSystemItemException(newFolder);
        return newFolder;
    }

    // The following are helper methods for creating a Smartsheet folder or
    // workspace hierarchy under a local folder using recursion. Since folders
    // in Smartsheet can have duplicate names even at the same level, duplicates
    // in a local folder where this is not permitted are resolved using a number
    // suffix starting from 2 (as in "folder name (2)", "folder name (3)", etc.)

    private void createFoldersRecursively(File parentFolder, List<? extends SmartsheetFolder> folders)
            throws Exception {
        for (SmartsheetFolder folder : folders) {
            // create folder
            String folderName = findNonDupeFolderName(parentFolder, folder.getName());
            File newFolder = createNewFolder(parentFolder, folderName, folder.getName());

            // save sheets in folder
            List<SmartsheetSheet> sheets = folder.getSheets();
            for (SmartsheetSheet sheet : sheets) {
                saveSheetToFolder(sheet, newFolder);
            }

            // create subfolders and save their sheets
            createFoldersRecursively(newFolder, folder.getFolders());
        }
    }

    private static String findNonDupeFolderName(File parentFolder, String baseFolderName) {
        String candidateFolderName = baseFolderName;
        int numberSuffix = 2;
        while (true) { // keep looping until get a non duplicate folder name
            if (!folderNameExistsInParentFolder(candidateFolderName, parentFolder))
                return candidateFolderName; // this name is not used in the parent folder

            // otherwise try another name by appending an incrementing number suffix
            candidateFolderName = baseFolderName + " (" + numberSuffix + ")";
            numberSuffix++;
        }
    }
}

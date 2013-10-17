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

import static com.smartsheet.utils.FileUtils.fileNameExistsInFolder;
import static com.smartsheet.utils.HttpUtils.saveUrlToFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.smartsheet.exceptions.CreateFileSystemItemException;
import com.smartsheet.exceptions.SmartsheetGetSheetDetailsException;
import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetNamedEntity;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.service.SmartsheetService;
import com.smartsheet.utils.ProgressWatcher;

/**
 * Saves a Smartsheet sheet/attachment (file/non-file) to a local directory.
 */
public class SheetSaver {

    private static final String XLS_EXTENSION = ".xls";

    private final SmartsheetService apiService;
    private final ParallelDownloadService parallelDownloadService;

    public SheetSaver(
            SmartsheetService apiService, ParallelDownloadService parallelDownloadService) {
        this.apiService = apiService;
        this.parallelDownloadService = parallelDownloadService;
    }

    /**
     * Saves a sheet to a local folder.
     *
     * @param sheet the sheet to save
     * @param folder the existing local folder to save the sheet to
     * @return the {@link File} where the sheet was saved to
     * @throws Exception
     */
    public File save(SmartsheetSheet sheet, File folder) throws Exception {
        File sheetFile = createFileFor(sheet, folder, XLS_EXTENSION);
        String url = "https://api.smartsheet.com/1.1/sheet/" + sheet.getId();
        String accessToken = apiService.getAccessToken();
        String userToAssume = apiService.getAssumedUser();
        try {
            saveUrlToFile(url, sheetFile, accessToken, "application/vnd.ms-excel", userToAssume);
            return sheetFile;

        } catch (Exception e) {
            throw new SmartsheetGetSheetDetailsException(e, sheet.getName(), sheet.getId());
        }
    }

    /**
     * Saves a file attachment to a local folder asynchronously. The method
     * hence returns immediately.
     *
     * @param attachment the file attachment to save
     * @param folder the existing local folder to save the file attachment to
     * @param sheetName the name of the sheet the attachment belongs to
     * @throws Exception
     */
    public void saveAsynchronously(SmartsheetAttachment attachment, File folder, String sheetName) throws Exception {
        File attachmentFile = createFileFor(attachment, folder, null);
        String filePath = attachmentFile.getAbsolutePath();

        String attachmentType = attachment.getAttachmentType();
        String attachmentName = attachment.getName();

        String postedMessage = String.format(">> Download request for %s Attachment [%s]", attachmentType, attachmentName);
        String completedMessage = String.format("...%s Attachment [%s] downloaded as [%s]", attachmentType, attachmentName, filePath);
        String errorContext = String.format("%s Attachment [%s] in Sheet [%s]", attachmentType, attachmentName, sheetName);

        parallelDownloadService.postAsynchronousDownloadJob(
            new SmartsheetAttachmentContentSource(apiService, attachment, sheetName),
            attachmentFile,
            postedMessage, completedMessage, errorContext);
    }

    /**
     * Creates a file in a specified folder to hold the contents of a
     * {@link SmartsheetNamedEntity} item (sheet or attachment). The file will
     * be created with the same name as the item unless a file of that name
     * already exists in the folder. In that case a number will be appended to
     * the file name starting from 2 (such as "sheet name (2)") and upward
     * until a unique file name is found in the folder. An optional file
     * extension can also be specified (for example when you want to use ".xls"
     * as the extension of the file which a sheet is saved to.
     */
    private File createFileFor(SmartsheetNamedEntity item, File folder, String extension) throws IOException {
        int numberSuffix = 2;
        String fileName = getUniqueFileNameForItemInFolder(item, folder, numberSuffix, extension);

        File newFile = new File(folder, fileName);
        ProgressWatcher.getInstance().notify(String.format("Creating new file: [%s]", newFile.getCanonicalPath()));
        if (!newFile.createNewFile())
            throw new CreateFileSystemItemException(newFile);
        return newFile;
    }

    /**
     * Gets a unique file name for an item in a folder, recursively trying a
     * number suffix until a unique file name is found.
     */
    private static String getUniqueFileNameForItemInFolder(
            SmartsheetNamedEntity item, File folder, int numberSuffix, String extension) {

        String itemName = item.getName();

        String fileNamePart;
        String extensionPart = extension;
        if (extensionPart != null) // extension supplied, so fileNamePart is just the itemName
            fileNamePart = itemName;
        else { // extension not supplied, so parse from itemName
            int lastDotIndex = itemName.lastIndexOf(".");
            if (lastDotIndex != -1) {
                fileNamePart = itemName.substring(0, lastDotIndex);
                extensionPart = itemName.substring(lastDotIndex);
            } else {
                fileNamePart = itemName;
                extensionPart = "";
            }
        }
        fileNamePart = scrubName(fileNamePart);
        String fullFileName = fileNamePart + extensionPart;

        if(fullFileName.length()==0)
            throw new IllegalStateException(
                    "File Name " + fullFileName + " results in a empty fileName!");

        if (!fileNameExistsInFolder(fullFileName, folder))
            return fullFileName;

        fullFileName = fileNamePart + " (" + numberSuffix + ")" + extensionPart;
        if (!fileNameExistsInFolder(fullFileName, folder))
            return fullFileName;

        // recursive call with incremented number suffix
        return getUniqueFileNameForItemInFolder(item, folder, numberSuffix + 1, extension);
    }

    public static String scrubName(String fileName) {

		return fileName.replaceAll("[\\\\/:\\*?\"<>|]+", "_");
	}

	/**
     * Saves a summary of a non-file attachment to a well-known named CSV file in a local folder.
     *
     * @param attachment the non-file attachment
     * @param sheet the sheet which the non-file attachment belongs to
     * @param folder the existing local folder to save the summary of the non-file attachment to
     * @return the CSV {@link File} where the summary was saved to
     * @throws IOException
     */
    public File saveSummary(SmartsheetAttachment attachment, SmartsheetSheet sheet, File folder) throws IOException {
        File summariesFile = new File(folder, scrubName(sheet.getName()) + " - non-file attachments.csv");
        boolean writeHeader = !summariesFile.exists();
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(summariesFile, true /*append*/)));
        try {
            if (writeHeader)
                writer.println("Name,URL,AttachmentType");

            writer.println(
                escapeCommas(attachment.getName()) + "," +
                attachment.getUrl() + "," +
                attachment.getAttachmentType());

        } finally {
            writer.close();
        }
        return summariesFile;
    }

    /**
     * Escape commas in a string to be added as a "column" in a CSV file.
     */
    private static String escapeCommas(String string) {
        if (string.contains(","))
            return '"' + string + '"';

        return string;
    }
}

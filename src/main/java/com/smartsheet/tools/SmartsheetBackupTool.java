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
import static com.smartsheet.utils.FileUtils.zipDirectory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.smartsheet.restapi.service.RestfulSmartsheetService;
import com.smartsheet.restapi.service.RetryingSmartsheetService;
import com.smartsheet.restapi.service.SmartsheetService;
import com.smartsheet.utils.ProgressWatcher;
import com.smartsheet.utils.PropertyUtils;

/**
 * The main class of the SmartsheetBackupTool utility program.
 */
public class SmartsheetBackupTool {


    private static final int SUCCESS_EXIT_CODE = 0;

    private static final int FAILURE_EXIT_CODE = -1;

    /**
     * The entry point of the program which reads properties, instantiates
     * services, and backs up the sheets of all active users in an organization
     * to a local directory (by default) or to a zip file (optionally). Clear
     * status messages are logged to the console during the backup process, with
     * summary statistics logged on completion.
     *
     * @param args
     *      optional - if provided, specifies the folder containing the
     *      {@literal smartsheet-backup.properties} file, which otherwise is
     *      expected to be in the current directory
     */
    public static void main(final String[] args) {
    
    	int exitCode = SUCCESS_EXIT_CODE;

        try {
        	
        	ProgressWatcher.notify("*** Org backup process started ***");
            // 1. Load properties
            PropertyUtils.loadPropertiesFile(args);
            // Log the properties to console
            PropertyUtils.logConfiguredProperties();
            
            final String accessToken = PropertyUtils.getAccessToken();
            final String outputDir = PropertyUtils.getOutputDir();
            final int downloadThreads = PropertyUtils.getDownloadThreads();
            final int allDownloadsDoneTimeout = PropertyUtils.getAllDownloadsDoneTimeout();
            final boolean zipOutputDir = PropertyUtils.isZipOutputDir();
            
            
            // 2. instantiate services
            final SmartsheetService apiService = new RetryingSmartsheetService(
                // the RetryingSmartsheetService wraps the RestfulSmartsheetService:
                new RestfulSmartsheetService(accessToken));

            final ParallelDownloadService parallelDownloadService = new ParallelDownloadService(
                downloadThreads, allDownloadsDoneTimeout);

            final SmartsheetBackupService backupService = new SmartsheetBackupService(
                apiService, parallelDownloadService);
            final long startTime = System.currentTimeMillis();

            // 3. back up the organization to a local folder
            final int numberUsers = backupService.backupOrgTo(new File(outputDir));

            if (parallelDownloadService.waitTillAllDownloadJobsDone()) {

                // 4. if requested, zip up the backup folder which is then deleted
                if (zipOutputDir){
                    zipAndDeleteOutputDir(outputDir);
                }
                // 5. tell user how long the backup took and how many users were backed up
                final String timeSummary = computeTimeSummary(startTime);
                ProgressWatcher.notify("*** Org backup done -> [" + numberUsers + "] users total backed up in " + timeSummary + " ***");

            } else{
                exitCode = FAILURE_EXIT_CODE;
            }
            ProgressWatcher.notify("No Errors reported during the backup process");
            
            //Print the logfile name if any error reported
            
        } catch (final Exception e) {
        		ProgressWatcher.notifyError(e);
            	ProgressWatcher.notifyError("Few errors reported during the backup process, log file location : "+ProgressWatcher.getLogFile());
            	exitCode = FAILURE_EXIT_CODE;
        }

        System.exit(exitCode);
    }

    private static void zipAndDeleteOutputDir(final String outputDirPath) throws IOException {
        final String zipFilePath = outputDirPath + ".zip";
        final File outputDir = new File(outputDirPath);
        zipDirectory(outputDir, new File(zipFilePath));
        deleteFolder(outputDir);
        ProgressWatcher.notify("Zipped output dir to: " + new File(zipFilePath).getAbsolutePath());
    }

    private static String computeTimeSummary(final long startTime) {
        final long finishTime = System.currentTimeMillis();
        final long duration = finishTime - startTime;
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        final long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        final long secondsOverMinute = totalSeconds - TimeUnit.MINUTES.toSeconds(minutes);
        if (minutes == 0)
            return totalSeconds + " seconds";
        return minutes + " minutes " + secondsOverMinute + " seconds";
    }

}

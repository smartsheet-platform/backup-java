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
package com.smartsheet.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A Progress Watcher which receives status and error notifications, publishing
 * them to the console.
 */
public class ProgressWatcher {

    private static final String SMARTSHEET_BACKUP_ERROR_LOG_PREFIX = "smartsheet-backup-error-log_";
    private static final String SMARTSHEET_BACKUP_ERROR_LOG_EXTENSION = ".log";
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private static final ProgressWatcher singleton = new ProgressWatcher();

    public static ProgressWatcher getInstance() {
        return singleton;
    }

    private int errorCount = 0;
    private boolean logErrorsToFile = false;
    private String errorLogFilePath; // initialized whenever logErrorsToFile is set to true

    private ProgressWatcher() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
    }

    public void notify(String status) {
        System.out.println(status);
    }

    public synchronized void notifyError(String error, Exception ex) {
        notify("***ERROR*** " + error);
        errorCount++;
        if (logErrorsToFile) {
            try {
            	if(ex != null){
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
            		error += error+"\nTRACE:\n"+sw.toString();
            	}
            	
                logToFile(error);
            } catch (IOException e) {
                notify(String.format(
                    "***ERROR*** Failed to write to error log file [%s] due to %s - %s",
                    errorLogFilePath, e.getClass().getSimpleName(), e.getLocalizedMessage()));
            }
        }
    }

    public void notifyError(Throwable error) {
        Throwable cause = error.getCause();
        if (cause != null)
            error = cause;
        notifyError(String.format("%s - %s", error.getClass().getSimpleName(), error.getLocalizedMessage()),null);
        System.out.flush(); // flush before printing stack trace to avoid overlapping logs from multiple threads
        error.printStackTrace();
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setLogErrorsToFile(boolean logErrorsToFile) {
        this.logErrorsToFile = logErrorsToFile;

        // prepare a pseudo-unique error log file path in the current directory
        // (in case it's needed; file NOT created yet)
        String logFilePrefix = SMARTSHEET_BACKUP_ERROR_LOG_PREFIX;
        String logFileSuffix = getCurrentDateTimeString().replace(':', '-').replace(' ', '_');
        String logFileExtension = SMARTSHEET_BACKUP_ERROR_LOG_EXTENSION;
        errorLogFilePath = new File(logFilePrefix + logFileSuffix + logFileExtension).getAbsolutePath();

        errorCount = 0; // reinitialize since errorLogFilePath has been reset
    }

    public String getErrorLogFile() {
        if (errorCount > 0 && logErrorsToFile)
            return errorLogFilePath;

        return null;
    }

    private void logToFile(String error) throws IOException {
        String log = String.format(
            "[%s] *** ERROR %d *** %s", getCurrentDateTimeString(), errorCount, error);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(errorLogFilePath, true /*append*/)));
        try {
            writer.println(log);

        } finally {
            writer.close();
        }
    }

    private static String getCurrentDateTimeString() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }
}

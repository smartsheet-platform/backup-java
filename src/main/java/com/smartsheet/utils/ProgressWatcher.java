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

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Progress Watcher which receives status and error notifications, publishing
 * them to the console. This can be easily enhanced by adding your own custom handler to write to a file
 */
public class ProgressWatcher {

	//Using java.util.logging instead of sysouts 
	static Logger logger = Logger.getLogger(ProgressWatcher.class.getName());
	static Handler fileHandler;
	private static boolean errorOccurred;
	static{
		//Set the log level and handle to Console
        try {
        	logger.setUseParentHandlers(false);

        	final ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new LoggingFormatter());
    		logger.addHandler(handler);
    		fileHandler = new FileHandler(getLogFile());
        	fileHandler.setFormatter(new LoggingFormatter());
        	logger.addHandler(fileHandler);
        	    	
        	logger.setLevel(Level.FINE);
        	
        	setErrorOccurred(false);
		
        }catch (final SecurityException e) {
			notifyWarning("Error while configuring the logger file"+e.getMessage());
		} catch (final IOException e) {
			notifyWarning("Error while configuring the logger file"+e.getMessage());
		}
	}
	
    private ProgressWatcher() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
    }

    /**
     * Logs the message as info
     * 
     * @param message
     */
    public static void notify(final String message) {
        //System.out.println(status);
    	logger.log(Level.INFO,message);
    }

    /**
     * Logs the message as warning 
     * 
     * @param warning
     */
    public static void notifyWarning(final String warning) {
        //notify("***WARNING*** " + warning);
    	logger.log(Level.WARNING,warning);
    }
    public static void notifyError(final String error) {
        //notify("***ERROR*** " + error);
    	logger.log(Level.SEVERE,error);
    	setErrorOccurred(true);
    }

    public static void notifyError(Throwable error) {
        final Throwable cause = error.getCause();
        if (cause != null){
            error = cause;
        }
        notifyError(String.format("%s - %s", error.getClass().getSimpleName(), error.getLocalizedMessage()));
        error.printStackTrace();
    }
    
    /**
     * Returns the path of logger file
     * @return
     */
    public static String getLogFile(){
    	final String tempFolderPath =  System.getProperty("java.io.tmpdir");
    	return tempFolderPath +"logger.log";
    }

	public static boolean isErrorOccurred() {
		return errorOccurred;
	}

	public static void setErrorOccurred(final boolean errorOccurred) {
		ProgressWatcher.errorOccurred = errorOccurred;
	}
}

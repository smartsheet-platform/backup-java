package com.smartsheet.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyUtils {

	//private static Properties props;
	
    private static final String PROPERTIES_FILENAME = "smartsheet-backup.properties";

    private final static int DEFAULT_DOWNLOAD_THREADS = 4; // optimal if 4 cores
    
    private final static int DEFAULT_ALL_DOWNLOADS_DONE_TIMEOUT_MINUTES = 2; // matches Smartsheet attachment URL expiry
    
    private static final boolean DEFAULT_ZIP_OUTPUT_DIR_FLAG = false;

    private static final boolean DEFAULT_CONTINUE_ON_ERROR_FLAG = true;

	private static Properties props;

	public static String getAccessToken(){
		return getRequiredProp("accessToken");
	}

	public static String getOutputDir(){
		return getRequiredProp("outputDir");
	}
	
	public static boolean isZipOutputDir(){
		return getOptionalProp("zipOutputDir", DEFAULT_ZIP_OUTPUT_DIR_FLAG);
	}
	
	public static int getDownloadThreads(){
		return getOptionalProp("downloadThreads", DEFAULT_DOWNLOAD_THREADS, 1);
	}
	
	public static int getAllDownloadsDoneTimeout(){
		return getOptionalProp("allDownloadsDoneTimeout", DEFAULT_ALL_DOWNLOADS_DONE_TIMEOUT_MINUTES, 0);
	}
	
	public static boolean isContinueOnError(){
		return getOptionalProp("continueOnError", DEFAULT_CONTINUE_ON_ERROR_FLAG);
	}
	
	public static void setProperty(final String propName,final String propValue){
		if(props==null){
			loadTestProperties();
		}
		props.setProperty(propName, propValue);
	}
	
	/**
	 * @param args
	 * @return a folder path built from the args, or an empty string if no args
	 */
	private static String getFolderPathFromArgs(final String[] args) {
		final StringBuffer folderPath = new StringBuffer();
		for (final String arg : args) {
			if (folderPath.length() > 0)
				folderPath.append(" ");
			folderPath.append(arg);
		}
		if (folderPath.length() > 0
				&& !folderPath.toString().endsWith(File.separator))
			folderPath.append(File.separator);
		return folderPath.toString();
	}

	// The following are helper methods for reading properties from a properties file.

    public static Properties loadPropertiesFile(final String[] args) throws IOException {

    	final String folderPath = getFolderPathFromArgs(args);
        final File file = new File(folderPath + PROPERTIES_FILENAME);
        if (!file.isFile())
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());

        ProgressWatcher.notify("Using properties file: " + file.getAbsolutePath());
        final FileReader reader = new FileReader(file);
        try {
            props = new Properties();
            props.load(reader);
            return props;

        } finally {
            reader.close();
        }
    }
    
    private static String getRequiredProp(final String propName) {
        final String propValue = props.getProperty(propName);
        if (propValue == null || propValue.trim().isEmpty()){
            throw new IllegalArgumentException("Property '" + propName + "' is missing");
        }
        return propValue;
    }
    
    private static boolean getOptionalProp(final String propName, final boolean defaultValue) {
        final String propValue = props.getProperty(propName, String.valueOf(defaultValue));
        if (!propValue.equals(Boolean.TRUE.toString()) && !propValue.equals(Boolean.FALSE.toString())){
            throw new IllegalArgumentException("Property '" + propName + "' must be either '" + Boolean.TRUE + "' or '" + Boolean.FALSE + "'");
        }
        return Boolean.valueOf(propValue);
    }
    
    private static int getOptionalProp(final String propName, final int defaultValue, final int minValue) {
        final String propValue = props.getProperty(propName, String.valueOf(defaultValue));
        try {
            final Integer value = Integer.valueOf(propValue);
            if (value < minValue){
                throw new IllegalArgumentException("Property '" + propName + "' (" + value + ") cannot be less than " + minValue);
            }
            return value;
            
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Property '" + propName + "' must be an integer - '" + propValue + "' is an invalid value");
        }
    }

	public static void loadTestProperties() {
		props = new Properties();
	}
	
	public static void logConfiguredProperties(){
		ProgressWatcher.notify(String.format("[Configuration] : Property accessToken : %s ",getAccessToken()));
		ProgressWatcher.notify(String.format("[Configuration] : Property outputDir : %s ",getOutputDir()));
		ProgressWatcher.notify(String.format("[Configuration] : Property downloadThreads : %s ",getDownloadThreads()));
		ProgressWatcher.notify(String.format("[Configuration] : Property allDownloadsDoneTimeout : %s ",getAllDownloadsDoneTimeout()));
		ProgressWatcher.notify(String.format("[Configuration] : Property zipOutputDir : %s ",isZipOutputDir()));
		ProgressWatcher.notify(String.format("[Configuration] : Property continueOnError : %s ",isContinueOnError()));
	}

}

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.smartsheet.restapi.service.ErrorContextualizingSmartsheetService;
import com.smartsheet.restapi.service.RestfulSmartsheetService;
import com.smartsheet.restapi.service.RetryingSmartsheetService;
import com.smartsheet.restapi.service.SmartsheetService;
import com.smartsheet.utils.ConfigHolder;
import com.smartsheet.utils.ProgressWatcher;

/**
 * The main class of the SmartsheetBackupTool utility program.
 */
public class SmartsheetBackupTool {

	private static final String PROPERTIES_FILENAME = "smartsheet-backup.properties";

	private final static int DEFAULT_DOWNLOAD_THREADS = 4; // optimal if 4 cores
	private static final boolean DEFAULT_ZIP_OUTPUT_DIR_FLAG = false;
	private static final boolean DEFAULT_CONTINUE_ON_ERROR_FLAG = false;

	private static final int SUCCESS_EXIT_CODE = 0;
	private static final int FAILURE_EXIT_CODE = -1;

	private static final ProgressWatcher progressWatcher = ProgressWatcher
			.getInstance();
	private static final ConfigHolder configHolder = ConfigHolder.getInstance();
	public static final String VERSION = "1.3";

	/**
	 * The entry point of the program which reads properties, instantiates
	 * services, and backs up the sheets of all active users in an organization
	 * to a local directory (by default) or to a zip file (optionally). Clear
	 * status messages are logged to the console during the backup process, with
	 * summary statistics logged on completion.
	 * 
	 * @param args
	 *            optional - if provided, specifies the folder containing the
	 *            {@literal smartsheet-backup.properties} file, which otherwise
	 *            is expected to be in the current directory
	 */
	public static void main(String[] args) {
		int exitCode = SUCCESS_EXIT_CODE;

		try {
			// 1. read properties
			Properties props = readPropertiesFile(args);
			String accessToken = getRequiredProp(props, "accessToken");
			String outputDir = getRequiredProp(props, "outputDir")
					+ "/"
					+ new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss")
							.format(new Date());
			boolean zipOutputDir = getOptionalProp(props, "zipOutputDir",
					DEFAULT_ZIP_OUTPUT_DIR_FLAG);
			boolean continueOnError = getOptionalProp(props, "continueOnError",
					DEFAULT_CONTINUE_ON_ERROR_FLAG);

			int downloadThreads = getOptionalProp(props, "downloadThreads",
					DEFAULT_DOWNLOAD_THREADS, 1);

			// 2. instantiate services
			SmartsheetService apiService = new ErrorContextualizingSmartsheetService(
			// the ErrorContextualizingSmartsheetService wraps the
			// RetryingSmartsheetService:
					new RetryingSmartsheetService(
					// the RetryingSmartsheetService wraps the
					// RestfulSmartsheetService:
							new RestfulSmartsheetService(accessToken)));

			ParallelDownloadService parallelDownloadService = new ParallelDownloadService(downloadThreads);

			configHolder.setContinueOnError(continueOnError);
			progressWatcher.setLogErrorsToFile(continueOnError);

			SmartsheetBackupService backupService = new SmartsheetBackupService(
					apiService, parallelDownloadService);
			long startTime = System.currentTimeMillis();

			// 3. back up the organization to a local folder
			int numberUsers = backupService.backupOrgTo(new File(outputDir));

			boolean allDownloadJobsDone = parallelDownloadService
					.waitTillAllDownloadJobsDone();
			if (allDownloadJobsDone || configHolder.isContinueOnError()) {

				// 4. if requested, zip up the backup folder which is then
				// deleted
				if (zipOutputDir)
					zipAndDeleteOutputDir(outputDir);

				// 5. tell user how long the backup took and how many users were
				// backed up
				String timeSummary = computeTimeSummary(startTime);
				progressWatcher.notify("*** Org backup done -> [" + numberUsers
						+ "] users total backed up in " + timeSummary + " ***");

				// 6. tell user if there were any errors (in the scenario where
				// they wanted to continue on error)
				int errorCount = progressWatcher.getErrorCount();
				if (errorCount > 0)
					progressWatcher
							.notify(String
									.format("*** NOTE: %d ERRORS occurred - please consult log file [%s]",
											errorCount,
											progressWatcher.getErrorLogFile())
									+ '\n'
									+ "    for details of what was skipped and which may require retry or manual recovery");

			} else
				exitCode = FAILURE_EXIT_CODE;

		} catch (Exception e) {
			// on error tell the user what was the error
			progressWatcher.notifyError(e);

			exitCode = FAILURE_EXIT_CODE;
		}

		System.exit(exitCode);
	}

	private static void zipAndDeleteOutputDir(String outputDirPath)
			throws IOException {
		String zipFilePath = outputDirPath + ".zip";
		File outputDir = new File(outputDirPath);
		zipDirectory(outputDir, new File(zipFilePath));
		deleteFolder(outputDir);
		progressWatcher.notify("Zipped output dir to: "
				+ new File(zipFilePath).getAbsolutePath());
	}

	private static String computeTimeSummary(long startTime) {
		long finishTime = System.currentTimeMillis();
		long duration = finishTime - startTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
		long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		long secondsOverMinute = totalSeconds
				- TimeUnit.MINUTES.toSeconds(minutes);
		if (minutes == 0)
			return totalSeconds + " seconds";
		return minutes + " minutes " + secondsOverMinute + " seconds";
	}

	// The following are helper methods for reading properties from a properties
	// file.

	private static Properties readPropertiesFile(String[] args)
			throws IOException {
		String folderPath = getFolderPathFromArgs(args);
		File file = new File(folderPath + PROPERTIES_FILENAME);
		if (!file.isFile())
			throw new FileNotFoundException("File not found: "
					+ file.getAbsolutePath());

		progressWatcher.notify("Using properties file: "
				+ file.getAbsolutePath());

		FileReader reader = new FileReader(file);
		try {
			Properties props = new Properties();
			props.load(reader);
			return props;

		} finally {
			reader.close();
		}
	}

	/**
	 * @param args
	 * @return a folder path built from the args, or an empty string if no args
	 */
	private static String getFolderPathFromArgs(String[] args) {
		StringBuffer folderPath = new StringBuffer();
		for (String arg : args) {
			if (folderPath.length() > 0)
				folderPath.append(" ");
			folderPath.append(arg);
		}
		if (folderPath.length() > 0
				&& !folderPath.toString().endsWith(File.separator))
			folderPath.append(File.separator);
		return folderPath.toString();
	}

	private static String getRequiredProp(Properties props, String propName) {
		String prop = props.getProperty(propName);
		if (prop == null || prop.trim().isEmpty())
			throw new IllegalArgumentException("Property '" + propName
					+ "' is missing");

		return prop;
	}

	private static int getOptionalProp(Properties props, String propName,
			int defaultValue, int minValue) {
		String prop = props.getProperty(propName, String.valueOf(defaultValue));
		try {
			Integer value = Integer.valueOf(prop);
			if (value < minValue)
				throw new IllegalArgumentException("Property '" + propName
						+ "' (" + value + ") cannot be less than " + minValue);
			return value;

		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Property '" + propName
					+ "' must be an integer - '" + prop
					+ "' is an invalid value");
		}
	}

	private static boolean getOptionalProp(Properties props, String propName,
			boolean defaultValue) {
		String prop = props.getProperty(propName, String.valueOf(defaultValue));
		if (!prop.equals(Boolean.TRUE.toString())
				&& !prop.equals(Boolean.FALSE.toString()))
			throw new IllegalArgumentException("Property '" + propName
					+ "' must be either '" + Boolean.TRUE + "' or '"
					+ Boolean.FALSE + "'");

		return Boolean.valueOf(prop);
	}
}

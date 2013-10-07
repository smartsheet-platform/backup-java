
Video demo: http://screencast.com/t/oce6umyFif


Running the application

- Download the "smartsheet-org-backup-0.0.1-SNAPSHOT.jar", "smartsheet-backup.properties" files into a new folder.
- Open the file "smartsheet-backup.properties" and provide your smartsheet API key for "accessToken" property(refer : http://www.smartsheet.com/developers/api-faq).
- Open the command prompt and navigate to the folder (where you have placed above files).
- enter and run the command "java -jar smartsheet-org-backup-0.0.1-SNAPSHOT.jar"
- You will see detailed logging in the console about backup process.
- Incase if there are any errors your will be provided with log file path on completing backup process execution.
- You can use the newly added flag "continueOnError" to control the backup execution if any exception occurs
	-continueOnError=true (default) means that backup process will continue with next file execution, even if any exception occurs with current file.
	-continueOnError=false means that it will halt the backup process if any exception occurs.

1) Graceful error handling: 

- You can verify this flag "continueOnError" behavior by using below testcases in file "/backup-java/src/test/java/com/smartsheet/tools/test/SmartsheetBackupServiceTest.java"
	-backsUpSmartsheetToLocalDirWithContinueOnErrorTrue() : for testing with flag value as "True"
	-backsUpSmartsheetToLocalDirWithContinueOnErrorTrue() : for testing with flag value as "false"
	I have provided the stub (StubSmartsheetServiceForError) to generate the error by providing invalid download url json

2) Change the way the backup downloads attachments:

- I have moved the querying attachment details to the worker thread, so that there won’t be any latency to download/save the file.

Apart from these i have enhanced the 
- Logging mechanism by using java.util logging with custom "LoggingFormatter".
- Moved loading and reading property values to PropertyUtil class for easy and efficient access.

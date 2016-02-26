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
package com.smartsheet.tools.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.smartsheet.exceptions.ServiceUnavailableException;
import com.smartsheet.exceptions.SmartsheetGetSheetDetailsException;
import com.smartsheet.restapi.service.ErrorContextualizingSmartsheetService;
import com.smartsheet.restapi.service.RetryingSmartsheetService;
import com.smartsheet.tools.ParallelDownloadService;
import com.smartsheet.tools.SmartsheetBackupService;
import com.smartsheet.utils.ConfigHolder;
import com.smartsheet.utils.ProgressWatcher;

/**
 * Note these are <i>integration</i> tests of the {@link SmartsheetBackupService}
 * since they involve the file system, the Internet, and time.
 */
public class SmartsheetBackupServiceTest {

    // test fixture
    private final static int DOWNLOAD_THREADS = 4;
    private ParallelDownloadService parallelDownloadService;

    /**
     * set up test fixture
     */
    @Before
    public void setUp() {
        parallelDownloadService = new ParallelDownloadService(DOWNLOAD_THREADS);
    }

    /**
     * tear down test fixture
     */
    @After
    public void tearDown() {
        // assert all downloads done after wait
        boolean allDownloadJobsDone = parallelDownloadService.waitTillAllDownloadJobsDone();

        if (!ConfigHolder.getInstance().isContinueOnError())
            assertTrue(allDownloadJobsDone);
        // else isContinueOnError, so acceptable if not all jobs done due to errors

        // reset
        setContinueOnError(false);
    }

    @Test
    public void backsUpSmartsheetHierarchyToLocalDir() throws Exception {
        printTestHeader("backsUpSmartsheetHierarchyToLocalDir");

        SmartsheetBackupService backupService = new SmartsheetBackupService(
            new RetryingSmartsheetService(new StubSmartsheetService()),
            parallelDownloadService);

        backupToTempDir(backupService);
    }

    @Test
    public void backsUpSmartsheetHierarchyToLocalDirRecoveringFromServiceUnavailable() throws Exception {
        printTestHeader("backsUpSmartsheetHierarchyToLocalDirRecoveringFromServiceUnavailable");

        SmartsheetBackupService backupService = new SmartsheetBackupService(
            new RetryingSmartsheetService(new StubRecoveringServiceUnavailableSmartsheetService()),
            parallelDownloadService);

        backupToTempDir(backupService);
    }

    @Test(expected = ServiceUnavailableException.class)
    public void retriesOnServiceUnavailableExceptionsUntilMaxRetries() throws Exception {
        printTestHeader("retriesOnServiceUnavailableExceptionsUntilMaxRetries");

        SmartsheetBackupService backupService = new SmartsheetBackupService(
            new RetryingSmartsheetService(new StubServiceUnavailableSmartsheetService()),
            parallelDownloadService);

        backupToTempDir(backupService);
    }

    // random test (simulates random network errors for comprehensive test coverage of scenarios)
    @Test
    public void continuesOnRandomNetworkErrorsIfConfigured() throws Exception {
        printTestHeader("continuesOnExceptions");

        ConfigHolder.getInstance().setContinueOnError(true);
        ProgressWatcher.getInstance().setLogErrorsToFile(true);

        SmartsheetBackupService backupService = new SmartsheetBackupService(
            new ErrorContextualizingSmartsheetService(new StubBadConnectionSmartsheetService()),
            parallelDownloadService);

        backupToTempDir(backupService);

        if (ProgressWatcher.getInstance().getErrorCount() > 0) {
            String errorLogFile = ProgressWatcher.getInstance().getErrorLogFile();
            assertNotNull(errorLogFile);
            printErrorLogFilePathWhenDone(errorLogFile);
        }
    }

    @Test
    public void continuesOnSpecificRequestErrorIfConfigured() throws Exception {
        printTestHeader("continuesOnSpecificRequestErrorIfConfigured");

        setContinueOnError(true);

        StubSpecificRequestFailureSmartsheetService stubSmartsheetService =
            new StubSpecificRequestFailureSmartsheetService();
        stubSmartsheetService.setMakeGetSheetRequestFail(true);

        SmartsheetBackupService backupService = new SmartsheetBackupService(
            new ErrorContextualizingSmartsheetService(stubSmartsheetService),
            parallelDownloadService);

        backupToTempDir(backupService);

        assertTrue(ProgressWatcher.getInstance().getErrorCount() > 0);
        String errorLogFile = ProgressWatcher.getInstance().getErrorLogFile();
        assertNotNull(errorLogFile);
        printErrorLogFilePathWhenDone(errorLogFile);
    }

    @Test
    public void doesNotContinueOnSpecificRequestErrorIfNotConfigured() {
        printTestHeader("doesNotContinueOnSpecificRequestErrorIfNotConfigured");

        // setContinueOnError(false); - not needed because default value is already false

        StubSpecificRequestFailureSmartsheetService stubSmartsheetService =
            new StubSpecificRequestFailureSmartsheetService();
        stubSmartsheetService.setMakeGetSheetRequestFail(true);

        SmartsheetBackupService backupService = new SmartsheetBackupService(
            new ErrorContextualizingSmartsheetService(stubSmartsheetService),
            parallelDownloadService);

        try {
            backupToTempDir(backupService);
            fail("Did not get exception even though continueOnError false");

        } catch (Exception e) {
            assertTrue(e.getClass().equals(SmartsheetGetSheetDetailsException.class));
            assertTrue(ProgressWatcher.getInstance().getErrorCount() > 0);
        }
    }

    // helpers

    private static void setContinueOnError(boolean continueOnError) {
        ConfigHolder.getInstance().setContinueOnError(continueOnError);
        // the backup tool does setLogErrorsToFile true if continueOnError is true,
        // so simulate that behaviour by having the test do the same here:
        ProgressWatcher.getInstance().setLogErrorsToFile(continueOnError);
    }

    private static void printTestHeader(String testName) {
        System.out.println("-------------------- TEST: " + testName  + " --------------------");
    }

    private void printErrorLogFilePathWhenDone(String errorLogFile) {
        parallelDownloadService.waitTillAllDownloadJobsDone();
        System.out.println("-------------------- check error log file: " + errorLogFile  + " --------------------");
    }

	private void backupToTempDir(SmartsheetBackupService backupService) throws Exception {
        Date timeBeforeBackup = new Date();

        File backupFolder = new File(
            System.getProperty("java.io.tmpdir"),
            getClass().getSimpleName() + "-" + System.currentTimeMillis());

        backupService.backupOrgTo(backupFolder);

        assertTrue(backupFolder.exists());
        assertTrue(backupFolder.lastModified() > timeBeforeBackup.getTime());
    }
}

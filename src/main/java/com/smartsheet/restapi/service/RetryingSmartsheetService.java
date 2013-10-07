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
package com.smartsheet.restapi.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.smartsheet.exceptions.ServiceUnavailableException;
import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;
import com.smartsheet.utils.ProgressWatcher;

/**
 * A wrapper around a {@link SmartsheetService} delegate which retries when
 * the delegate fails with {@link ServiceUnavailableException}.
 * <p>
 * Used to encapsulate the retry algorithm from a client of the
 * {@link SmartsheetService} delegate. The client simply calls the wrapper
 * unaware of retry attempts being made on the delegate as necessary.
 */
public class RetryingSmartsheetService implements SmartsheetService {

    public static final int MAX_RETRIES = 5;
    private static final int WAIT_INTERVAL_SECS = 5;

    private final SmartsheetService delegateService;

    public RetryingSmartsheetService(SmartsheetService delegateService) {
        this.delegateService = delegateService;
    }

    @Override
    public List<SmartsheetUser> getUsers() throws Exception {
        ServiceUnavailableException finalException = null;

        for (int i = 0; i <= MAX_RETRIES; i++) {
            notifyIfRetry(i);
            try {
                return delegateService.getUsers();

            } catch (ServiceUnavailableException e) {
                if (i < MAX_RETRIES)
                    sleepForDefinedInterval(i+1, "getUsers");
                else
                    finalException = e;
            }
        }

        throw finalException;
    }

    @Override
    public SmartsheetHome getHome() throws Exception {
        ServiceUnavailableException finalException = null;

        for (int i = 0; i <= MAX_RETRIES; i++) {
            notifyIfRetry(i);
            try {
                return delegateService.getHome();

            } catch (ServiceUnavailableException e) {
                if (i < MAX_RETRIES)
                    sleepForDefinedInterval(i+1, "getHome");
                else
                    finalException = e;
            }
        }

        throw finalException;
    }

    @Override
    public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {
        ServiceUnavailableException finalException = null;

        for (int i = 0; i <= MAX_RETRIES; i++) {
            notifyIfRetry(i);
            try {
                return delegateService.getSheetDetails(sheetName, sheetId);

            } catch (ServiceUnavailableException e) {
                if (i < MAX_RETRIES)
                    sleepForDefinedInterval(i+1, "getSheetDetails");
                else
                    finalException = e;
            }
        }

        throw finalException;
    }

    @Override
    public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName) throws Exception {
        ServiceUnavailableException finalException = null;

        for (int i = 0; i <= MAX_RETRIES; i++) {
            notifyIfRetry(i);
            try {
                return delegateService.getAttachmentDetails(attachmentName, attachmentId, sheetName);

            } catch (ServiceUnavailableException e) {
                if (i < MAX_RETRIES)
                    sleepForDefinedInterval(i+1, "getAttachmentDetails");
                else
                    finalException = e;
            }
        }

        throw finalException;
    }

    @Override
    public String getAccessToken() {
        return delegateService.getAccessToken();
    }

    private static void notifyIfRetry(int i) {
        if (i > 0)
            ProgressWatcher.notify("--- retry #" + i);
    }

    public static void sleepForDefinedInterval(int retryNumber, String action) throws InterruptedException {
        int sleepSecs = retryNumber * WAIT_INTERVAL_SECS;
        ProgressWatcher.notify("503 (Service Unavailable) received for [" + action + "] - sleep " + sleepSecs + " secs before retry...");
        Thread.sleep(TimeUnit.SECONDS.toMillis(sleepSecs));
    }

    @Override
    public void assumeUser(String assumedUserEmail) {
        delegateService.assumeUser(assumedUserEmail);
    }

    @Override
    public String getAssumedUser() {
        return delegateService.getAssumedUser();
    }
}

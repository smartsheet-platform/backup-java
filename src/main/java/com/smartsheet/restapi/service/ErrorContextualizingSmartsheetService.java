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

import com.smartsheet.exceptions.SmartsheetGetAttachmentDetailsException;
import com.smartsheet.exceptions.SmartsheetGetHomeException;
import com.smartsheet.exceptions.SmartsheetGetSheetDetailsException;
import com.smartsheet.exceptions.SmartsheetGetUsersException;
import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;

/**
 * A wrapper around a {@link SmartsheetService} delegate which contextualizes
 * {@link Exception}s thrown by the delegate. The contextualization is done by
 * wrapping the {@link Exception} from the delegate in a new {@link Exception}
 * which adds context based on the current service method being invoked and the
 * parameters of this invocation. It is this new {@link Exception} which is thrown.
 */
public class ErrorContextualizingSmartsheetService implements SmartsheetService {

    private final SmartsheetService delegateService;

    public ErrorContextualizingSmartsheetService(SmartsheetService delegateService) {
        this.delegateService = delegateService;
    }

    @Override
    public List<SmartsheetUser> getUsers() throws Exception {
        try {
            return delegateService.getUsers();
        } catch (Exception e) {
            throw new SmartsheetGetUsersException(e);
        }
    }

    @Override
    public SmartsheetHome getHome() throws Exception {
        try {
            return delegateService.getHome();
        } catch (Exception e) {
            throw new SmartsheetGetHomeException(e);
        }
    }

    @Override
    public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {
        try {
            return delegateService.getSheetDetails(sheetName, sheetId);
        } catch (Exception e) {
            throw new SmartsheetGetSheetDetailsException(e, sheetName, sheetId);
        }
    }

    @Override
    public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName) throws Exception {
        try {
            return delegateService.getAttachmentDetails(attachmentName, attachmentId, sheetName);
        } catch (Exception e) {
            throw new SmartsheetGetAttachmentDetailsException(e, attachmentName, attachmentId, sheetName);
        }
    }

    @Override
    public String getAccessToken() {
        return delegateService.getAccessToken();
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

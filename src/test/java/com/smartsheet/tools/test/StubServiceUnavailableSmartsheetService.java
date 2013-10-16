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

import java.util.List;

import com.smartsheet.exceptions.ServiceUnavailableException;
import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;
import com.smartsheet.restapi.service.SmartsheetService;

/**
 * A stub implementation of interface {@link SmartsheetService} which always
 * throws {@link ServiceUnavailableException}.
 */
public class StubServiceUnavailableSmartsheetService implements SmartsheetService {

    @Override
    public List<SmartsheetUser> getUsers() throws Exception {
        throw new ServiceUnavailableException();
    }

    @Override
    public SmartsheetHome getHome() throws Exception {
        throw new ServiceUnavailableException();
    }

    @Override
    public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {
        throw new ServiceUnavailableException();
    }

    @Override
    public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName) throws Exception {
        throw new ServiceUnavailableException();
    }

    @Override
    public String getAccessToken() {
        return null;
    }

    @Override
    public void assumeUser(String assumedUserEmail) {
    }

    @Override
    public String getAssumedUser() {
        return null;
    }
}

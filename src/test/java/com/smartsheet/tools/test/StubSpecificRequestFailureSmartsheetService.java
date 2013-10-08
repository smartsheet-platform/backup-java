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

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.service.SmartsheetService;

/**
 * A stub implementation of interface {@link SmartsheetService} which extends
 * {@link StubBadConnectionSmartsheetService}.
 * Whereas {@link StubBadConnectionSmartsheetService} simulates random network
 * errors, {@link StubSpecificRequestFailureSmartsheetService} lets the tester
 * control which requests fail due to simulated network error.
 * Tests can thus be performed deterministically using this stub.
 */
public class StubSpecificRequestFailureSmartsheetService extends StubBadConnectionSmartsheetService {

    private boolean makeGetHomeRequestFail = false;
    private boolean makeGetSheetRequestFail = false;
    private boolean makeGetAttachmentRequestFail = false;

    public StubSpecificRequestFailureSmartsheetService() {
        simulateRandomConnectionErrors = false; // override random behaviour of superclass
    }

    public void setMakeGetHomeRequestFail(boolean makeGetHomeRequestFail) {
        this.makeGetHomeRequestFail = makeGetHomeRequestFail;
    }

    public void setMakeGetSheetRequestFail(boolean makeGetSheetRequestFail) {
        this.makeGetSheetRequestFail = makeGetSheetRequestFail;
    }

    public void setMakeGetAttachmentRequestFail(boolean makeGetAttachmentRequestFail) {
        this.makeGetAttachmentRequestFail = makeGetAttachmentRequestFail;
    }

    @Override
    public SmartsheetHome getHome() throws Exception {
        if (makeGetHomeRequestFail)
            throw fakeConnectionException();

        return super.getHome();
    }

    @Override
    public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {
        if (makeGetSheetRequestFail)
            throw fakeConnectionException();

        return super.getSheetDetails(sheetName, sheetId);
    }

    @Override
    public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName) throws Exception {
        if (makeGetAttachmentRequestFail)
            throw fakeConnectionException();

        return super.getAttachmentDetails(attachmentName, attachmentId, sheetName);
    }
}

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

import java.io.IOException;
import java.util.Random;

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.service.SmartsheetService;

/**
 * A stub implementation of interface {@link SmartsheetService} which simulates
 * operating on a bad connection by randomly throwing {@link IOException} from
 * user-specific methods.
 */
public class StubBadConnectionSmartsheetService extends StubSmartsheetService {

    private static final String INVALID_S3_URL = "https://s3.amazonaws.com/SmartsheetBx/73de1644e44ad916e6b9e937cec2d";

    protected boolean simulateRandomConnectionErrors = true;

    @Override
    public SmartsheetHome getHome() throws Exception {

        if (isConnectionCurrentlyBad())
            throw fakeConnectionException();

        return super.getHome();
    }

    @Override
    public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {

        if (isConnectionCurrentlyBad())
            throw fakeConnectionException();

        return super.getSheetDetails(sheetName, sheetId);
    }

    @Override
    public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName) throws Exception {

        if (isConnectionCurrentlyBad())
            throw fakeConnectionException();

        SmartsheetAttachment attachmentDetails = super.getAttachmentDetails(attachmentName, attachmentId, sheetName);

        if (isConnectionCurrentlyBad()) {
            // fake the connection being bad when the caller tries to retrieve
            // the attachment referenced in the attachment details by returning an
            // invalid S3 URL
            attachmentDetails.setUrl(INVALID_S3_URL);
        }

        return attachmentDetails;
    }

    private boolean isConnectionCurrentlyBad() {
        if (simulateRandomConnectionErrors)
            return new Random().nextBoolean();

        return false;
    }

    protected static IOException fakeConnectionException() {
        return new IOException("Connection refused");
    }
}

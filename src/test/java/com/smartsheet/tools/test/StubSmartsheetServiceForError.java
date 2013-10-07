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
import com.smartsheet.restapi.service.JsonDeserializer;
import com.smartsheet.testutils.TestUtils;

/**
 * A stub for invalid url json loading extends {@link StubSmartsheetService}.
 */
public class StubSmartsheetServiceForError extends StubSmartsheetService {

    @Override
	public SmartsheetAttachment getAttachmentDetails(final long attachmentId) throws Exception {

        final String json = TestUtils.getSampleGetAttachmentJsonResponseWithInvalidUrl();
        return new JsonDeserializer<SmartsheetAttachment>().deserialize(json, SmartsheetAttachment.class);
    }
}

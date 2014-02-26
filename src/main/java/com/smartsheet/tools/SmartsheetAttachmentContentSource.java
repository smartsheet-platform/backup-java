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

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.service.SmartsheetService;

/**
 * An {@link InternetContentSource} which is an ethereal Smartsheet attachment.
 */
public class SmartsheetAttachmentContentSource implements InternetContentSource {

    private final SmartsheetService apiService;
    private final SmartsheetAttachment attachment;
    private final String sheetName;

    public SmartsheetAttachmentContentSource(SmartsheetService apiService,
            SmartsheetAttachment attachment, String sheetName) {
        this.apiService = apiService;
        this.attachment = attachment;
        this.sheetName = sheetName;
    }

    @Override
    public String getURL() throws Exception {
    	SmartsheetAttachment attachmentDetails = apiService.getAttachmentDetails(
            attachment.getName(), attachment.getId(), sheetName);
        
    	return attachmentDetails.getUrl(); // ethereal - valid for only 2 minutes
    }
}

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

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;

/**
 * An abstraction of a service for Smartsheet requests.
 */
public interface SmartsheetService {

    List<SmartsheetUser> getUsers() throws Exception;

    SmartsheetHome getHome() throws Exception;

    SmartsheetSheet getSheetDetails(long sheetId) throws Exception;

    SmartsheetAttachment getAttachmentDetails(long attachmentId) throws Exception;

    String getAccessToken();

    void assumeUser(String assumedUserEmail);

    String getAssumedUser();
}

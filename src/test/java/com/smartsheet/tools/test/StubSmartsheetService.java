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

import org.codehaus.jackson.type.TypeReference;

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetPagingwrapper;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;
import com.smartsheet.restapi.service.JsonDeserializer;
import com.smartsheet.restapi.service.SmartsheetService;
import com.smartsheet.testutils.TestUtils;

/**
 * A stub implementation of interface {@link SmartsheetService}.
 */
public class StubSmartsheetService implements SmartsheetService {

    @Override
    public SmartsheetPagingwrapper<SmartsheetUser> getUsers(int page) throws Exception {

        String json = TestUtils.getSampleGetUsersJsonResponse();
        return (new JsonDeserializer<SmartsheetPagingwrapper<SmartsheetUser>>().deserialize(json,
				new TypeReference<SmartsheetPagingwrapper<SmartsheetUser>>() {}));
    }

    @Override
    public SmartsheetHome getHome() throws Exception {

        String json = TestUtils.getSampleGetHomeJsonResponse();
        return new JsonDeserializer<SmartsheetHome>().deserialize(json, SmartsheetHome.class);
    }

    @Override
    public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {

        String json = TestUtils.getSampleGetSheetJsonResponse();
        return new JsonDeserializer<SmartsheetSheet>().deserialize(json, SmartsheetSheet.class);
    }

    @Override
    public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName,long sheetId) throws Exception {

        String json = TestUtils.getSampleGetAttachmentJsonResponse();
        return new JsonDeserializer<SmartsheetAttachment>().deserialize(json, SmartsheetAttachment.class);
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
    
    @Override
	public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }

    @Override
    public String getApiBaseUrl() {
        return null;
    }
}

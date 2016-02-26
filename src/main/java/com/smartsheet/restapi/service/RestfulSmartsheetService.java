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

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetPagingwrapper;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;
import com.smartsheet.utils.HttpUtils;

/**
 * A RESTful implementation of the {@link Smartsheet} interface, i.e., using the
 * Smartsheet REST API to provide the service functionality. An instance of the
 * service is constructed with an access token argument. The instance can be
 * requested to assume the identity of another user on demand as long as the
 * access token provided was for an administrator of that user's organization.
 */
public class RestfulSmartsheetService implements SmartsheetService, Cloneable {

	public static final String API_BASE_URL = "https://api.smartsheet.com/2.0/";

	private String accessToken;
	private String assumedUserEmail;

	public RestfulSmartsheetService(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public SmartsheetPagingwrapper<SmartsheetUser> getUsers(int page) throws Exception {
		String json = getJsonPayload(API_BASE_URL + "users?page=" + page);
		return (new JsonDeserializer<SmartsheetPagingwrapper<SmartsheetUser>>().deserialize(json,
				new TypeReference<SmartsheetPagingwrapper<SmartsheetUser>>() {
				}));
	}

	@Override
	public SmartsheetHome getHome() throws Exception {

		String json = getJsonPayload(API_BASE_URL + "home");
		return new JsonDeserializer<SmartsheetHome>().deserialize(json, SmartsheetHome.class);
	}

	@Override
	public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {

		String json = getJsonPayload(API_BASE_URL + "sheets/" + sheetId + "?include=attachments,discussions");
		return new JsonDeserializer<SmartsheetSheet>().deserialize(json, SmartsheetSheet.class);
	}

	@Override
	public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName,
			long sheetId) throws Exception {

		String json = getJsonPayload(API_BASE_URL + "sheets/" + sheetId + "/attachments/" + attachmentId);
		return new JsonDeserializer<SmartsheetAttachment>().deserialize(json, SmartsheetAttachment.class);
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	private String getJsonPayload(String url) throws IOException {
		return HttpUtils.getJsonPayload(url, accessToken, assumedUserEmail);
	}

	@Override
	public void assumeUser(String assumedUserEmail) {
		this.assumedUserEmail = assumedUserEmail;
	}

	@Override
	public String getAssumedUser() {
		return assumedUserEmail;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		RestfulSmartsheetService rss = (RestfulSmartsheetService) super.clone();

		rss.assumedUserEmail = this.assumedUserEmail;
		rss.accessToken = this.accessToken;

		return rss;
	}
}

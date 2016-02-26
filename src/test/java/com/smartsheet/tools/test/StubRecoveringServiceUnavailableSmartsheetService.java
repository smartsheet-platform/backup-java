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
import java.util.concurrent.atomic.AtomicInteger;

import com.smartsheet.exceptions.ServiceUnavailableException;
import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetPagingwrapper;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;

/**
 * A {@link StubSmartsheetService} which always throws
 * {@link ServiceUnavailableException} on the first try but recovers on
 * subsequent tries.
 */
public class StubRecoveringServiceUnavailableSmartsheetService extends StubSmartsheetService {

	private final AtomicInteger countGetUserCalls = new AtomicInteger();
	private final AtomicInteger countGetHomeCalls = new AtomicInteger();
	private final AtomicInteger countGetSheetDetails = new AtomicInteger();
	private final AtomicInteger countGetAttachmentDetails = new AtomicInteger();

	@Override
	public SmartsheetPagingwrapper<SmartsheetUser> getUsers(int page) throws Exception {
		if (countGetUserCalls.getAndIncrement() == 0)
			throw new ServiceUnavailableException();
		return super.getUsers(page);
	}

	@Override
	public SmartsheetHome getHome() throws Exception {
		if (countGetHomeCalls.getAndIncrement() == 0)
			throw new ServiceUnavailableException();
		return super.getHome();
	}

	@Override
	public SmartsheetSheet getSheetDetails(String sheetName, long sheetId) throws Exception {
		if (countGetSheetDetails.getAndIncrement() == 0)
			throw new ServiceUnavailableException();
		return super.getSheetDetails(sheetName, sheetId);
	}

	@Override
	public SmartsheetAttachment getAttachmentDetails(String attachmentName, long attachmentId, String sheetName,
			long sheetId) throws Exception {
		if (countGetAttachmentDetails.getAndIncrement() == 0)
			throw new ServiceUnavailableException();
		return super.getAttachmentDetails(attachmentName, attachmentId, sheetName, sheetId);
	}

	@Override
	public String getAccessToken() {
		return null;
	}

}

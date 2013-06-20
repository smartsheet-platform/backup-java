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
package com.smartsheet.restapi.model.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;

import com.smartsheet.restapi.model.SmartsheetAttachment;
import com.smartsheet.restapi.model.SmartsheetHome;
import com.smartsheet.restapi.model.SmartsheetSheet;
import com.smartsheet.restapi.model.SmartsheetUser;
import com.smartsheet.restapi.service.JsonDeserializer;
import com.smartsheet.testutils.TestUtils;

public class SmartsheetModelDeserializationTest {

    @Test
    public void deserializesSmartsheetHome() throws JsonMappingException, JsonParseException, IOException {
        JsonDeserializer<SmartsheetHome> deserializer = new JsonDeserializer<SmartsheetHome>();
        String json = TestUtils.getSampleGetHomeJsonResponse();
        SmartsheetHome home = deserializer.deserialize(json, SmartsheetHome.class);
        assertNotNull(home);
    }

    @Test
    public void deserializesSmartsheetSheet() throws JsonMappingException, JsonParseException, IOException {
        JsonDeserializer<SmartsheetSheet> deserializer = new JsonDeserializer<SmartsheetSheet>();
        String json = TestUtils.getSampleGetSheetJsonResponse();
        SmartsheetSheet sheet = deserializer.deserialize(json, SmartsheetSheet.class);
        assertNotNull(sheet);
    }

    @Test
    public void deserializesSmartsheetAttachment() throws JsonMappingException, JsonParseException, IOException {
        JsonDeserializer<SmartsheetAttachment> deserializer = new JsonDeserializer<SmartsheetAttachment>();
        String json = TestUtils.getSampleGetAttachmentJsonResponse();
        SmartsheetAttachment attachment = deserializer.deserialize(json, SmartsheetAttachment.class);
        assertNotNull(attachment);
    }

    @Test
    public void deserializesSmartsheetUsers() throws JsonMappingException, JsonParseException, IOException {
        JsonDeserializer<SmartsheetUser> deserializer = new JsonDeserializer<SmartsheetUser>();
        String json = TestUtils.getSampleGetUsersJsonResponse();
        List<SmartsheetUser> users = deserializer.deserializeArray(json, SmartsheetUser.class);
        assertNotNull(users);
        assertFalse(users.isEmpty());
        for (SmartsheetUser user : users) {
            assertNotNull(user);
        }
    }
}

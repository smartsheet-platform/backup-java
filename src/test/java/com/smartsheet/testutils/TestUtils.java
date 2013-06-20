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
package com.smartsheet.testutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class TestUtils {

    private TestUtils() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
    }

    public static String getSampleGetHomeJsonResponse() throws IOException {
        return getSampleJsonPayload("smartsheet-get-home-response");
    }

    public static String getSampleGetSheetJsonResponse() throws IOException {
        return getSampleJsonPayload("smartsheet-get-sheet-response");
    }

    public static String getSampleGetAttachmentJsonResponse() throws IOException {
        return getSampleJsonPayload("smartsheet-get-attachment-response");
    }

    public static String getSampleGetUsersJsonResponse() throws IOException {
        return getSampleJsonPayload("smartsheet-get-users-response");
    }

    private static String getSampleJsonPayload(String fileName) throws IOException {
        String filePath = "sample-payloads/" + fileName + ".json";
        URL fileUrl = TestUtils.class.getResource(filePath);
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
        try {
            StringBuffer json = new StringBuffer();
            String line;
            while ((line = fileReader.readLine()) != null) {
                json.append(line);
            }
            return json.toString();

        } finally {
            fileReader.close();
        }
    }
}

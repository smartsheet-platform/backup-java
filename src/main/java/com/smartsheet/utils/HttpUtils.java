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
package com.smartsheet.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.smartsheet.exceptions.ServiceUnavailableException;
import com.smartsheet.restapi.service.RetryingSmartsheetService;
import com.smartsheet.tools.SmartsheetBackupTool;

/**
 * Utilities for HTTP operations.
 */
public class HttpUtils {

    private static final String CHARSET = "UTF-8";
    private static final String ACCEPT_JSON_HEADER = "application/json; charset=" + CHARSET.toLowerCase();
    private static final int ATTACHMENT_BUFFER_SIZE = 64*1024; // 64K

    private HttpUtils() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
    }

    /**
     * Gets the JSON payload (as a String) returned by invoking HTTP GET on the
     * specified URL, with the optional accessToken and userToAssume arguments.
     */
    public static String getJsonPayload(String url, String accessToken, String userToAssume)
            throws IOException {

        HttpGet httpGet = newGetRequest(url, accessToken, ACCEPT_JSON_HEADER, userToAssume);
        HttpResponse response = getResponse(httpGet);
        try {
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == ServiceUnavailableException.SERVICE_UNAVAILABLE_CODE)
                throw new ServiceUnavailableException(url);

            InputStream content = getContentOnSuccess(response, url, status);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            InputStream inStream = new BufferedInputStream(content);
            OutputStream outStream = new BufferedOutputStream(byteArrayOutputStream);
            copyAndClose(inStream, outStream);

            return byteArrayOutputStream.toString(CHARSET);

        } finally {
            httpGet.releaseConnection();
        }
    }

    /**
     * Saves the contents at the specified URL to a local file, with the optional
     * accessToken and userToAssume arguments used when requesting the URL.
     */
    public static void saveUrlToFile(String url, File file, String accessToken, String acceptHeader, String userToAssume) 
            throws InterruptedException, IOException {
        IOException finalException = null;
        // This logic should be moved to the smartsheet api class. adding retry logic here for expediency.
        for (int i = 0; i <= RetryingSmartsheetService.MAX_RETRIES; i++) {
            try {
                HttpGet httpGet = newGetRequest(url, accessToken, acceptHeader, userToAssume);
                HttpResponse response = getResponse(httpGet);
                try {
                    StatusLine status = response.getStatusLine();
                    if (status.getStatusCode() == 403 && accessToken == null)
                        return; // ignore 403 if accessToken null for test mode

                    if (status.getStatusCode() == ServiceUnavailableException.SERVICE_UNAVAILABLE_CODE)
                        throw new ServiceUnavailableException(url);

                    InputStream content = getContentOnSuccess(response, url, status);
                    InputStream inStream = new BufferedInputStream(content, ATTACHMENT_BUFFER_SIZE);
                    if (file.exists())
                        file.delete();

                    OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file), ATTACHMENT_BUFFER_SIZE);
                    copyAndClose(inStream, outStream);
                    return;

                } finally {
                    httpGet.releaseConnection();
                }
            } catch (ServiceUnavailableException sue) {
                if (i < RetryingSmartsheetService.MAX_RETRIES)
                    RetryingSmartsheetService.sleepForDefinedInterval(i+1, "saveUrlToFile");
                else
                    finalException = sue;

            } catch (IOException unexpected) {
                // There was an unexpected error getting the content at the URL.
                // We'll try again immediately, unless we've reached MAX_RETRIES.
                if (i < RetryingSmartsheetService.MAX_RETRIES)
                    ProgressWatcher.getInstance().notify(String.format(
                        "There was an issue while attempting to download [%s] to [%s]. Retrying...",
                        url, file.getAbsolutePath()));
                else
                    finalException = unexpected;
            }
        }
        throw finalException;
    }

    public static void saveUrlToFile(String url, File file) throws InterruptedException, IOException {
        saveUrlToFile(url, file, null, null, null);
    }

    /**
     * Creates a new HTTP GET request, where accessToken, acceptHeader, and
     * userToAssume arguments are all optional.
     */
    private static HttpGet newGetRequest(String url, String accessToken, String acceptHeader, String userToAssume)
            throws UnsupportedEncodingException {
		
        HttpGet httpGet = new HttpGet(url);
        
        httpGet.addHeader("User-Agent","Smartsheet Org Backup Tool/"+SmartsheetBackupTool.VERSION+" " + 
				System.getProperty("os.name") + " "+System.getProperty("java.vm.name") + " " + 
				System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        
        if (accessToken != null)
            httpGet.addHeader("Authorization", "Bearer " + accessToken);
        if (acceptHeader != null)
            httpGet.addHeader("Accept", acceptHeader);
        if (userToAssume != null)
            httpGet.addHeader("Assume-User", URLEncoder.encode(userToAssume, CHARSET));
        return httpGet;
    }

    /**
     * Invokes a HTTP GET and returns the response.
     */
    private static HttpResponse getResponse(HttpGet httpGet) throws IOException, ClientProtocolException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        return httpclient.execute(httpGet);
    }

    /**
     * Gets the content of the HTTP response as an input stream if the response
     * status is success (200). Otherwise {@link IOException} is thrown.
     */
    private static InputStream getContentOnSuccess(HttpResponse response, String url, StatusLine status)
            throws IOException {

        int statusCode = status.getStatusCode();
        String reason = status.getReasonPhrase();
        
        
        
        if(statusCode >= 500){
        	// This exception is caught and causes it to try again.
        	throw new ServiceUnavailableException(url);
        }else if (statusCode != 200) {
            // Try to get the response as well
            String errorResponse = "";
            try {
                errorResponse = org.apache.commons.io.IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            }catch(Exception e) {
                //ignore
            }
            throw new IOException("GET " + url + " returned: " + statusCode + " - " + reason + " " + errorResponse+"\n");
        }

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    /**
     * Copies an input stream to an output stream, closing the output stream before returning.
     */
    private static void copyAndClose(InputStream inStream, OutputStream outStream) throws IOException {
        try {
            byte[] bytes = new byte[ATTACHMENT_BUFFER_SIZE];
            int actualRead;
            while ((actualRead = inStream.read(bytes, 0, ATTACHMENT_BUFFER_SIZE)) != -1) {
                outStream.write(bytes, 0, actualRead);
            }

        } finally {
        	outStream.flush();
            outStream.close();
        }
    }
}

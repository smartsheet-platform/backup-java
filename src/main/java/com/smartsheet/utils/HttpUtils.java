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
    public static String getJsonPayload(final String url, final String accessToken, final String userToAssume)
            throws IOException {

        final HttpGet httpGet = newGetRequest(url, accessToken, ACCEPT_JSON_HEADER, userToAssume);
        final HttpResponse response = getResponse(httpGet);
        try {
            final StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == ServiceUnavailableException.SERVICE_UNAVAILABLE_CODE)
                throw new ServiceUnavailableException(url);

            final InputStream content = getContentOnSuccess(response, url, status);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            final InputStream inStream = new BufferedInputStream(content);
            final OutputStream outStream = new BufferedOutputStream(byteArrayOutputStream);
            copyAndClose(inStream, outStream, url,userToAssume);

            return byteArrayOutputStream.toString(CHARSET);

        } finally {
            httpGet.releaseConnection();
        }
    }

    /**
     * Saves the contents at the specified URL to a local file, with the optional
     * accessToken and userToAssume arguments used when requesting the URL.
     * @throws InterruptedException 
     * @throws IOException 
     */
    public static void saveUrlToFile(final String url, final File file, final String accessToken, final String acceptHeader, final String userToAssume)
            throws InterruptedException, IOException {
    	 //final ServiceUnavailableException finalException = null;
    	 //This logic should be moved to the smartsheet api class. adding retry logic here for expediency.
    	for (int i = 0; i <= RetryingSmartsheetService.MAX_RETRIES; i++) {
    	try {
	        final HttpGet httpGet = newGetRequest(url, accessToken, acceptHeader, userToAssume);
	        final HttpResponse response = getResponse(httpGet);
	        try {
	            final StatusLine status = response.getStatusLine();
	            if (status.getStatusCode() == 403 && accessToken == null)
	                return; // ignore 403 if accessToken null for test mode
	            if (status.getStatusCode() == ServiceUnavailableException.SERVICE_UNAVAILABLE_CODE)
	                throw new ServiceUnavailableException(url);
	            
	            final InputStream content = getContentOnSuccess(response, url, status);
	            final InputStream inStream = new BufferedInputStream(content, ATTACHMENT_BUFFER_SIZE);
	            if (file.exists()) {
	            	file.delete();
	            }
	            final OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file), ATTACHMENT_BUFFER_SIZE);
	            copyAndClose(inStream, outStream,file.getName(),userToAssume);
	            return;
	        } finally {
	            httpGet.releaseConnection();
	        }
    	}  catch (final ServiceUnavailableException sue) {
    		 if (i < RetryingSmartsheetService.MAX_RETRIES){
    			 RetryingSmartsheetService.sleepForDefinedInterval(i+1, "saveUrlToFile");
    		 }
             else{
            	 ProgressWatcher.notifyError(String.format("[UserId : %s] : An unexpected error occured while attempting to download %s",userToAssume, file.getName()));
     			if(!PropertyUtils.isContinueOnError()){
     				throw sue;
     			}
     			continue;
             }
    		} catch (final IOException se) {
    			//There was an error downloading the sheet. We'll try again.
        		if (i >= RetryingSmartsheetService.MAX_RETRIES) {
        			ProgressWatcher.notifyError(String.format("[UserId : %s] : An unexpected error occured while attempting to download %s",userToAssume, file.getName()));
        			if(!PropertyUtils.isContinueOnError()){
        				throw se;
        			}
        			continue;
        		}
        	}
        }
    }

    public static void saveUrlToFile(final String url, final File file) throws IOException, InterruptedException {
        saveUrlToFile(url, file, null, null, null);
    }

    /**
     * Creates a new HTTP GET request, where accessToken, acceptHeader, and
     * userToAssume arguments are all optional.
     */
    private static HttpGet newGetRequest(final String url, final String accessToken, final String acceptHeader, final String userToAssume)
            throws UnsupportedEncodingException {

        final HttpGet httpGet = new HttpGet(url);
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
    private static HttpResponse getResponse(final HttpGet httpGet) throws IOException, ClientProtocolException {
        final DefaultHttpClient httpclient = new DefaultHttpClient();
        return httpclient.execute(httpGet);
    }

    /**
     * Gets the content of the HTTP response as an input stream if the response
     * status is success (200). Otherwise {@link IOException} is thrown.
     */
    private static InputStream getContentOnSuccess(final HttpResponse response, final String url, final StatusLine status)
            throws IOException {

        final int statusCode = status.getStatusCode();
        final String reason = status.getReasonPhrase();
        if (statusCode != 200)
            throw new IOException("GET " + url + " returned " + statusCode + " (" + reason + ")");

        final HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    /**
     * Copies an input stream to an output stream, closing the output stream before returning.
     * @param userToAssume 
     * @param file 
     */
    private static void copyAndClose(final InputStream inStream, final OutputStream outStream, final String resourceName, final String userToAssume) throws IOException {
        try {
            final byte[] bytes = new byte[ATTACHMENT_BUFFER_SIZE];
            int actualRead;
            while ((actualRead = inStream.read(bytes, 0, ATTACHMENT_BUFFER_SIZE)) != -1) {
                outStream.write(bytes, 0, actualRead);
            }

        }catch (final IOException e) {
			ProgressWatcher.notifyError(String.format("[UserId : %s] : Error while reading/writing %s",userToAssume, resourceName));
			throw e;
		} finally {
            outStream.close();
        }
    }
}

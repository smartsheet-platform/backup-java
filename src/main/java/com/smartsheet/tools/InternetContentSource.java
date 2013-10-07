package com.smartsheet.tools;

/**
 * A content source on the Internet addressable by a URL. Typically represents a file.
 */
public interface InternetContentSource {

    String getURL() throws Exception;
}

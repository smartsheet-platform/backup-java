package com.smartsheet.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ConfigHolder class.
 * @author isim
 *
 */
public class ConfigHolderTest {
  
    private static final String DEFAULT_ACCESS_TOKEN = "[access token]";
    private static final String DEFAULT_BACKUP_FOLDER = "backup/smartsheet-backup";
    private static final boolean DEFAULT_CONTINUE_ON_ERROR = true;
    private static final String DEFAULT_ZIP_OUTPUT_FOLDER = "zip";
    private static final int DEFAULT_NUM_DOWNLOAD_THREADS = 4;
    private static final int DEFAULT_ALL_DOWNLOADS_TIMEOUT = 2;
    
    private ConfigHolder configHolder;

    @Before
    public void setUp(){
        configHolder = ConfigHolder.getInstance();
    }
    
    @Test
    public void testConfigHolder_CanLoadFromPropertiesFile() {
        Assert.assertTrue(configHolder.hasProperties());
    }

    @Test
    public void testConfigHolder_HasAccessTokenProperty(){
        Assert.assertTrue(configHolder.hasProperty("accessToken"));
    }
  
    @Test
    public void testConfigHolder_AccessTokenIsCorrect(){
        String expected = DEFAULT_ACCESS_TOKEN;
        String actual = configHolder.getProperty("accessToken").toString();
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testConfigHolder_HasOutputDirProperty(){
        Assert.assertTrue(configHolder.hasProperty("outputDir"));
    }
    
    @Test
    public void testConfigHolder_OutputDirPathIsCorrect(){
        String expected = DEFAULT_BACKUP_FOLDER;
        String actual = configHolder.getProperty("outputDir").toString();
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testConfigHolder_HasContinueOnErrorProperty(){
        Assert.assertTrue(configHolder.hasProperty("continueOnError"));
    }
  
    @Test
    public void testConfigHolder_ContinueOnErrorIsTrue(){
        boolean expected = DEFAULT_CONTINUE_ON_ERROR;
        Assert.assertEquals(expected, Boolean.parseBoolean(configHolder.getProperty("continueOnError").toString()));
    }
  
    @Test
    public void testConfigHolder_HasZipOutputDirProperty(){
      Assert.assertTrue(configHolder.hasProperty("zipOutputDir"));
    }
  
    @Test
    public void testConfigHolder_ZipOutputDirPathIsCorrect(){
        String expected = DEFAULT_ZIP_OUTPUT_FOLDER;
        String actual = configHolder.getProperty("zipOutputDir").toString();
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testConfigHolder_HasDownloadThreadsProperty(){
        Assert.assertTrue(configHolder.hasProperty("downloadThreads"));
    }
    
    @Test
    public void testConfigHolder_DownloadThreadIsCorrect(){
        int expected = DEFAULT_NUM_DOWNLOAD_THREADS;
        int actual = Integer.parseInt(configHolder.getProperty("downloadThreads").toString());
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testConfigHolder_HasAllDownloadTimeoutProperty(){
        Assert.assertTrue(configHolder.hasProperty("allDownloadsDoneTimeout"));
    }
  
    @Test
    public void testConfigHolder_AllDownloadTimeoutIsCorrect(){
        int expected = DEFAULT_ALL_DOWNLOADS_TIMEOUT;
        int actual = Integer.parseInt(configHolder.getProperty("allDownloadsDoneTimeout").toString());
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testConfigHolder_NonExistentProperty_ExpectNotFound(){
        Assert.assertTrue(!configHolder.hasProperty("unknownProperty"));
    }
  
    @Test
    public void testConfigHolder_TryGetNonExistentProperty_ExpectNull(){
        Assert.assertNull(configHolder.getProperty("unknownProperty"));
    }
    
    @Test
    public void testConfigHolder_CommentOutProperty_ExpectNotFound(){
        Assert.assertTrue(!configHolder.hasProperty("commentOutProperty"));
    }
    
    @Test
    public void testConfigHolder_TryGetCommentOutProperty_ExpectNull(){
        Assert.assertNull(configHolder.getProperty("commentOutProperty"));
    }
}

package com.smartsheet.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for FileUtils.java
 * @author isim
 *
 */
public class FileUtils_FileExistTest {

    private final static String TEST_FILE_PREFIX = "smartsheet-test-";
    private final static String TEST_FILE_SUFFIX = ".tmp";
    
    private File systemTempFolder;
    private List<File> testFiles;
  
    @Before
    public void setUp(){
        systemTempFolder = new File(System.getProperty("java.io.tmpdir"));
        if(!systemTempFolder.exists())
          systemTempFolder.mkdir();
        
        testFiles = new ArrayList<File>();
    }
  
    @After
    public void tearDown(){
        if(testFiles != null)
            for(File file : testFiles)
                file.delete();
    }
  
    @Test
    public void testFileExistsInFolder_EmptyFileName() {
        Assert.assertTrue(!FileUtils.fileNameExistsInFolder("", systemTempFolder));
    }
    
    @Test(expected=NullPointerException.class)
    public void testFileExistsInFolder_NullFileName() {
        FileUtils.fileNameExistsInFolder(null, systemTempFolder);
        Assert.fail("Should have failed with a NullPointerException.");
    }
  
    @Test
    public void testFileExistsInFolder_AddOneFileToTempFolder() {
        try {
            createUniqueFileInSystemTempFolder();
            Assert.assertTrue(FileUtils.fileNameExistsInFolder(testFiles.get(0).getName(), systemTempFolder));
        } catch(IOException e){
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
  
    @Test
    public void testFileExistsInFolder_AddFourFilesToTempFolder() {
        try {
            createUniqueFileInSystemTempFolder();
            createUniqueFileInSystemTempFolder();
            createUniqueFileInSystemTempFolder();
            createUniqueFileInSystemTempFolder();
            Assert.assertTrue(FileUtils.fileNameExistsInFolder(testFiles.get(0).getName(), systemTempFolder));
        } catch(IOException e){
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
  
    @Test
    public void testFileExistsInFolder_NewFolderToTempFolder() {
        try {
            createNewFolderInSystemTempFolder();
            Assert.assertTrue(FileUtils.fileNameExistsInFolder(testFiles.get(0).getName(), systemTempFolder));
        } catch(IOException e){
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testFileExistsInFolder_NoSuchFileInTempFolder() {
        File noSuchFile = new File(Long.toString(new Date().getTime()));
        Assert.assertTrue(!FileUtils.fileNameExistsInFolder(noSuchFile.getName(), systemTempFolder));
    }
    
    private void createUniqueFileInSystemTempFolder() throws IOException{
        File tempFile = File.createTempFile(TEST_FILE_PREFIX, TEST_FILE_SUFFIX);
        testFiles.add(tempFile);
    }
    
    private void createNewFolderInSystemTempFolder() throws IOException{
        File tempFile = File.createTempFile(TEST_FILE_PREFIX, TEST_FILE_SUFFIX);
        tempFile.mkdir();
        testFiles.add(tempFile);
    }
}

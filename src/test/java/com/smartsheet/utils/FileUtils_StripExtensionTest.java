package com.smartsheet.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for FileUtils.stripExtension().
 * @author isim
 *
 */
public class FileUtils_StripExtensionTest {

    @Test
    public void testStripExtension_AlphabetOnlyFilename() {
        String filename = "tempfile.txt";
        String expected = "tempfile";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testStripExtension_NumericFilename() {
        String filename = "20131210.txt";
        String expected = "20131210";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testStripFileExtension_AlphaNumericFilename() {
        String filename = "tempfile12345.txt";
        String expected = "tempfile12345";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testStripFileExtension_PeriodInFilename() {
        String filename = "tempfile.org.txt";
        String expected = "tempfile.org";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testStripFileExtension_MultiplePeriodsInFilename() {
        String filename = "temp.file.org.txt";
        String expected = "temp.file.org";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testStripFileExtension_SpecialCharactersInFilename() {
        String filename = "temp_file-$%^&.txt";
        String expected = "temp_file-$%^&";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testStripFileExtension_NoExtension() {
        String filename = "tempfile";
        String expected = "tempfile";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
  
    @Test
    public void testStripFileExtension_EmptyStringInput() {
        String filename = "";
        String expected = "";
        String actual = FileUtils.stripExtension(filename);
        Assert.assertEquals(expected, actual);
    }
    
    @Test(expected=NullPointerException.class)
    public void testStripFileExtension_NullInput() {
        FileUtils.stripExtension(null);
        Assert.fail("Should have failed with a NullPointerException.");
    }
}

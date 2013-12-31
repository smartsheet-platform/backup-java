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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.smartsheet.exceptions.DeleteFileSystemItemException;

/**
 * Utilities for file operations.
 */
public class FileUtils {

    private static final int ZIP_BUFFER_SIZE = 64*1024; // 64K

    private FileUtils() {
        // private constructor because this is a singleton helper class, not intended to be instantiated
    }

    /**
     * Deletes a folder and all its contents.
     */
    public static void deleteFolder(File folder) throws DeleteFileSystemItemException {
        String[] entries = folder.list();
        if (entries == null)
            return;

        for (String entryName : entries) {
            File entry = new File(folder, entryName);
            if (entry.isDirectory())
                deleteFolder(entry);
            else
                delete(entry);
        }

        delete(folder);
    }

    private static void delete(File fileSystemItem) throws DeleteFileSystemItemException {
        if (!fileSystemItem.delete())
            throw new DeleteFileSystemItemException(fileSystemItem);
    }

    /**
     * Checks if the specified file name exists (is already used) in a folder.
     */
    public static boolean fileNameExistsInFolder(String fileName, File folder) {
        if(fileName.equals(""))
           return false;
        File file = new File(folder, fileName);
        return file.exists();
    }

    public static boolean folderNameExistsInParentFolder(String folderName, File parentFolder) {
        return fileNameExistsInFolder(folderName, parentFolder); // alias
    }

    /**
     * Returns only the name part of a file name, stripping its extension, if any.
     */
    public static String stripExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1)
            return fileName.substring(0, lastDotIndex);

        return fileName;
    }

    /**
     * Zips a directory to a specified file.
     *
     * @param directory
     *          The directory to zip.
     *
     * @param zipFile
     *          The file to zip to (created if it doesn't exist; otherwise
     *          overwritten). The file is assumed to have the appropriate
     *          extension (e.g., ".zip").
     *
     * @throws IOException
     */
    public static void zipDirectory(File directory, File zipFile) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        try {
            zipDirectory(directory /*root*/, directory, zos);
        } finally {
            zos.close();
        }
    }

    private static void zipDirectory(File root, File directory, ZipOutputStream zos) throws IOException {
        for (File item : directory.listFiles()) {
            if (item.isDirectory())
                zipDirectory(root, item, zos);

            else {
                byte[] readBuffer = new byte[ZIP_BUFFER_SIZE];
                InputStream fis = new FileInputStream(item);
                try {
                    String path = item.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
                    ZipEntry zipEntry = new ZipEntry(path);
                    zos.putNextEntry(zipEntry);
                    int bytesRead;
                    while ((bytesRead = fis.read(readBuffer)) != -1) {
                        zos.write(readBuffer, 0, bytesRead);
                    }
                } finally {
                    fis.close();
                }
            }
        }
    }
}

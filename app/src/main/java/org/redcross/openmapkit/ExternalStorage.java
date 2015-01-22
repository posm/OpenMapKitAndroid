package org.redcross.openmapkit;

import android.os.Environment;

import java.io.File;

/**
 * For encapsulating tasks such as checking if external storage is available for read, for write, and fetching files from storage
 */
public class ExternalStorage {

    /**
     * For checking if external storage is available for read and write
     */
    public static boolean isWritable() {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            return true;
        }

        return false;
    }

    /**
     *  For checking if external storage is available to at least read
     */
    public static boolean isReadable() {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {

            return true;
        }

        return false;
    }

    /**
     * For fetching a file from external storage
     * @param fileNameAndPath - file path + file name
     * @return File
     */
    public static File fetchFileFromExternalStorage(String fileNameAndPath) {

        File targetFile = null;

        try {

            targetFile = new File(fileNameAndPath);

        } catch(Exception e){

            e.printStackTrace();
        }

        return targetFile;
    }

    /**
     * For creating the application directory structure for Open Mapkit
     */
    public static void createAppFolders() {

        String appFolderName = "openmapkit";
        String mbtilesFolderName = "mbtiles";

        File appFolder = new File(Environment.getExternalStorageDirectory(), appFolderName);

        if(!appFolder.exists()) {

            appFolder.mkdirs();
        }

        File mbtilesSubfolder = new File(Environment.getExternalStorageDirectory() + "/" + appFolderName, mbtilesFolderName);

        if(!mbtilesSubfolder.exists()) {

            mbtilesSubfolder.mkdirs();
        }
    }
}


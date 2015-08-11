package org.redcross.openmapkit;

import android.os.Environment;

import java.io.File;

/**
 * For encapsulating tasks such as checking if external storage is available for read, for write, and fetching files from storage
 */
public class ExternalStorage {

    /**
     * Directories used by the app.
     * * * 
     */
    public static final String APP_DIR = "openmapkit";
    public static final String MBTILES_DIR = "mbtiles";
    public static final String OSM_DIR = "osm";
    public static final String SETTINGS_DIR = "settings";

    /**
     * Creating the application directory structure.
     */
    public static void checkOrCreateAppDirs() {
        File externalDir = Environment.getExternalStorageDirectory();
        File appDir = new File(externalDir, APP_DIR);
        if(!appDir.exists()) {
            appDir.mkdirs();
        }
        File mbtilesDir = new File(appDir, MBTILES_DIR);
        if(!mbtilesDir.exists()) {
            mbtilesDir.mkdirs();
        }
        File osmDir = new File(appDir, OSM_DIR);
        if (!osmDir.exists()) {
            osmDir.mkdirs();
        }
        File settingsDir = new File(appDir, SETTINGS_DIR);
        if (!settingsDir.exists()) {
            settingsDir.mkdirs();
        }
    }

    public static String getMBTilesDir() {
        return Environment.getExternalStorageDirectory() + "/"
                + APP_DIR + "/"
                + MBTILES_DIR + "/";
    }
    
    public static String getOSMDir() {
        return Environment.getExternalStorageDirectory() + "/"
                + APP_DIR + "/"
                + OSM_DIR + "/";
    }

    public static String getSettingsDir() {
        return Environment.getExternalStorageDirectory() + "/"
                + APP_DIR + "/"
                + SETTINGS_DIR + "/";
    }
    
    public static String getOSMDirRelativeToExternalDir() {
        return "/" + APP_DIR + "/" + OSM_DIR + "/";
    }
    
    public static File[] fetchOSMXmlFiles() {
        String dirPath = getOSMDir();
        File dir = new File(dirPath);
        return dir.listFiles();
    }
    
    public static String[] fetchOSMXmlFileNames() {
        File[] files = fetchOSMXmlFiles();
        int len = files.length;
        String [] names = new String[len];
        for (int i=0; i < len; ++i) {
            names[i] = files[i].getName();
        }
        return names;
    }
    
    public static File[] fetchMBTilesFiles() {
        String dirPath = getMBTilesDir();
        File dir = new File(dirPath);
        return dir.listFiles();
    }

    public static File[] fetchSettingsFiles() {
        String dirPath = getSettingsDir();
        File dir = new File(dirPath);
        return dir.listFiles();
    }
    
    /**
     * Checking if external storage is available for read and write
     */
    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     *  Checking if external storage is available to read.
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
    
}


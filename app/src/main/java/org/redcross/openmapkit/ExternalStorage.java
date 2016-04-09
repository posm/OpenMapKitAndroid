package org.redcross.openmapkit;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

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

    /**
     * Creating the application directory structure.
     */
    public static void checkOrCreateAppDirs() {

        // NOTE: Unable to create directories in SD Card dir. Investigate further. #135
        //  File storageDir = getSDCardDirWithExternalFallback();

        File storageDir = Environment.getExternalStorageDirectory();
        File appDir = new File(storageDir, APP_DIR);
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
    }

    public static String getMBTilesDir() {
        return Environment.getExternalStorageDirectory() + "/"
                + APP_DIR + "/"
                + MBTILES_DIR + "/";
    }

    public static String getMBTilesDirRelativeToExternalDir() {
        return "/" + APP_DIR + "/" + MBTILES_DIR + "/";
    }

    public static String getOSMDir() {
        return Environment.getExternalStorageDirectory() + "/"
                + APP_DIR + "/"
                + OSM_DIR + "/";
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

    /**
     * From
     * https://github.com/mstorsjo/vlc-android/blob/master/vlc-android/src/org/videolan/vlc/util/AndroidDevices.java
     *
     * @return storage directories, including external SD card
     */
    public static String[] getStorageDirectories() {
        String[] dirs = null;
        BufferedReader bufReader = null;
        ArrayList<String> list = new ArrayList<String>();
        list.add(Environment.getExternalStorageDirectory().getPath());

        List<String> typeWL = Arrays.asList("vfat", "exfat", "sdcardfs", "fuse");
        List<String> typeBL = Arrays.asList("tmpfs");
        String[] mountWL = { "/mnt", "/Removable" };
        String[] mountBL = {
                "/mnt/secure",
                "/mnt/shell",
                "/mnt/asec",
                "/mnt/obb",
                "/mnt/media_rw/extSdCard",
                "/mnt/media_rw/sdcard",
                "/storage/emulated" };
        String[] deviceWL = {
                "/dev/block/vold",
                "/dev/fuse",
                "/mnt/media_rw/extSdCard" };

        try {
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while((line = bufReader.readLine()) != null) {

                StringTokenizer tokens = new StringTokenizer(line, " ");
                String device = tokens.nextToken();
                String mountpoint = tokens.nextToken();
                String type = tokens.nextToken();

                // skip if already in list or if type/mountpoint is blacklisted
                if (list.contains(mountpoint) || typeBL.contains(type) || Strings.StartsWith(mountBL, mountpoint))
                    continue;

                // check that device is in whitelist, and either type or mountpoint is in a whitelist
                if (Strings.StartsWith(deviceWL, device) && (typeWL.contains(type) || Strings.StartsWith(mountWL, mountpoint)))
                    list.add(mountpoint);
            }

            dirs = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                dirs[i] = list.get(i);
            }
        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                }
                catch (IOException e) {}
            }
        }
        return dirs;
    }

    /**
     * Use last storage dir, which is ext SD card. If there is no SD card,
     * the last in the list will be the standard ext storage.
     *
     * @return storage dir
     */
    public static File getSDCardDirWithExternalFallback() {
        String[] dirs = getStorageDirectories();
        return new File(dirs[dirs.length-1]);
    }
}


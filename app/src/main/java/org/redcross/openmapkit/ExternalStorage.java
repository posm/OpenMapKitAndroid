package org.redcross.openmapkit;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.google.common.io.Files;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public static final String DEPLOYMENTS_DIR = "deployments";
    public static final String CONSTRAINTS_DIR = "constraints";
    public static final String DEFAULT_CONSTRAINT = "default.json";

    /**
     * The name of the form specific constraints file if it were to be delivered as an ODK media file
     */
    public static final String CONSTRAINTS_FILE_NAME_ON_ODK = "omk-constraints.json";
    public static final String TAG = "apple";

    /**
     * Creating the application directory structure.
     */
    public static void checkOrCreateAppDirs() {

        // NOTE: Unable to create directories in SD Card dir. Investigate further. #135
        //  File storageDir = getSDCardDirWithExternalFallback();

        File storageDir = Environment.getExternalStorageDirectory();
        File appDir = new File(storageDir, APP_DIR);
        if (!appDir.exists()) {
            appDir.mkdirs(); // mkdirs is mkdir -p
        }
        File mbtilesDir = new File(appDir, MBTILES_DIR);
        if (!mbtilesDir.exists()) {
            mbtilesDir.mkdirs();
        }
        File osmDir = new File(appDir, OSM_DIR);
        if (!osmDir.exists()) {
            osmDir.mkdirs();
        }
        File deploymentsDir = new File(appDir, DEPLOYMENTS_DIR);
        if (!deploymentsDir.exists()) {
            deploymentsDir.mkdirs();
        }
        File constraintsDir = new File(appDir, CONSTRAINTS_DIR);
        if (!constraintsDir.exists()) {
            constraintsDir.mkdirs();
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
        List<File> osms = allDeploymentOSMXmlFiles();
        String dirPath = getOSMDir();
        File dir = new File(dirPath);
        File[] otherOsms = dir.listFiles();
        Collections.addAll(osms, otherOsms);
        return osms.toArray(new File[osms.size()]);
    }

    public static String[] fetchOSMXmlFileNames() {
        File[] files = fetchOSMXmlFiles();
        int len = files.length;
        String[] names = new String[len];
        for (int i = 0; i < len; ++i) {
            names[i] = files[i].getName();
        }
        return names;
    }

    public static File[] fetchMBTilesFiles() {
        List<File> mbtiles = allDeploymentMBTilesFiles();
        String dirPath = getMBTilesDir();
        File dir = new File(dirPath);
        File[] otherMBTiles = dir.listFiles();
        if (otherMBTiles != null)
            Collections.addAll(mbtiles, otherMBTiles);
        return mbtiles.toArray(new File[mbtiles.size()]);
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
     * Checking if external storage is available to read.
     */
    public static boolean isReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static File deploymentDir(String deploymentName) {
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentsDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR);
        File deploymentDir = new File(deploymentsDir, deploymentName);
        if (!deploymentDir.exists()) {
            deploymentDir.mkdirs();
        }
        return deploymentDir;
    }

    public static String deploymentDirRelativeToExternalDir(String deploymentName) {
        // make sure deployment dir is created
        deploymentDir(deploymentName);
        return "/" + APP_DIR + "/" + DEPLOYMENTS_DIR + "/" + deploymentName + "/";
    }

    /**
     * Fetches all deployment.json files in ExternalStorage
     *
     * @return - list of deployment.json files
     */
    public static List<File> allDeploymentJSONFiles() {
        List<File> jsonFiles = new ArrayList<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentsDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR);
        File[] deployments = deploymentsDir.listFiles();
        for (File deploymentDir : deployments) {
            File[] files = deploymentDir.listFiles();
            for (File f : files) {
                String fileName = f.getName();
                if (fileName.equals("deployment.json")) {
                    jsonFiles.add(f);
                }
            }
        }
        return jsonFiles;
    }

    public static List<File> allDeploymentOSMXmlFiles() {
        List<File> deploymentOSMFiles = new ArrayList<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentsDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR);
        File[] deployments = deploymentsDir.listFiles();
        for (File deploymentDir : deployments) {
            File[] files = deploymentDir.listFiles();
            for (File f : files) {
                String ext = FilenameUtils.getExtension(f.getPath());
                if (ext.equals("osm")) {
                    deploymentOSMFiles.add(f);
                }
            }
        }
        return deploymentOSMFiles;
    }

    public static List<File> allDeploymentMBTilesFiles() {
        List<File> deploymentMBTilesFiles = new ArrayList<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentsDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR);
        File[] deployments = deploymentsDir.listFiles();
        if (deployments != null)
            for (File deploymentDir : deployments) {
                File[] files = deploymentDir.listFiles();
                for (File f : files) {
                    String ext = FilenameUtils.getExtension(f.getPath());
                    if (ext.equals("mbtiles")) {
                        deploymentMBTilesFiles.add(f);
                    }
                }
            }
        return deploymentMBTilesFiles;
    }

    public static Set<File> deploymentOSMXmlFiles(String deploymentName) {
        Set<File> osmXmlFiles = new HashSet<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR + "/" + deploymentName);
        File[] files = deploymentDir.listFiles();
        for (File f : files) {
            String ext = FilenameUtils.getExtension(f.getPath());
            if (ext.equals("osm")) {
                osmXmlFiles.add(f);
            }
        }
        return osmXmlFiles;
    }

    public static File deploymentFPFile(String deploymentName) {
        Set<File> osmXmlFiles = new HashSet<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR + "/" + deploymentName);
        File[] files = deploymentDir.listFiles();
        for (File f : files) {
            if (f.getName().equals("fp.geojson")) {
                return f;
            }
        }
        return null;
    }

    public static Set<File> deploymentMBTilesFiles(String deploymentName) {
        Set<File> mbtilesFiles = new HashSet<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR + "/" + deploymentName);
        File[] files = deploymentDir.listFiles();
        for (File f : files) {
            String ext = FilenameUtils.getExtension(f.getPath());
            if (ext.equals("mbtiles")) {
                mbtilesFiles.add(f);
            }
        }
        return mbtilesFiles;
    }

    public static Map<String, File> deploymentDownloadedFiles(String deploymentName) {
        Map<String, File> deploymentFiles = new HashMap<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR + "/" + deploymentName);
        File[] files = deploymentDir.listFiles();
        if (files == null || files.length == 0) return deploymentFiles;
        for (File f : files) {
            String ext = FilenameUtils.getExtension(f.getPath());
            if (ext.equals("mbtiles") || ext.equals("osm") || ext.equals("geojson")) {
                deploymentFiles.put(f.getName(), f);
            }
        }
        return deploymentFiles;
    }

    public static List<String> deploymentMBTilesFilePaths(String deploymentName) {
        List<String> mbtilesFiles = new ArrayList<>();
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR + "/" + deploymentName);
        File[] files = deploymentDir.listFiles();
        for (File f : files) {
            String ext = FilenameUtils.getExtension(f.getPath());
            if (ext.equals("mbtiles")) {
                mbtilesFiles.add(f.getAbsolutePath());
            }
        }
        return mbtilesFiles;
    }

    public static void deleteDeployment(String deploymentName) {
        File storageDir = Environment.getExternalStorageDirectory();
        File deploymentsDir = new File(storageDir, APP_DIR + "/" + DEPLOYMENTS_DIR);
        File deploymentDir = new File(deploymentsDir, deploymentName);
        deleteRecursive(deploymentDir);
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static void copyConstraintsToExternalStorageIfNeeded(Context context) {
        File storageDir = Environment.getExternalStorageDirectory();
        File appDir = new File(storageDir, APP_DIR);
        File constraintsDir = new File(appDir, CONSTRAINTS_DIR);
        File defaultConstraint = new File(constraintsDir, DEFAULT_CONSTRAINT);
        // We want to always copy over JSON while developing.
        // In production, we only want to copy over once.
        if (!defaultConstraint.exists() || BuildConfig.DEBUG) {
            copyAssetsFileOrDirToExternalStorage(context, CONSTRAINTS_DIR);
        }
    }

    private static void copyAssetsFileOrDirToExternalStorage(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyAssetsFileToExternalStorage(context, path);
            } else {
                String fullPath = Environment.getExternalStorageDirectory() + "/" + APP_DIR + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyAssetsFileOrDirToExternalStorage(context, path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private static void copyAssetsFileToExternalStorage(Context context, String filename) {
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = Environment.getExternalStorageDirectory() + "/" + APP_DIR + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public static File fetchConstraintsFile(String formName) {
        File storageDir = Environment.getExternalStorageDirectory();
        File appDir = new File(storageDir, APP_DIR);
        File constraintsDir = new File(appDir, CONSTRAINTS_DIR);
        return new File(constraintsDir, formName + ".json");
    }

    public static File fetchConstraintsFileFromODKNew(String formName) {
        File storageDir = Environment.getExternalStorageDirectory();
        File appDir = new File(storageDir, "Android");
        File appDir1 = new File(appDir, "data");
        File constraintsDir = new File(appDir1, "org.odk.collect.android");
        File appDir2 = new File(constraintsDir, constraintsDir.list()[0]);
        File appDir3 = new File(appDir2, appDir2.list()[0]);
        File appDir4 = new File(appDir3, appDir3.list()[0]);
        File appDir5 = new File(appDir4, "forms");
        File appDir6 = new File(appDir5, formName + "-media");
        return new File(appDir6, appDir6.list()[0]);
    }

    /**
     * This method attempts to fetch the form's constraints file from ODK's media directory for the
     * form. If the constraints file is found on ODK's media directory for the form, its contents
     * will overwrite whatever is in OMK's constraints file for the form
     *
     * @param formFileName The name of the ODK form
     * @return TRUE if there was a successful copy
     */
    public static boolean copyFormConstraintsFromOdk(String formFileName) {
        String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (formFileName != null) {
            //new method to support moving constraints from odk inside Android/Data/org.odk.collect.android for Android 10 and below devices
            //this doesn't apply to android 11 due to folder not made by odk app itself
            try {
                File formConstraintsFileTesting = ExternalStorage.fetchConstraintsFileFromODKNew(formFileName);
                File omkConstraintsFile = fetchConstraintsFile(formFileName);
                Files.copy(formConstraintsFileTesting, omkConstraintsFile);
                return true;
            } catch (Exception e) {
            }
            String mediaDirPath = sdCardPath + "/odk/forms/" + formFileName + "-media";
            File mediaDirectory = new File(mediaDirPath);
            if (mediaDirectory.exists() && mediaDirectory.isDirectory()) {
                String constraintsFilePath = mediaDirPath + "/" + CONSTRAINTS_FILE_NAME_ON_ODK;
                File odkConstraintsFile = new File(constraintsFilePath);
                if (odkConstraintsFile.exists() && !odkConstraintsFile.isDirectory()) {
                    File omkConstraintsFile = fetchConstraintsFile(formFileName);
                    try {
                        Files.copy(odkConstraintsFile, omkConstraintsFile);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
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
        String[] mountWL = {"/mnt", "/Removable"};
        String[] mountBL = {
                "/mnt/secure",
                "/mnt/shell",
                "/mnt/asec",
                "/mnt/obb",
                "/mnt/media_rw/extSdCard",
                "/mnt/media_rw/sdcard",
                "/storage/emulated"};
        String[] deviceWL = {
                "/dev/block/vold",
                "/dev/fuse",
                "/mnt/media_rw/extSdCard"};

        try {
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while ((line = bufReader.readLine()) != null) {

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
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                }
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
        return new File(dirs[dirs.length - 1]);
    }
}


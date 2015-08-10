package com.mapbox.mapboxsdk.customsettings;

import android.os.Environment;

import java.io.File;

/**
 * Created by coder on 8/6/15.
 */
public class Settings {
    public static final String APP_DIR = "openmapkit";
    public static final String SETTINGS_DIR = "settings";

    /**
     *
     * @return the path to the settings directorys.
     */
    public static String getSettingsDir() {
        createSettingsDir();
        return Environment.getExternalStorageDirectory() + "/"
                + APP_DIR + "/"
                + SETTINGS_DIR + "/";
    }

    /**
     * Create settings dir if it does not exist.
     * @return true if successfully created.
     */
    public static boolean createSettingsDir() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/"
                + APP_DIR + "/"
                + SETTINGS_DIR);
        boolean createStatus = true;
        if (!folder.exists()) {
            createStatus = folder.mkdirs() ? true : false;
        }
        return createStatus;
    }
}

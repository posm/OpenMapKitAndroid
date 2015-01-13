package com.mapbox.mapboxsdk.util.constants;

public class UtilConstants {

    /**
     * The time we wait after the last gps location before using a non-gps location.
     */
    public static final long GPS_WAIT_TIME = 20000; // 20 seconds
    public static boolean DEBUGMODE = false;
    public static void setDebugMode(final boolean value) {
        DEBUGMODE = value;
    }
}

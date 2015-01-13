package com.mapbox.mapboxsdk.util;

import android.os.Looper;

public class AppUtils {

    public static boolean runningOnMainThread() {
        return  Looper.myLooper() == Looper.getMainLooper();
    }
}

package com.mapbox.mapboxsdk.util;

import android.content.Context;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

public class AppUtils {

    private static final String TAG = "AppUtils";

    public static boolean runningOnMainThread() {
        return  Looper.myLooper() == Looper.getMainLooper();
    }

    public static boolean isRunningOn2xOrGreaterScreen(Context context) {
        if (context == null) {
            return false;
        }
        int density = context.getResources().getDisplayMetrics().densityDpi;
        boolean result = density >= DisplayMetrics.DENSITY_HIGH;
        Log.d(TAG, String.format("Device density is %d, and result of @2x check is %b", density, result));
        return result;
    }
}

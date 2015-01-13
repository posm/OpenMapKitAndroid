/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 3/9/14 at 2:50 PM
 */

package com.mapbox.mapboxsdk.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    public static final int[] EXPIRED = new int[] { -1 };

    public static BitmapFactory.Options getBitmapOptions(DisplayMetrics mDisplayMetrics) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDensity = DisplayMetrics.DENSITY_DEFAULT;
        options.inTargetDensity = mDisplayMetrics.densityDpi;
        return options;
    }

    public static boolean isCacheDrawableExpired(Drawable drawable) {
        if (drawable != null && drawable.getState() == EXPIRED) {
            return true;
        }
        return false;
    }

    public static void setCacheDrawableExpired(CacheableBitmapDrawable drawable) {
        if (drawable != null) {
            drawable.setState(EXPIRED);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class ActivityManagerHoneycomb {
        static int getLargeMemoryClass(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }

    public static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean largeHeap =
                (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
        }
        Log.d(TAG, "LargeHeap enabled? = '" + largeHeap + "'");
        // Target ~15% of the available heap.
        int heapRes = 1024 * 1024 * memoryClass / 7;
        Log.d(TAG, "Heap Reserve Request For Cache Size = '" + heapRes + "'");
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        Log.d(TAG, "Available Memory = '" + memoryInfo.availMem + "'");
        return heapRes;
    }
}

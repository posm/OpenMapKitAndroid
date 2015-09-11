package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import com.mapbox.mapboxsdk.util.MapboxUtils;
import com.mapbox.mapboxsdk.util.NetworkUtils;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * An Icon provided by the Mapbox marker API, optionally
 * with a symbol from Maki
 */
public class Icon implements MapboxConstants {

    private static final String TAG = "Icon";

    private Marker marker;
    private Drawable drawable;
    private Context context;

    protected static BitmapLruCache sIconCache;
    private static final String DISK_CACHE_SUBDIR = "mapbox_icon_cache";

    // Well, we only want to download the same URL once. If we request the same url rapidly
    // We place it in this queue..
    private static ConcurrentHashMap<String, ArrayList<Icon>> downloadQueue =
            new ConcurrentHashMap<String, ArrayList<Icon>>();

    public enum Size {
        LARGE("l"), MEDIUM("m"), SMALL("s");

        private String apiString;

        Size(String api) {
            this.apiString = api;
        }

        public String getApiString() {
            return apiString;
        }
    }

    protected BitmapLruCache getCache() {
        return getCache(null);
    }

    // TODO: This is common code from MapTileCache, ideally this would be extracted
    // and used by both classes.
    protected BitmapLruCache getCache(Context context) {
        if (sIconCache == null && context != null) {
            File cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR);
            if (!cacheDir.exists()) {
                if (cacheDir.mkdirs()) {
                    if (UtilConstants.DEBUGMODE) {
                        Log.d(TAG, "creating cacheDir " + cacheDir);
                    }
                } else {
                    Log.e(TAG, "can't create cacheDir " + cacheDir);
                }
            }
            sIconCache = (new BitmapLruCache.Builder(context)).setMemoryCacheEnabled(true)
                    .setMemoryCacheMaxSize(
                            BitmapUtils.calculateMemoryCacheSize(context)).setDiskCacheEnabled(true)
                            // 1 MB (a marker image is around 1kb)
                    .setDiskCacheMaxSize(1024 * 1024).build();
        }
        return sIconCache;
    }

    /**
     * Creates a unique subdirectory of the designated app cache directory. Tries to use external
     * but if not mounted, falls back on internal storage.
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                        || (!Environment.isExternalStorageRemovable())
                        ? Environment.getExternalStorageDirectory().getPath()
                        : context.getFilesDir().getPath();
        return new File(cachePath, uniqueName);
    }

    /**
     * Initialize an icon with size, symbol, and color, and start a
     * download process to load it from the API.
     * @param context Android context - Used for proper Bitmap Density generation
     * @param size    Size of Icon
     * @param symbol  Maki Symbol
     * @param aColor  Color of Icon
     */
    public Icon(Context context, Size size, String symbol, String aColor) {
        this.context = context;
        String url = MapboxUtils.markerIconURL(context, size.apiString, symbol, aColor);
        downloadBitmap(context, url);
    }

    /**
     * Initialize an Icon with a custom Drawable
     * @param drawable Custom Drawable
     */
    public Icon(Drawable drawable) {
        this.drawable = drawable;
    }

    /**
     * Set the marker that this icon belongs to, calling the same method on the other side
     *
     * @param aMarker the marker to be added to
     * @return this icon
     */
    public Icon setMarker(Marker aMarker) {
        this.marker = aMarker;
        if (drawable != null) {
            this.marker.setMarker(drawable, true);
        }
        return this;
    }

    private void downloadBitmap(Context context, String url) {
        CacheableBitmapDrawable bitmap = getCache(context).getFromMemoryCache(url);

        // Cache hit! We're done..
        if (bitmap != null) {
            drawable = bitmap;
            if (marker != null) {
                marker.setMarker(drawable, true);
            }
            return;
        }

        // Ok, we want to download a url only once. So if we have multiple requests coming in in
        // a short period of time we will batch them up..

        // The idea is simple. We have a map url->[list of icons wanting that image]
        // The first icon in the list will kick of a downloader on the background.
        // any incoming requests that resulted in a cache miss with the downloader active
        // will be added to the list.
        // Once the downloader finishes, it will notify every icon that the image is there.
        if (Icon.downloadQueue.putIfAbsent(url, new ArrayList<Icon>()) == null) {
            // We just placed a new list in the queue, so we will be responsible for
            // kicking off the downloader..
            ArrayList<Icon> list = Icon.downloadQueue.get(url);
            synchronized (list) {
                list.add(this);
                new BitmapLoader().execute(url);
            }
        } else {
            // Okay, another downloader for this url is active, and the bitmap is not
            // yet retrieved..
            ArrayList<Icon> list = Icon.downloadQueue.get(url);

            // Case 1:
            // The download thread just finished up, the list is now removed from the
            // hashmap
            if (list == null) {
                // Note, there is an extremely unlikely chance we are immediately kicked
                // out of the cache...
                drawable = sIconCache.get(url);
                if (marker != null) {
                    marker.setMarker(drawable, true);
                }
                return;
            }

            synchronized (list) {
                // Case 2:
                // The downloader thread just released the lock, the list is empty.
                // The cache has our icon..
                if (list.isEmpty()) {
                    drawable = sIconCache.get(url);
                    if (marker != null) {
                        marker.setMarker(drawable, true);
                    }
                    return;
                }

                // Case 3: The background thread is busy, or waiting to get the lock..
                // We can safely add ourselves to the list to be notified of the retrieved bitmap.
                list.add(this);
            }
        }
    }

    class BitmapLoader extends AsyncTask<String, Void, CacheableBitmapDrawable> {

        private String url;

        @Override
        protected CacheableBitmapDrawable doInBackground(String... src) {
            this.url = src[0];
            CacheableBitmapDrawable result = getCache().getFromDiskCache(this.url, null);
            if (result == null) {
                try {
                    if (UtilConstants.DEBUGMODE) {
                        Log.d(TAG, "Maki url to load = '" + this.url + "'");
                    }
                    HttpURLConnection connection = NetworkUtils.getHttpURLConnection(new URL(url));
                    // Note, sIconCache cannot be null..

                    BitmapFactory.Options opts = BitmapUtils.getBitmapOptions(context.getResources().getDisplayMetrics());
                    result = sIconCache.put(this.url, connection.getInputStream(), opts);
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Unable to fetch icon from: " + this.url);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(CacheableBitmapDrawable bitmap) {
            if (bitmap != null && marker != null) {
                ArrayList<Icon> list = Icon.downloadQueue.get(this.url);
                synchronized (list) {
                    for (Icon icon : list) {
                        if (icon.marker != null) {
                            icon.marker.setMarker(bitmap, true);
                        }
                    }
                    if (UtilConstants.DEBUGMODE) {
                        Log.d(TAG, "Loaded:" + this.url);
                    }
                    Icon.downloadQueue.remove(this.url);
                }
            }
        }
    }
}

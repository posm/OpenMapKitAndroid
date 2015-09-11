/**
 * @author Brad Leege <bleege@gmail.com>
 * Created on 2/15/14 at 3:26 PM
 */

package com.mapbox.mapboxsdk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static HttpURLConnection getHttpURLConnection(final URL url) {
        return getHttpURLConnection(url, null, null);
    }

    public static HttpURLConnection getHttpURLConnection(final URL url, final Cache cache) {
        return getHttpURLConnection(url, cache, null);
    }

    public static HttpURLConnection getHttpURLConnection(final URL url, final Cache cache, final SSLSocketFactory sslSocketFactory) {
        OkHttpClient client = new OkHttpClient();
        if (cache != null) {
            client.setCache(cache);
        }
        if (sslSocketFactory != null) {
            client.setSslSocketFactory(sslSocketFactory);
        }
        HttpURLConnection connection = new OkUrlFactory(client).open(url);
        connection.setRequestProperty("User-Agent", MapboxUtils.getUserAgent());
        return connection;
    }

    public static Cache getCache(final File cacheDir, final int maxSize) throws IOException {
        return new Cache(cacheDir, maxSize);
    }
}

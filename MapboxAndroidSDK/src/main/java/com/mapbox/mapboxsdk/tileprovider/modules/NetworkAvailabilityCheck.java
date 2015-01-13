package com.mapbox.mapboxsdk.tileprovider.modules;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A straightforward network check implementation. NOTE: Requires
 * android.permission.ACCESS_NETWORK_STATE and android.permission.ACCESS_WIFI_STATE (?) and
 * android.permission.INTERNET (?)
 *
 * @author Marc Kurtz
 */

public class NetworkAvailabilityCheck {

    private final ConnectivityManager mConnectionManager;

    public NetworkAvailabilityCheck(final Context aContext) {
        mConnectionManager =
                (ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean getNetworkAvailable() {
        final NetworkInfo networkInfo = mConnectionManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    public boolean getWiFiNetworkAvailable() {
        final NetworkInfo wifi = mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isAvailable();
    }

    public boolean getCellularDataNetworkAvailable() {
        final NetworkInfo mobile =
                mConnectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobile != null && mobile.isAvailable();
    }

    public boolean getRouteToPathExists(final int hostAddress) {
        return (mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_WIFI, hostAddress)
                || mConnectionManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE,
                hostAddress));
    }
}

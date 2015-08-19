package org.redcross.openmapkit.proximity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.mapbox.mapboxsdk.util.NetworkLocationIgnorer;

public class GpsProximityProvider implements LocationListener {

    private final LocationManager mLocationManager;
    private Location mLocation;

    private UserProximityOverlay mMyLocationConsumer;
    private long mLocationUpdateMinTime = 0;
    private float mLocationUpdateMinDistance = 0.0f;
    private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();

    public GpsProximityProvider(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public long getLocationUpdateMinTime() {
        return mLocationUpdateMinTime;
    }

    /**
     * Set the minimum interval for location updates.
     */
    public void setLocationUpdateMinTime(final long milliSeconds) {
        mLocationUpdateMinTime = milliSeconds;
    }

    public float getLocationUpdateMinDistance() {
        return mLocationUpdateMinDistance;
    }

    /**
     * Set the minimum distance for location updates.
     */
    public void setLocationUpdateMinDistance(final float meters) {
        mLocationUpdateMinDistance = meters;
    }

    /**
     * Enable location updates and show your current location on the map. By default this will
     * request location updates as frequently as possible, but you can change the frequency and/or
     * distance by calling setLocationUpdateMinTime(long) and/or
     * setLocationUpdateMinDistance(float) before calling this method.
     */
    public boolean startLocationProvider(UserProximityOverlay myLocationConsumer) {
        mMyLocationConsumer = myLocationConsumer;
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.PASSIVE_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                result = true;
                if (mLocation == null) {
                    mLocation = mLocationManager.getLastKnownLocation(provider);
                    if (mLocation != null) {
                        mMyLocationConsumer.onLocationChanged(mLocation, this);
                    }
                }
                mLocationManager.requestLocationUpdates(provider, mLocationUpdateMinTime,
                        mLocationUpdateMinDistance, this);
            }
        }
        return result;
    }

    public void stopLocationProvider() {
        mMyLocationConsumer = null;
        mLocationManager.removeUpdates(this);
    }

    public Location getLastKnownLocation() {
        return mLocation;
    }

    //
    // LocationListener
    //

    @Override
    public void onLocationChanged(final Location location) {
        // ignore temporary non-gps fix
        if (mIgnorer.shouldIgnore(location.getProvider(), System.currentTimeMillis())) {
            return;
        }

        mLocation = location;
        if (mMyLocationConsumer != null) {
            mMyLocationConsumer.onLocationChanged(mLocation, this);
        }
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
    }

    public float getAccuracy() {
        return mLocation.getAccuracy();
    }
}

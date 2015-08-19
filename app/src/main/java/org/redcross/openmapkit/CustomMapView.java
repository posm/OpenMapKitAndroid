package org.redcross.openmapkit;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.util.AttributeSet;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import org.redcross.openmapkit.proximity.GpsProximityProvider;
import org.redcross.openmapkit.proximity.UserProximityOverlay;
import org.redcross.openmapkit.settings.SettingsXmlParser;

/**
 * Created by coder on 8/12/15.
 */
public class CustomMapView extends MapView {
    private UserLocationOverlay mLocationOverlay;

    public CustomMapView(final Context aContext) {
        super(aContext, 256, null, null, null);
    }

    public CustomMapView(final Context aContext, AttributeSet attrs) {
        super(aContext, 256, null, null, attrs);
    }

    protected CustomMapView(Context aContext, int tileSizePixels, MapTileLayerBase aTileProvider) {
        super(aContext, tileSizePixels, aTileProvider, null, null);
    }

    private UserLocationOverlay getOrCreateLocationOverlay() {
        if (mLocationOverlay == null) {
            if (SettingsXmlParser.hasSettingsXmlFile()) {
                mLocationOverlay = new UserProximityOverlay(new GpsProximityProvider(getContext()), this);
            } else {
                mLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(getContext()), this);
            }
            addOverlay(mLocationOverlay);
        }
        return mLocationOverlay;
    }

    /**
     * Show the user location overlay
     */
    public MapView setUserLocationEnabled(final boolean value) {
        getOrCreateLocationOverlay().enableMyLocation();
        return this;
    }

    /**
     * Show or hide the user location overlay
     */
    public final boolean getUserProximityEnabled() {
        if (mLocationOverlay != null) {
            return mLocationOverlay.isMyLocationEnabled();
        }
        return false;
    }

    /**
     * Set the user location tracking mode
     */
    public MapView setUserLocationTrackingMode(final UserProximityOverlay.TrackingMode mode) {
        getOrCreateLocationOverlay().setTrackingMode(mode);
        return this;
    }

    /**
     * Set the user location tracking zoom level
     */
    public MapView setUserLocationRequiredZoom(final float zoomLevel) {
        getOrCreateLocationOverlay().setRequiredZoom(zoomLevel);
        return this;
    }

    /**
     * get the user location tracking mode
     */
    public UserLocationOverlay.TrackingMode getUserProximityTrackingMode() {
        if (mLocationOverlay != null) {
            return mLocationOverlay.getTrackingMode();
        }
        return UserLocationOverlay.TrackingMode.NONE;
    }

    /**
     * Go to user location
     */
    public void goToUserLocation(final boolean animated) {
        if (mLocationOverlay != null) {
            mLocationOverlay.goToMyPosition(animated);
        }
    }

    /**
     * Get the user location overlay if created
     */
    public UserLocationOverlay getUserProximityOverlay() {
        return mLocationOverlay;
    }

    /**
     * Get the user location overlay if created
     */
    public LatLng getUserLocation() {
        if (mLocationOverlay != null) {
            return mLocationOverlay.getMyLocation();
        }
        return null;
    }

    public boolean isUserLocationVisible() {
        if (mLocationOverlay != null) {
            final Location pos = mLocationOverlay.getLastFix();
            if (pos != null && isLayedOut()) {
                final Projection projection = getProjection();
                final float accuracyInPixels = pos.getAccuracy() / (float) projection.groundResolution(
                        pos.getLatitude());
                final PointF point = projection.toMapPixels(pos.getLatitude(), pos.getLongitude(), null);
                return projection.getScreenRect().intersects((int) (point.x - accuracyInPixels),
                        (int) (point.y - accuracyInPixels),
                        (int) (point.x + accuracyInPixels),
                        (int) (point.y + accuracyInPixels));
            }
        }
        return false;
    }
}

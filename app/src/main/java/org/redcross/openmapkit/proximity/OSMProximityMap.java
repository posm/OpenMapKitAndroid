package org.redcross.openmapkit.proximity;

import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by imwongela on 8/11/15.
 */
public class OSMProximityMap {
    private UserProximityOverlay userProximityOverlay;

    public OSMProximityMap(MapView mapView) {
        userProximityOverlay = new UserProximityOverlay(new GpsProximityProvider(mapView.getContext()), mapView);
        mapView.getOverlays().add(userProximityOverlay);
        mapView.invalidate();
    }
}

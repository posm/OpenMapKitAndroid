package org.redcross.openmapkit.proximity;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.JTSModel;

/**
 * Created by imwongela on 8/11/15.
 */
public class OSMProximityMap {
    private UserProximityOverlay osmColorOverlay;

    public OSMProximityMap(MapView mapView) {
        osmColorOverlay = new UserProximityOverlay(new GpsProximityProvider(mapView.getContext()), mapView);
        mapView.getOverlays().add(osmColorOverlay);
        mapView.invalidate();
    }
}

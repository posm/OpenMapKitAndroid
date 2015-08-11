package org.redcross.openmapkit.mapcoloring;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.JTSModel;

/**
 * Created by coder on 8/11/15.
 */
public class CustomColoredOSMMap {
    private MapView mapView;
    private OSMColorOverlay osmColorOverlay;

    public CustomColoredOSMMap(MapView mapView, JTSModel jtsModel) {
        this.mapView = mapView;
        osmColorOverlay = new OSMColorOverlay(jtsModel);
        updateBoundingBox();
        mapView.getOverlays().add(osmColorOverlay);
        mapView.invalidate();
    }

    private void updateBoundingBox() {
        BoundingBox bbox = mapView.getBoundingBox();
        if (bbox != null) {
            osmColorOverlay.updateBoundingBox(bbox);
        }
    }
}

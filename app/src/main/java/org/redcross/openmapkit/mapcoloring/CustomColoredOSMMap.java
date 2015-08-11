package org.redcross.openmapkit.mapcoloring;

import android.graphics.Paint;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.events.OSMSelectionListener;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.renderer.OSMOverlay;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Created by coder on 8/11/15.
 */
public class CustomColoredOSMMap {
    // DEBUG MODE - SHOW ENVELOPE AROUND TAP ON MAP
    private static final boolean DEBUG = true;

    private MapView mapView;
    private JTSModel jtsModel;
    private OSMColorOverlay osmOverlay;

    public CustomColoredOSMMap(MapView mapView, JTSModel jtsModel) {
        this.mapView = mapView;
        this.jtsModel = jtsModel;
        osmOverlay = new OSMColorOverlay(jtsModel);
        updateBoundingBox();
        mapView.getOverlays().add(osmOverlay);
        mapView.invalidate();
    }

    private void updateBoundingBox() {
        BoundingBox bbox = mapView.getBoundingBox();
        if (bbox != null) {
            osmOverlay.updateBoundingBox(bbox);
        }
    }
}

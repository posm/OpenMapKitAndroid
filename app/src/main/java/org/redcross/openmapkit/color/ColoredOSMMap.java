package org.redcross.openmapkit.color;

import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.renderer.OSMOverlay;

/**
 * Created by imwongela on 8/11/15.
 */
public class ColoredOSMMap implements MapListener {
    private MapView mapView;
    private OSMColorOverlay osmColorOverlay;

    public ColoredOSMMap(MapView mapView, JTSModel jtsModel, float minVectorRenderZoom) {
        if (minVectorRenderZoom > 0) {
            osmColorOverlay = new OSMColorOverlay(jtsModel, minVectorRenderZoom);
        } else {
            osmColorOverlay = new OSMColorOverlay(jtsModel);
        }
        this.mapView = mapView;
        updateBoundingBox();
        osmColorOverlay.updateZoom(mapView.getZoomLevel());
        mapView.getOverlays().add(osmColorOverlay);
        mapView.invalidate();
    }

    private void updateBoundingBox() {
        BoundingBox bbox = mapView.getBoundingBox();
        if (bbox != null) {
            osmColorOverlay.updateBoundingBox(bbox);
        }
    }

    /**
     * MapListener Methods
     */

    @Override
    public void onScroll(ScrollEvent event) {
        updateBoundingBox();
    }

    @Override
    public void onZoom(ZoomEvent event) {
        updateBoundingBox();
        osmColorOverlay.updateZoom(event.getZoomLevel());
//        Log.i("ZOOM", String.valueOf(event.getZoomLevel()));
    }

    @Override
    public void onRotate(RotateEvent event) {
        updateBoundingBox();
    }
}

/**
 * Created by Nicholas Hallahan on 1/7/15.
 * nhallahan@spatialdev.com
 */

package com.spatialdev.osm;

import android.graphics.Paint;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.model.OSMElement;
import com.vividsolutions.jts.geom.Envelope;

import java.util.List;

public class OSMMapListener implements MapViewListener, MapListener {

    // DEBUG MODE - SHOW ENVELOPE AROUND TAP ON MAP
    private static final boolean DEBUG = true;

    private MapView mapView;
    private JTSModel jtsModel;

    private PathOverlay debugTapEnvelopePath;

    public OSMMapListener(MapView mapView, JTSModel jtsModel) {
        this.mapView = mapView;
        this.jtsModel = jtsModel;

        mapView.setMapViewListener(this);
        mapView.addListener(this);
    }

    /**
     * MapViewListener Methods
     */

    @Override
    public void onShowMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onHideMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onTapMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onLongPressMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onTapMap(MapView pMapView, ILatLng pPosition) {
        float zoom = pMapView.getZoomLevel();

        OSMElement.deselectAll();

        OSMElement element = jtsModel.queryFromTap(pPosition, zoom);
        if (element != null) {
            element.select();
            PathOverlay path = (PathOverlay) element.getOverlay();
            List<Overlay> overlays = pMapView.getOverlays();
            overlays.add(path);
        }

        // DEBUG MODE - SHOW ENVELOPE AROUND TAP ON MAP
        if (DEBUG) {
            drawDebugTapEnvelope(pMapView, pPosition, zoom);
        }

        mapView.invalidate();
    }

    private void drawDebugTapEnvelope(MapView pMapView, ILatLng pPosition, float zoom) {
        Envelope env = jtsModel.createTapEnvelope(pPosition, zoom);
        PathOverlay path;
        if (debugTapEnvelopePath == null) {
            path = new PathOverlay();
            debugTapEnvelopePath = path;
            pMapView.getOverlays().add(path);
        } else {
            path = debugTapEnvelopePath;
        }
        Paint paint = path.getPaint();
        paint.setStrokeWidth(0); // hairline mode
        paint.setARGB(200, 0, 255, 255);
        double maxX = env.getMaxX();
        double maxY = env.getMaxY();
        double minX = env.getMinX();
        double minY = env.getMinY();
        path.clearPath();
        path.addPoint(minY, minX);
        path.addPoint(maxY, minX);
        path.addPoint(maxY, maxX);
        path.addPoint(minY, maxX);
        path.addPoint(minY, minX);
    }

    @Override
    public void onLongPressMap(MapView pMapView, ILatLng pPosition) {
//        float zoom = pMapView.getZoomLevel();
//        jtsModel.queryFromTap(pPosition, zoom);
    }

    /**
     * MapListener Methods
     */

    @Override
    public void onScroll(ScrollEvent event) {

    }

    @Override
    public void onZoom(ZoomEvent event) {

    }

    @Override
    public void onRotate(RotateEvent event) {

    }
}

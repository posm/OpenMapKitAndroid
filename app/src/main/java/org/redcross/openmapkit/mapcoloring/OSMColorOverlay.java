package org.redcross.openmapkit.mapcoloring;

import android.graphics.Canvas;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMNode;
import com.spatialdev.osm.model.OSMWay;
import com.vividsolutions.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by coder on 8/11/15.
 */
public class OSMColorOverlay extends Overlay {

    private static final int DEFAULT_OVERLAY_INDEX = 4;

    private JTSModel model;
    private Envelope envelope;

    private float minVectorRenderZoom = 0;
    private float zoom = 0; // current zoom of map

    /**
     * This should only be created by OSMMap.
     * * *
     * @param model
     */
    public OSMColorOverlay(JTSModel model) {
        this.model = model;
        setOverlayIndex(DEFAULT_OVERLAY_INDEX);
    }

    public void updateBoundingBox(BoundingBox bbox) {
        double x1 = bbox.getLonWest();
        double x2 = bbox.getLonEast();
        double y1 = bbox.getLatSouth();
        double y2 = bbox.getLatNorth();
        envelope = new Envelope(x1, x2, y1, y2);
    }

    @Override
    protected void draw(Canvas c, MapView mapView, boolean shadow) {
        // no shadow support & need a bounding box to query rtree & at or above min render zoom
        if (shadow || envelope == null || zoom < minVectorRenderZoom) {
            return;
        }

        List<OSMWay> polys = new ArrayList<>();
        List<OSMWay> lines = new ArrayList<>();
        List<OSMNode> points = new ArrayList<>();

        List<OSMElement> viewPortElements = model.queryFromEnvelope(envelope);

        // Sort the elements into their geom types so we can draw
        // points on top of lines on top of polys.
        for (OSMElement el : viewPortElements) {
            if (el instanceof OSMWay) {
                OSMWay w = (OSMWay) el;
                if (w.isClosed()) {
                    polys.add(w);
                } else {
                    lines.add(w);
                }
                continue;
            }
            // if it isn't a Way, it's a Node.
            points.add((OSMNode)el);
        }

        // Draw polygons
        for (OSMWay w : polys) {
            w.getOSMPath(mapView).draw(c);
        }

        // Draw lines
        for (OSMWay w : lines) {
            w.getOSMPath(mapView).draw(c);
        }
    }

}
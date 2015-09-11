/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */

package com.spatialdev.osm.renderer;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.marker.OSMMarker;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.model.OSMNode;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMWay;
import com.vividsolutions.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.List;

public class OSMOverlay extends Overlay {

    private static final int DEFAULT_OVERLAY_INDEX = 4;
    
    private JTSModel model;
    private Envelope envelope;
    
    private float minVectorRenderZoom = 0;
    private float zoom = 0; // current zoom of map

    private List<OSMNode> viewPortNodes = new ArrayList<>();

    private boolean needToAddItemizedOverlay = true;

    /**
     * This should only be created by OSMMap.
     * * *
     * @param model
     */
    public OSMOverlay(JTSModel model) {
        this.model = model;
        setOverlayIndex(DEFAULT_OVERLAY_INDEX);
    }

    /**
     * This should only be created by OSMMap.
     * * *
     * @param model
     * @param minVectorRenderZoom
     */
    public OSMOverlay(JTSModel model, float minVectorRenderZoom) {
        this(model);
        this.minVectorRenderZoom = minVectorRenderZoom;
    }
    
    public void updateBoundingBox(BoundingBox bbox) {
        double x1 = bbox.getLonWest();
        double x2 = bbox.getLonEast();
        double y1 = bbox.getLatSouth();
        double y2 = bbox.getLatNorth();
        envelope = new Envelope(x1, x2, y1, y2);
    }

    /**
     * Have the map set the current zoom.
     * * 
     * @param zoom
     */
    public void updateZoom(float zoom) {
        this.zoom = zoom;
    }

    public List<OSMNode> getViewPortNodes() {
        return viewPortNodes;
    }
    
    @Override
    protected void draw(Canvas c, MapView mapView, boolean shadow) {
        // no shadow support & need a bounding box to query rtree & at or above min render zoom
        if (shadow || envelope == null || zoom < minVectorRenderZoom) {
            return;
        }

        List<OSMWay> polys = new ArrayList<>();
        List<OSMWay> lines = new ArrayList<>();

        // We want to always be referring to the same list so external sources
        // do not reference stale lists.
        viewPortNodes.clear();
        
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
            // If it isn't a Way, it's a Node.
            // We need to render the marker...
            renderMarker(mapView, (OSMNode) el);
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

    private void renderMarker(MapView mapView, OSMNode node) {
        viewPortNodes.add(node);
        if (node.getMarker() == null) {
            OSMMarker marker = new OSMMarker(mapView, node);
            marker.setMarker(mapView.getContext().getResources().getDrawable(R.mipmap.maki_star_blue));
            /**
             * Issue #81
             * setMarker doesn't position bitmaps in the same way as setIcon.
             * By setting the anchor, we bring down the image slightly so that the marker
             * does indeed point to the point it refers to.
             */
            PointF anchor = new PointF(0.5f, 0.8f);
            marker.setAnchor(anchor);
            mapView.addOSMMarker(this, marker);
        } else if (needToAddItemizedOverlay){
            mapView.setDefaultOSMItemizedOverlay(this);
            needToAddItemizedOverlay = false;
        }
    }

}

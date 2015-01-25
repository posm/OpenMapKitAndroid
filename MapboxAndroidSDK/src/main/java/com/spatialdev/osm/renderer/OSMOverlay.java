/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */

package com.spatialdev.osm.renderer;

import android.graphics.Canvas;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.model.Node;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.Way;
import com.vividsolutions.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.List;

public class OSMOverlay extends Overlay {

    private static final int DEFAULT_OVERLAY_INDEX = 1;
    
    private JTSModel model;
    private Envelope envelope;
    
    public OSMOverlay(JTSModel model) {
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
        // no shadow support & need a bounding box to query rtree
        if (shadow || envelope == null) {
            return;
        }

        List<Way> polys = new ArrayList<>();
        List<Way> lines = new ArrayList<>();
        List<Node> points = new ArrayList<>();
        
        List<OSMElement> viewPortElements = model.queryFromEnvelope(envelope);
        
        // Sort the elements into their geom types so we can draw 
        // points on top of lines on top of polys.
        for (OSMElement el : viewPortElements) {
            if (el instanceof Way) {
                Way w = (Way) el;
                if (w.isClosed()) {
                    polys.add(w);
                } else {
                    lines.add(w);
                }
                continue;
            }
            // if it isn't a Way, it's a Node.
            points.add((Node)el);
        }
        
        
        
    }
    
}

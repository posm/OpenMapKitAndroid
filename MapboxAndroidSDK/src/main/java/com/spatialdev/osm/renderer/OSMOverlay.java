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
import com.vividsolutions.jts.geom.Envelope;

public class OSMOverlay extends Overlay {

    private JTSModel model;
    private Envelope envelope;
    
    public OSMOverlay(JTSModel model) {
        this.model = model;
        
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
        // no shadow support
        if (shadow) {
            return;
        }
        
        
    }
    
}

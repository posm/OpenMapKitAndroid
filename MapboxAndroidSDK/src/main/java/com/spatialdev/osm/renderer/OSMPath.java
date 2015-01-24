package com.spatialdev.osm.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.spatialdev.osm.model.Node;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public abstract class OSMPath extends Overlay {

    private static final int DEFAULT_OVERLAY_INDEX = 1;
    private int overlayIndex = DEFAULT_OVERLAY_INDEX;
    
    protected Paint paint = new Paint();
    protected final Path path = new Path();
    
    // These are the points for a path converted to an "intermediate"
    // pixel space of the entire earth.
    protected PointF[] projectedPoints;
    
    protected Projection pj;
    
    public OSMPath(List<Node> nodes, MapView mapView) {
        projectNodes(nodes, mapView);
        pj = mapView.getProjection();
    }

    /**
     * Do the expensive projection straight up upon construction rather than draw.
     * 
     * @param nodes
     * @param mapView
     */
    private void projectNodes(List<Node> nodes, MapView mapView) {
        projectedPoints = new PointF[nodes.size()];
        final Projection pj = mapView.getProjection();
        int i = 0;
        for (Node n : nodes) {
            projectedPoints[i++] = pj.toMapPixelsProjected(n.getLng(), n.getLat(), null);
        }
    }

    /**
     * Sets the z position of this layer in the layer stack
     * larger values for @param layerIndex are drawn on top.
     *
     * Default values are:
     * 0 for MapEventsOverlay
     * 1 for PathOverlay
     * 2 for UserLocationOverlay
     * 3 for other Overlays
     */
    public void setOverlayIndex(int overlayIndex) {
        this.overlayIndex = overlayIndex;
    }

    public Paint getPaint() {
        return paint;
    }

    public OSMPath setPaint(final Paint pPaint) {
        paint = pPaint;
        return this;
    }

    @Override
    protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
        int size = projectedPoints.length;
        
        // nothing to paint
        if (shadow || size < 2) {
            return;
        }

        PointF screenPoint0; // points on screen
        PointF screenPoint1;
        PointF projectedPoint0; // points from the points list
        PointF projectedPoint1;
        
        path.rewind();
        projectedPoint0 = projectedPoints[size - 1];
        
    }
    
}

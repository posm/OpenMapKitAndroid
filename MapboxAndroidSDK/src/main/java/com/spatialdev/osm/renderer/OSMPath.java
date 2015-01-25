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
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.Way;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public abstract class OSMPath extends Overlay {

    protected Paint paint = new Paint();
    protected final Path path = new Path();

    // These are the points for a path converted to an "intermediate"
    // pixel space of the entire earth.
    protected PointF[] projectedPoints;

    public static OSMPath createOSMPathFromOSMElement(OSMElement element) {
        if (element instanceof Way) {
            Way w = (Way) element;
            // polygon
            if (w.isClosed()) {
                return new OSMPolygon(w);
            }
            // line
            return new OSMLine(w);
        }
        
        // TODO Point
        return null;
    }

    /**
     * We only want to construct subclasses. This is ultimately created via
     * OSMPath.createOSMPathFromOSMElement
     * * * *
     * @param w
     */
    protected OSMPath(Way w) {
        List<Node> nodes = w.getNodes();
        projectNodes(nodes);
    }

    /**
     * Do the expensive projection straight up upon construction rather than draw.
     *
     * @param nodes
     */
    private void projectNodes(List<Node> nodes) {
        projectedPoints = new PointF[nodes.size()];
        int i = 0;
        for (Node n : nodes) {
            // Note: PathOverlay calls this static method from an instance variable. Doesn't matter though...
            projectedPoints[i++] = Projection.toMapPixelsProjected(n.getLng(), n.getLat(), null);
        }
    }

    public Paint getPaint() {
        return paint;
    }

    public OSMPath setPaint(final Paint pPaint) {
        paint = pPaint;
        return this;
    }
    
    public void select() {
        
        
    }
    
    public void deselect() {
        
        
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

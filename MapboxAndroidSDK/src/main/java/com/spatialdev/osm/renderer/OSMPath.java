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
public abstract class OSMPath {

    /**
     * Paint Settings * 
     */
    protected Paint paint = new Paint();
    protected final Path path = new Path();
    
    // This is the real stroke width. 
    // The paint's stroke width gets adjusted for approximate zooms.
    private float strokeWidth = 10.0f;

    /**
     * This gets reused by Projection#toMapPixelsTranslated so 
     * that the returned Point is not constantly reallocated.
     * * * 
     */
    protected final PointF tempPoint0 = new PointF();
    protected final PointF tempPoint1 = new PointF();

    // These are the points for a path converted to an "intermediate"
    // pixel space of the entire earth.
    protected PointF[] projectedPoints;
    
    protected MapView mapView;
    protected Projection pj;

    public static OSMPath createOSMPath(OSMElement element, MapView mv) {
        if (element instanceof Way) {
            Way w = (Way) element;
            // polygon
            if (w.isClosed()) {
                return new OSMPolygon(w, mv);
            }
            // line
            return new OSMLine(w, mv);
        }
        
        // TODO Point
        return null;
    }

    /**
     * We only want to construct subclasses. This is ultimately created via
     * OSMPath.createOSMPath
     * * * *
     * @param w Way, MapView mv
     */
    protected OSMPath(Way w, MapView mv) {
        List<Node> nodes = w.getNodes();
        projectNodes(nodes);
        mapView = mv;
        pj = mv.getProjection();
    }

    /**
     * Do the expensive projection straight up upon construction rather than draw.
     *
     * TODO: LOOK INTO HAVING THESE POINTS NOT BE FLOAT!!!!
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
    
    public void setStrokeWidth(float width) {
        strokeWidth = width;
    }
    
    public float getStrokeWidth() {
        return strokeWidth;
    }
    
    public void select() {
        
        
    }
    
    public void deselect() {
        
        
    }

    public void draw(final Canvas c) {
        int size = projectedPoints.length;

        // nothing to paint
        if (size < 2) {
            return;
        }

        PointF screenPoint0; // points on screen
        PointF screenPoint1;
        PointF projectedPoint0; // points from the points list
        PointF projectedPoint1;

        path.rewind();
        projectedPoint0 = projectedPoints[size - 1];
        screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, tempPoint0);
        // We want to dereference this object as little as possible.
        float screenPoint0_x = screenPoint0.x; 
        float screenPoint0_y = screenPoint0.y;
        
        path.moveTo(screenPoint0_x, screenPoint0_y);
        
        for (int i = size - 2; i >= 0; --i) {
            projectedPoint1 = projectedPoints[i];
            screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, tempPoint1);
            float screenPoint1_x = screenPoint1.x;
            float screenPoint1_y = screenPoint1.y;
            
            // Skip if the next point isn't a pixel away
            if (Math.abs(screenPoint1_x - screenPoint0_x) < 1 
                    || Math.abs(screenPoint1_y - screenPoint0_y) < 1) {
                continue;
            }
            
            path.lineTo(screenPoint1_x, screenPoint1_y);
            
            // Update comparison points to next position.
            screenPoint0_x = screenPoint1_x;
            screenPoint0_y = screenPoint1_y;
        }
        
        paint.setStrokeWidth(strokeWidth / mapView.getScale());
        c.drawPath(path, paint);
    }

}

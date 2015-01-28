package com.spatialdev.osm.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

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
    protected final double[] tempPoint0 = new double[2];
    protected final double[] tempPoint1 = new double[2];

    // These are the points for a path converted to an "intermediate"
    // pixel space of the entire earth.
    protected double[][] projectedPoints;
    
    protected MapView mapView;
    
    // gets set in draw, the bounds of the viewport in Mercator Projected Pixels
    protected Rect viewPortBounds;
    
    protected boolean pathMoveToPlaced = false;

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
    }

    /**
     * Do the expensive projection straight up upon construction rather than draw.
     *
     * TODO: LOOK INTO HAVING THESE POINTS NOT BE FLOAT!!!!
     * @param nodes
     */
    private void projectNodes(List<Node> nodes) {
        projectedPoints = new double[nodes.size()][2];
        int i = 0;
        for (Node n : nodes) {
            projectedPoints[i++] = Projection.latLongToPixelXY(n.getLat(), n.getLng());
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

        final Projection pj = mapView.getProjection();
        viewPortBounds = pj.fromPixelsToProjected(pj.getScreenRect());

        double[] screenPoint0; // points on screen
        double[] screenPoint1;
        double[] projectedPoint0; // points from the points list
        double[] projectedPoint1;

        path.rewind();
        projectedPoint0 = projectedPoints[size - 1];
        screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, tempPoint0);
        
        clipOrDrawPath(path, projectedPoint0, screenPoint0);

        for (int i = size - 2; i >= 0; --i) {
            projectedPoint1 = projectedPoints[i];
            screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, tempPoint1);

            clipOrDrawPath(path, projectedPoint1, screenPoint1);

        }
        pathMoveToPlaced = false;
        paint.setStrokeWidth(strokeWidth / mapView.getScale());
        c.drawPath(path, paint);
    }

    abstract void clipOrDrawPath(Path path, double[] projectedPoint1, double[] screenPoint1);
    
}

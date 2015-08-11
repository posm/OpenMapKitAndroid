package org.redcross.openmapkit.mapcoloring;

/**
 * Created by coder on 8/11/15.
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMNode;
import com.spatialdev.osm.model.OSMWay;
import com.spatialdev.osm.renderer.OSMLine;
import com.spatialdev.osm.renderer.OSMPolygon;

import java.util.List;

public abstract class OSMColorPath {

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
    protected final double[] tempPoint = new double[2];

    // These are the points for a path converted to an "intermediate"
    // pixel space of the entire earth.
    protected double[][] projectedPoints;

    protected MapView mapView;

    // gets set in draw, the bounds of the viewport in Mercator Projected Pixels
    protected Rect viewPortBounds;

    /**
     * When drawing, this gets set to true when
     * we know that we wan to call path.lineTo next.
     * * * *
     */
    protected boolean pathLineToReady = false;


    public static OSMColorPath createOSMPath(OSMElement element, MapView mv) {
        if (element instanceof OSMWay) {
            OSMWay w = (OSMWay) element;
            // polygon
            if (w.isClosed()) {
                return new OSMColorPolygon(w, mv);
            }
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
    protected OSMColorPath(OSMWay w, MapView mv) {
        List<OSMNode> nodes = w.getNodes();
        projectNodes(nodes);
        mapView = mv;
        paint.setAntiAlias(true);
    }

    /**
     * Do the expensive projection straight up upon construction rather than draw.
     *
     * @param nodes
     */
    private void projectNodes(List<OSMNode> nodes) {
        projectedPoints = new double[nodes.size()][2];
        int i = 0;
        for (OSMNode n : nodes) {
            projectedPoints[i++] = Projection.latLongToPixelXY(n.getLat(), n.getLng());
        }
    }

    public Paint getPaint() {
        return paint;
    }

    public OSMColorPath setPaint(final Paint pPaint) {
        paint = pPaint;
        return this;
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setMapView(MapView mv) {
        mapView = mv;
    }

    public void draw(final Canvas c) {
        int size = projectedPoints.length;

        // nothing to paint
        if (size < 2) {
            return;
        }

        final Projection pj = mapView.getProjection();
        viewPortBounds = pj.fromPixelsToProjected(pj.getScreenRect());

        double[] screenPoint; // points on screen
        double[] projectedPoint; // points from the points list

        path.rewind();

        // Looping downward is the fastest loop you can do in Dalvik.
        for (int i = size - 1; i > 0; --i) { // every one but the 0th
            projectedPoint = projectedPoints[i];
            screenPoint = pj.toMapPixelsTranslated(projectedPoint, tempPoint);
            clipOrDrawPath(path, projectedPoint, projectedPoints[i-1], screenPoint);
        }
        // that 0th projected point has no next projected point...
        projectedPoint = projectedPoints[0];
        screenPoint = pj.toMapPixelsTranslated(projectedPoint, tempPoint);
        clipOrDrawPath(path, projectedPoint, null, screenPoint);

        pathLineToReady = false;
        paint.setStrokeWidth(strokeWidth / mapView.getScale());
        c.drawPath(path, paint);
    }

    abstract void clipOrDrawPath(Path path, double[] projectedPoint, double[] nextProjectedPoint, double[] screenPoint1);

}


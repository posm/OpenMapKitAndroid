package com.mapbox.mapboxsdk.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Viesturs Zarins
 * @author Martin Pearman
 *         <p/>
 *         This class draws a path line in given color.
 */
public class PathOverlay extends Overlay {

    private static final String TAG = "PathOverlay";
    /**
     * Stores points, converted to the map projection.
     */
    private ArrayList<PointF> mPoints;

    /**
     * Number of points that have precomputed values.
     */
    private int mPointsPrecomputed;


    private boolean mOptimizePath = true;

    /**
     * Paint settings.
     */
    protected Paint mPaint = new Paint();
    private final Path mPath = new Path();

    private final PointF mTempPoint1 = new PointF();
    private final PointF mTempPoint2 = new PointF();

    // bounding rectangle for the current line segment.
    private final Rect mLineBounds = new Rect();

    /**
     * These are provided as parameters in the draw method.
     */
    protected MapView mapView;
    protected Canvas canvas;

    
    public PathOverlay() {
        super();
        this.mPaint.setColor(Color.BLUE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(10.0f);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.clearPath();
        setOverlayIndex(1);
    }

    public PathOverlay(final int color, final float width) {
        super();
        this.mPaint.setColor(color);
        this.mPaint.setStrokeWidth(width);
        this.mPaint.setStyle(Paint.Style.STROKE);

        this.clearPath();
        setOverlayIndex(PATHOVERLAY_INDEX);
    }

    public Paint getPaint() {
        return mPaint;
    }

    public PathOverlay setPaint(final Paint pPaint) {
        mPaint = pPaint;
        return this;
    }

    public void clearPath() {
        this.mPoints = new ArrayList<PointF>();
        this.mPointsPrecomputed = 0;
    }

    public void addPoint(final LatLng aPoint) {
        addPoint(aPoint.getLatitude(), aPoint.getLongitude());
    }

    public void addPoint(final double aLatitude, final double aLongitude) {
        mPoints.add(new PointF((float) aLatitude, (float) aLongitude));
    }

    public void addPoints(final LatLng... aPoints) {
        for (final LatLng point : aPoints) {
            addPoint(point);
        }
    }

    public void addPoints(final List<LatLng> aPoints) {
        for (final LatLng point : aPoints) {
            addPoint(point);
        }
    }

    public void removeAllPoints() {
        mPoints.clear();
    }

    public int getNumberOfPoints() {
        return this.mPoints.size();
    }

    /**
     * This is where the paths are set up to do the draw.
     * This needs to be redone. optimizePath does not work
     * right, the projection is broken, and this can be
     * done much more efficiently - needs a Spatial Index!!!
     * * * * * * *
     * @param params
     * @return
     */
    @Override
    protected Boolean doInBackground(Boolean... params) {
        boolean shadow = params[0];
        final int size = this.mPoints.size();

        // nothing to paint
        if (shadow || size < 2) {
            return false;
        }

        final Projection pj = mapView.getProjection();

        // precompute new points to the intermediate projection.
        for (; this.mPointsPrecomputed < size; this.mPointsPrecomputed++) {
            final PointF pt = this.mPoints.get(this.mPointsPrecomputed);
            pj.toMapPixelsProjected((double) pt.x, (double) pt.y, pt);
        }

        PointF screenPoint0 = null; // points on screen
        PointF screenPoint1;
        PointF projectedPoint0; // points from the points list
        PointF projectedPoint1;

        // clipping rectangle in the intermediate projection, to avoid performing projection.
        final Rect clipBounds = pj.fromPixelsToProjected(pj.getScreenRect());

        mPath.rewind();
        boolean needsDrawing = !mOptimizePath;
        projectedPoint0 = this.mPoints.get(size - 1);
        mLineBounds.set((int) projectedPoint0.x, (int) projectedPoint0.y, (int) projectedPoint0.x,
                (int) projectedPoint0.y);

        for (int i = size - 2; i >= 0; i--) {
            // compute next points
            projectedPoint1 = this.mPoints.get(i);

            //mLineBounds needs to be computed
            mLineBounds.union((int) projectedPoint1.x, (int) projectedPoint1.y);

            if (mOptimizePath && !Rect.intersects(clipBounds, mLineBounds)) {
                // skip this line, move to next point
                projectedPoint0 = projectedPoint1;
                mLineBounds.set((int) projectedPoint0.x, (int) projectedPoint0.y, (int) projectedPoint0.x,
                        (int) projectedPoint0.y);
                screenPoint0 = null;
                continue;
            }

            // the starting point may be not calculated, because previous segment was out of clip
            // bounds
            if (screenPoint0 == null) {
                screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
                mPath.moveTo(screenPoint0.x, screenPoint0.y);
            }

            screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);

            // skip this point, too close to previous point
            if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(
                    screenPoint1.y - screenPoint0.y) <= 1) {
                continue;
            }

            mPath.lineTo(screenPoint1.x, screenPoint1.y);
            // update starting point to next position
            projectedPoint0 = projectedPoint1;
            screenPoint0.x = screenPoint1.x;
            screenPoint0.y = screenPoint1.y;
            if (mOptimizePath) {
                needsDrawing = true;
                mLineBounds.set((int) projectedPoint0.x, (int) projectedPoint0.y, (int) projectedPoint0.x,
                        (int) projectedPoint0.y);
            }
        }
        if (!mOptimizePath) {
            needsDrawing = Rect.intersects(clipBounds, mLineBounds);
        }
        return needsDrawing;
    }

    @Override
    protected void onPostExecute(Boolean needsDrawing) {
        if (needsDrawing) {
            final float realWidth = mPaint.getStrokeWidth();
            mPaint.setStrokeWidth(realWidth / mapView.getScale());
            canvas.drawPath(mPath, mPaint);
            mPaint.setStrokeWidth(realWidth);
        }
    }
    
    /**
     * This method draws the line.
     */
    @Override
    protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
        this.mapView = mapView;
        this.canvas = canvas;

        /**
         * You can uncomment this to test drawing without threads (identical to the old way).
         * * *
         */
        Boolean needsDrawing = doInBackground(shadow);
        onPostExecute(needsDrawing);
        
//        Status status = getStatus();
//        if (status == Status.RUNNING) {
//            cancel(true);
//            execute(shadow);
//        }
    }

    /**
     * if true the path will be optimised. True by default. But be aware that the optimize method
     * does not work for filled path.
     */
    public void setOptimizePath(final boolean value) {
        mOptimizePath = value;
    }
}
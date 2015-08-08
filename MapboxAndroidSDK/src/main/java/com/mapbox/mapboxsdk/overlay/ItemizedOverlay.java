// Created by plusminus on 23:18:23 - 02.10.2008
package com.mapbox.mapboxsdk.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.MotionEvent;

import com.mapbox.mapboxsdk.clustering.Cluster;
import com.mapbox.mapboxsdk.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.mapbox.mapboxsdk.clustering.algo.PreCachingAlgorithmDecorator;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas.UnsafeCanvasHandler;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Draws a list of {@link Marker} as markers to a map. The item with the lowest index is drawn
 * as last and therefore the 'topmost' marker. It also gets checked for onTap first. This class is
 * generic, because you then you get your custom item-class passed back in onTap().
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 * @author Theodore Hong
 * @author Fred Eisele
 */
public abstract class ItemizedOverlay extends SafeDrawOverlay implements Overlay.Snappable, MapListener {
    private static final String TAG = ItemizedOverlay.class.getSimpleName();
    private final ArrayList<Marker> mInternalItemList;
    private ArrayList<ClusterMarker> mInternalClusterList;
    protected boolean mDrawFocusedItem = true;
    private Marker mFocusedItem;
    private boolean mPendingFocusChangedEvent = false;
    private OnFocusChangeListener mOnFocusChangeListener;
    private boolean mIsClusteringEnabled;
    private ClusterMarker.OnDrawClusterListener mOnDrawClusterListener;

    private static SafePaint mClusterTextPaint;
    private CalculateClusterTask mCalculateClusterTask;
    private float mMinZoomForClustering = 22;

    private PreCachingAlgorithmDecorator<Marker> mAlgorithm;


    /**
     * Method by which subclasses create the actual Items. This will only be called from populate()
     * we'll cache them for later use.
     */
    protected abstract Marker createItem(int i);

    /**
     * The number of items in this overlay.
     */
    public abstract int size();

    public ItemizedOverlay() {

        super();

        if (mClusterTextPaint == null) {
            mClusterTextPaint = new SafePaint();

            mClusterTextPaint.setTextAlign(Paint.Align.CENTER);
            mClusterTextPaint.setTextSize(30);
            mClusterTextPaint.setFakeBoldText(true);
        }

        mAlgorithm = new PreCachingAlgorithmDecorator<>(new NonHierarchicalDistanceBasedAlgorithm<Marker>());

        mInternalItemList = new ArrayList<>();

        mInternalClusterList = new ArrayList<>();
    }

    /**
     * Draw a marker on each of our items. populate() must have been called first.<br/>
     * <br/>
     * The marker will be drawn twice for each Item in the Overlay--once in the shadow phase,
     * skewed
     * and darkened, then again in the non-shadow phase. The bottom-center of the marker will be
     * aligned with the geographical coordinates of the Item.<br/>
     * <br/>
     * The order of drawing may be changed by overriding the getIndexToDraw(int) method. An item
     * may
     * provide an alternate marker via its Marker.getMarker(int) method. If that method returns
     * null, the default marker is used.<br/>
     * <br/>
     * The focused item is always drawn last, which puts it visually on top of the other
     * items.<br/>
     *
     * @param canvas  the Canvas upon which to draw. Note that this may already have a
     *                transformation
     *                applied, so be sure to leave it the way you found it
     * @param mapView the MapView that requested the draw. Use MapView.getProjection() to convert
     *                between on-screen pixels and latitude/longitude pairs
     * @param shadow  if true, draw the shadow layer. If false, draw the overlay contents.
     */
    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
        if (shadow) {
            return;
        }

        if (mPendingFocusChangedEvent && mOnFocusChangeListener != null) {
            mOnFocusChangeListener.onFocusChanged(this, mFocusedItem);
        }
        mPendingFocusChangedEvent = false;

        final Projection pj = mapView.getProjection();
        final int size = this.mInternalItemList.size() - 1;

        final RectF bounds =
                new RectF(0, 0, mapView.getMeasuredWidth(), mapView.getMeasuredHeight());
        pj.rotateRect(bounds);
        final float mapScale = 1 / mapView.getScale();

        if (!mIsClusteringEnabled || mapView.getZoomLevel() > mMinZoomForClustering) {
            /* Draw in backward cycle, so the items with the least index are on the front. */
            for (int i = size; i >= 0; i--) {
                final Marker item = getItem(i);
                if (item == mFocusedItem) {
                    continue;
                }
                onDrawItem(canvas, item, pj, mapView.getMapOrientation(), bounds, mapScale);
            }

            if (mFocusedItem != null) {
                onDrawItem(canvas, mFocusedItem, pj, mapView.getMapOrientation(), bounds, mapScale);
            }

        } else if (mInternalClusterList != null) {
            for (int i = mInternalClusterList.size() - 1; i >= 0; --i) {
                final ClusterMarker clusterMarker = mInternalClusterList.get(i);
                List<Marker> markerList = clusterMarker.getMarkersReadOnly();

                if (markerList.size() > 1) {
//                    if (mOnDrawClusterListener != null) {
//                        Drawable drawable = mOnDrawClusterListener.drawCluster(clusterMarker);
//                        clusterMarker.setMarker(drawable);
//                    }
                    onDrawItem(canvas, clusterMarker, pj, mapView.getMapOrientation(), bounds, mapScale);
                } else {
                    onDrawItem(canvas, markerList.get(0), pj, mapView.getMapOrientation(), bounds, mapScale);
                }

            }
        }
    }

    /**
     * Utility method to perform all processing on a new ItemizedOverlay. Subclasses provide Items
     * through the createItem(int) method. The subclass should call this as soon as it has data,
     * before anything else gets called.
     */
    protected void populate() {
        final int size = size();
        mAlgorithm.clearItems();
        mInternalItemList.clear();
        mInternalItemList.ensureCapacity(size);
        for (int a = 0; a < size; a++) {
            mInternalItemList.add(createItem(a));
        }
        mAlgorithm.addItems(mInternalItemList);

    }

    /**
     * Returns the Item at the given index.
     *
     * @param position the position of the item to return
     * @return the Item of the given index.
     */
    public final Marker getItem(final int position) {
        return mInternalItemList.get(position);
    }

    /**
     * Draws an item located at the provided screen coordinates to the canvas.
     *
     * @param canvas          what the item is drawn upon.
     * @param item            the item to be drawn.
     * @param projection      the projection to use.
     * @param aMapOrientation
     * @param mapBounds
     * @param mapScale
     */
    protected void onDrawItem(ISafeCanvas canvas, final Marker item, final Projection projection,
                              final float aMapOrientation, final RectF mapBounds, final float mapScale) {
        item.updateDrawingPosition();
        final PointF position = item.getPositionOnMap();
        final Point roundedCoords = new Point((int) position.x, (int) position.y);
        if (!RectF.intersects(mapBounds, item.getDrawingBounds(projection, null))) {
            //dont draw item if offscreen
            return;
        }

        canvas.save();

        canvas.scale(mapScale, mapScale, position.x, position.y);
        final int state =
                (mDrawFocusedItem && (mFocusedItem == item) ? Marker.ITEM_STATE_FOCUSED_MASK : 0);
        final Drawable marker = item.getMarker(state);
        if (marker == null) {
            return;
        }
        final Point point = item.getAnchor();

        // draw it
        if (this.isUsingSafeCanvas()) {
            Overlay.drawAt(canvas.getSafeCanvas(), marker, roundedCoords, point, false,
                    aMapOrientation);
        } else {
            canvas.getUnsafeCanvas(new UnsafeCanvasHandler() {
                @Override
                public void onUnsafeCanvas(Canvas canvas) {
                    Overlay.drawAt(canvas, marker, roundedCoords, point, false, aMapOrientation);
                }
            });
        }

        canvas.restore();
    }

    protected boolean markerHitTest(final Marker pMarker, final Projection pProjection,
                                    final float pX, final float pY) {
        RectF rect = pMarker.getHitBounds(pProjection, null);
/*
        RectF rect = pMarker.getDrawingBounds(pProjection, null);
        if (pMarker.isUsingMakiIcon()) {
            //a marker drawing bounds is twice the actual size of the marker
            rect.bottom -= rect.height() / 2;
        }
*/
        return rect.contains(pX, pY);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
        final int size = this.size();
        final Projection projection = mapView.getProjection();
        final float x = e.getX();
        final float y = e.getY();

        for (int i = 0; i < size; i++) {
            final Marker item = getItem(i);
            if (markerHitTest(item, projection, x, y)) {
                // We have a hit, do we get a response from onTap?
                if (onTap(i)) {
                    // We got a response so consume the event
                    return true;
                }
            }
        }

        return super.onSingleTapConfirmed(e, mapView);
    }

    /**
     * Override this method to handle a "tap" on an item. This could be from a touchscreen tap on
     * an
     * onscreen Item, or from a trackball click on a centered, selected Item. By default, does
     * nothing and returns false.
     *
     * @return true if you handled the tap, false if you want the event that generated it to pass to
     * other overlays.
     */
    protected boolean onTap(int index) {
        return false;
    }

    /**
     * Set whether or not to draw the focused item. The default is to draw it, but some clients may
     * prefer to draw the focused item themselves.
     */
    public void setDrawFocusedItem(final boolean drawFocusedItem) {
        mDrawFocusedItem = drawFocusedItem;
    }

    /**
     * If the given Item is found in the overlay, force it to be the current focus-bearer. Any
     * registered {@link ItemizedOverlay} will be notified. This does not move
     * the map, so if the Item isn't already centered, the user may get confused. If the Item is
     * not
     * found, this is a no-op. You can also pass null to remove focus.
     */
    public void setFocus(final Marker item) {
        mPendingFocusChangedEvent = item != mFocusedItem;
        mFocusedItem = item;
    }

    /**
     * @return the currently-focused item, or null if no item is currently focused.
     */
    public Marker getFocus() {
        return mFocusedItem;
    }

    /**
     * an item want's to be blured, if it is the focused one, blur it
     */
    public void blurItem(final Marker item) {
        if (mFocusedItem == item) {
            setFocus(null);
        }
    }

    //    /**
    //     * Adjusts a drawable's bounds so that (0,0) is a pixel in the location described by the anchor
    //     * parameter. Useful for "pin"-like graphics. For convenience, returns the same drawable that
    //     * was passed in.
    //     *
    //     * @param marker  the drawable to adjust
    //     * @param anchor the anchor for the drawable (float between 0 and 1)
    //     * @return the same drawable that was passed in.
    //     */
    //    protected synchronized Drawable boundToHotspot(final Drawable marker, Point anchor) {
    //        final int markerWidth = marker.getIntrinsicWidth();
    //        final int markerHeight = marker.getIntrinsicHeight();
    //
    //        mRect.set(0, 0, markerWidth, markerHeight);
    //        mRect.offset(anchor.x, anchor.y);
    //        marker.setBounds(mRect);
    //        return marker;
    //    }

    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }

    public interface OnFocusChangeListener {
        void onFocusChanged(ItemizedOverlay overlay, Marker newFocus);
    }


    /**
     * Enable or disable clustering
     *
     * @param enabled
     * @param onDrawClusterListener A listener that allows the modification of the cluster's drawable
     */
    public void setClusteringEnabled(final boolean enabled, final ClusterMarker.OnDrawClusterListener onDrawClusterListener, float minZoom) {
        mIsClusteringEnabled = enabled;
        mOnDrawClusterListener = onDrawClusterListener;
        mMinZoomForClustering = minZoom;
    }

    public void onScroll(ScrollEvent event) {

    }

    /**
     * Called when a map is zoomed.
     */
    public void onZoom(ZoomEvent event) {
        if (mIsClusteringEnabled && event.getZoomLevel() < mMinZoomForClustering) {
            if (mCalculateClusterTask != null && mCalculateClusterTask.getStatus() != AsyncTask.Status.FINISHED) {
                mCalculateClusterTask.cancel(true);
            }
            mCalculateClusterTask = new CalculateClusterTask(event);
            mCalculateClusterTask.execute();
        }
    }

    /**
     * Called when a map is rotated.
     */
    public void onRotate(RotateEvent event) {

    }

    public ClusterMarker.OnDrawClusterListener getOnDrawClusterListener() {
        return mOnDrawClusterListener;
    }

    public boolean isClusteringEnabled() {
        return mIsClusteringEnabled;
    }

    private class CalculateClusterTask extends AsyncTask<Void, Void, ArrayList<ClusterMarker>> {
        private ZoomEvent mZoomEvent;

        public CalculateClusterTask(ZoomEvent event) {
            mZoomEvent = event;
        }


        @Override
        protected ArrayList<ClusterMarker> doInBackground(final Void... voids) {
            ArrayList<ClusterMarker> clusterMarkers = new ArrayList<>();
            Set<? extends Cluster<Marker>> clusters = mAlgorithm.getClusters(mZoomEvent.getZoomLevel());
            for (Cluster<Marker> cluster : clusters) {
                Collection<Marker> markers = cluster.getItems();
                if (markers.size() > 0) {
                    ClusterMarker clusterMarker;

                    clusterMarker = new ClusterMarker();
                    clusterMarker.addMarkersToCluster(markers);
                    clusterMarker.addTo(mZoomEvent.getSource());
                    clusterMarker.setPoint(cluster.getPosition());
                    if (mOnDrawClusterListener != null) {
                        Drawable drawable = mOnDrawClusterListener.drawCluster(clusterMarker);
                        clusterMarker.setMarker(drawable);
                    }

                    clusterMarkers.add(clusterMarker);

                }
            }
            return clusterMarkers;
        }

        @Override
        protected void onPostExecute(final ArrayList<ClusterMarker> clusterList) {
            mInternalClusterList = clusterList;
            mZoomEvent.getSource().invalidate();
        }
    }
}

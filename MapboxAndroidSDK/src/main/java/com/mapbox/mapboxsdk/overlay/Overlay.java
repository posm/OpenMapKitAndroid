// Created by plusminus on 20:32:01 - 27.09.2008
package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.mapbox.mapboxsdk.views.MapView;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class representing an overlay which may be displayed on top of a {@link MapView}. To add an
 * overlay, subclass this class, create an instance, and add it to the list obtained from
 * getOverlays() of {@link MapView}.
 * <p/>
 * This class implements a form of Gesture Handling similar to
 * {@link android.view.GestureDetector.SimpleOnGestureListener} and
 * {@link GestureDetector.OnGestureListener}. The difference is there is an additional argument for
 * the item.
 *
 * @author Nicolas Gramlich
 */
public abstract class Overlay {
    private static AtomicInteger sOrdinal = new AtomicInteger();

    protected float mScale;
    private static final Rect mRect = new Rect();
    private boolean mEnabled = true;
    private int mOverlayIndex = 3;

    public static final int MAPEVENTSOVERLAY_INDEX = 0;
    public static final int PATHOVERLAY_INDEX = 1;
    public static final int USERLOCATIONOVERLAY_INDEX = 2;

    public Overlay() {
    }

    public Overlay(final Context ctx) {
        mScale = ctx.getResources().getDisplayMetrics().density;
    }

    /**
     */
    public Overlay setContext(final Context ctx) {
        mScale = ctx.getResources().getDisplayMetrics().density;
        return this;
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
        mOverlayIndex = overlayIndex;
    }

    /**
     * Get the z position of this layer in the overlay stack
     * @return overlay index, larger values are drawn on top
     */
    public int getOverlayIndex() {
        return mOverlayIndex;
    }

    /**
     * Sets whether the Overlay is marked to be enabled. This setting does nothing by default, but
     * should be checked before calling draw().
     */
    public void setEnabled(final boolean pEnabled) {
        this.mEnabled = pEnabled;
    }

    /**
     * Specifies if the Overlay is marked to be enabled. This should be checked before calling
     * draw().
     *
     * @return true if the Overlay is marked enabled, false otherwise
     */
    public boolean isEnabled() {
        return this.mEnabled;
    }

    /**
     * Since the menu-chain will pass through several independent Overlays, menu IDs cannot be
     * fixed
     * at compile time. Overlays should use this method to obtain and store a menu id for each menu
     * item at construction time. This will ensure that two overlays don't use the same id.
     *
     * @return an integer suitable to be used as a menu identifier
     */
    protected static final int getSafeMenuId() {
        return sOrdinal.getAndIncrement();
    }


    /**
     * Draw the overlay over the map. This will be called on all active overlays with shadow=true,
     * to lay down the shadow layer, and then again on all overlays with shadow=false. Callers
     * should check isEnabled() before calling draw(). By default, draws nothing.
     */
    protected abstract void draw(final Canvas c, final MapView osmv, final boolean shadow);

    /**
     * Override to perform clean up of resources before shutdown. By default does nothing.
     */
    public void onDetach(final MapView mapView) {
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onKeyDown(final int keyCode, final KeyEvent event, final MapView mapView) {
        return false;
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onKeyUp(final int keyCode, final KeyEvent event, final MapView mapView) {
        return false;
    }

    /**
     * <b>You can prevent all(!) other Touch-related events from happening!</b><br />
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        return false;
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onTrackballEvent(final MotionEvent event, final MapView mapView) {
        return false;
    }

    /** GestureDetector.OnDoubleTapListener **/

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onDoubleTap(final MotionEvent e, final MapView mapView) {
        return false;
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onDoubleTapEvent(final MotionEvent e, final MapView mapView) {
        return false;
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
        return false;
    }

    /** OnGestureListener **/

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onDown(final MotionEvent e, final MapView mapView) {
        return false;
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onFling(final MotionEvent pEvent1, final MotionEvent pEvent2,
            final float pVelocityX, final float pVelocityY, final MapView pMapView) {
        return false;
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        return false;
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onScroll(final MotionEvent pEvent1, final MotionEvent pEvent2,
            final float pDistanceX, final float pDistanceY, final MapView pMapView) {
        return false;
    }

    public void onShowPress(final MotionEvent pEvent, final MapView pMapView) {
    }

    /**
     * By default does nothing (<code>return false</code>). If you handled the Event, return
     * <code>true</code>, otherwise return <code>false</code>. If you returned <code>true</code>
     * none of the following Overlays or the underlying {@link MapView} has the chance to handle
     * this event.
     */
    public boolean onSingleTapUp(final MotionEvent e, final MapView mapView) {
        return false;
    }

    /**
     * Convenience method to draw a Drawable at an offset. x and y are pixel coordinates. You can
     * find appropriate coordinates from latitude/longitude using the MapView.getProjection()
     * method
     * on the MapView passed to you in draw(Canvas, MapView, boolean).
     *
     * @param shadow If true, draw only the drawable's shadow. Otherwise, draw the drawable itself.
     */
    protected static synchronized void drawAt(final Canvas canvas, final Drawable drawable,
            final Point origin, final Point offset, final boolean shadow,
            final float aMapOrientation) {
        canvas.save();
        canvas.rotate(-aMapOrientation, origin.x, origin.y);
        canvas.translate(origin.x + offset.x, origin.y + offset.y);
        drawable.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);
        canvas.drawLine(0, -9, 0, 9, paint);
        canvas.drawLine(-9, 0, 9, 0, paint);
        canvas.drawRect(drawable.getBounds(), paint);
        canvas.restore();
    }

    /**
     * Interface definition for overlays that contain items that can be snapped to (for example,
     * when the user invokes a zoom, this could be called allowing the user to snap the zoom to an
     * interesting point.)
     */
    public interface Snappable {

        /**
         * Checks to see if the given x and y are close enough to an item resulting in snapping the
         * current action (e.g. zoom) to the item.
         *
         * @param x The x in screen coordinates.
         * @param y The y in screen coordinates.
         * @param snapPoint To be filled with the the interesting point (in screen coordinates)
         * that
         * is
         * closest to the given x and y. Can be untouched if not snapping.
         * @param mapView The {@link MapView} that is requesting the snap. Use
         * MapView.getProjection()
         * to convert between on-screen pixels and latitude/longitude pairs.
         * @return Whether or not to snap to the interesting point.
         */
        boolean onSnapToItem(int x, int y, Point snapPoint, MapView mapView);
    }
}
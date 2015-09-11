package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Overlay.Snappable;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;
import com.mapbox.mapboxsdk.views.util.Projection;
import java.util.LinkedList;

/**
 * @author Marc Kurtz
 * @author Manuel Stahl
 */
public class UserLocationOverlay extends SafeDrawOverlay implements Snappable, MapListener {

    public enum TrackingMode {
        NONE, FOLLOW, FOLLOW_BEARING
    }

    private final SafePaint mPaint = new SafePaint();
    private final SafePaint mCirclePaint = new SafePaint();
    protected final MapView mMapView;
    protected final Context mContext;

    private final MapController mMapController;
    public GpsLocationProvider mMyLocationProvider;

    private final LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
    private final PointF mMapCoords = new PointF();

    private Location mLocation;
    private LatLng mLatLng;
    private boolean mIsLocationEnabled = false;
    private boolean mDrawAccuracyEnabled = true;
    private TrackingMode mTrackingMode = TrackingMode.NONE;
    private boolean mZoomBasedOnAccuracy = true;
    private float mRequiredZoomLevel = 10;

    /**
     * Coordinates the feet of the person are located scaled for display density.
     */

    // to avoid allocations during onDraw
    private final RectF mMyLocationRect = new RectF();
    private final RectF mMyLocationPreviousRect = new RectF();

    private Bitmap mPersonBitmap;
    private Bitmap mDirectionArrowBitmap;

    private PointF mPersonHotspot;
    private PointF mDirectionHotspot;

    public void setDirectionArrowBitmap(Bitmap bitmap) {
        mDirectionArrowBitmap = bitmap;
    }

    public void setPersonBitmap(Bitmap bitmap) {
        mPersonBitmap = bitmap;
    }

    public void setDirectionArrowHotspot(PointF point) {
        mDirectionHotspot = point;
    }

    public void setPersonHotspot(PointF point) {
        mPersonHotspot = point;
    }

    public void setOverlayCircleColor(int newColor) {
        mCirclePaint.setColor(newColor);
    }

    public UserLocationOverlay(GpsLocationProvider myLocationProvider, MapView mapView, int arrowId, int personId) {
        mMapView = mapView;
        mMapController = mapView.getController();
        mContext = mapView.getContext();
        mCirclePaint.setColor(0x776464FF);
        mCirclePaint.setAntiAlias(true);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);

        mPersonHotspot = new PointF(0.5f, 0.5f);
        mDirectionHotspot = new PointF(0.5f, 0.5f);

        if (personId != 0) {
            mPersonBitmap = BitmapFactory.decodeResource(mContext.getResources(), personId);
        }
        if (arrowId != 0) {
            mDirectionArrowBitmap = BitmapFactory.decodeResource(mContext.getResources(), arrowId);
        }

        setMyLocationProvider(myLocationProvider);
        setOverlayIndex(USERLOCATIONOVERLAY_INDEX);
    }

    public UserLocationOverlay(GpsLocationProvider myLocationProvider, MapView mapView) {
        this(myLocationProvider, mapView, R.drawable.direction_arrow, R.drawable.location_marker);
    }

    @Override
    public void onDetach(MapView mapView) {
        this.disableMyLocation();
        super.onDetach(mapView);
    }

    /**
     * If enabled, an accuracy circle will be drawn around your current position.
     *
     * @param drawAccuracyEnabled whether the accuracy circle will be enabled
     */
    public void setDrawAccuracyEnabled(final boolean drawAccuracyEnabled) {
        mDrawAccuracyEnabled = drawAccuracyEnabled;
    }

    /**
     * If enabled, an accuracy circle will be drawn around your current position.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isDrawAccuracyEnabled() {
        return mDrawAccuracyEnabled;
    }

    public GpsLocationProvider getMyLocationProvider() {
        return mMyLocationProvider;
    }

    protected void setMyLocationProvider(GpsLocationProvider myLocationProvider) {

        if (mMyLocationProvider != null) {
            mMyLocationProvider.stopLocationProvider();
        }

        mMyLocationProvider = myLocationProvider;
    }

    protected void drawMyLocation(final ISafeCanvas canvas, final MapView mapView, final Location lastFix) {

        final Rect mapBounds = new Rect(0, 0, mapView.getMeasuredWidth(), mapView.getMeasuredHeight());
        final Projection projection = mapView.getProjection();
        Rect rect = new Rect();
        getDrawingBounds(projection, lastFix, null).round(rect);
        if (!Rect.intersects(mapBounds, rect)) {
            //dont draw item if offscreen
            return;
        }
        projection.toMapPixels(mLatLng, mMapCoords);
        final float mapScale = 1 / mapView.getScale();

        canvas.save();

        canvas.scale(mapScale, mapScale, mMapCoords.x, mMapCoords.y);

        if (mDrawAccuracyEnabled) {
            final float radius = lastFix.getAccuracy() / (float) Projection.groundResolution(
                    lastFix.getLatitude(), mapView.getZoomLevel()) * mapView.getScale();
            canvas.save();
            // Rotate the icon
            canvas.rotate(lastFix.getBearing(), mMapCoords.x, mMapCoords.y);
            // Counteract any scaling that may be happening so the icon stays the same size

            mCirclePaint.setAlpha(50);
            mCirclePaint.setStyle(Style.FILL);
            canvas.drawCircle(mMapCoords.x, mMapCoords.y, radius, mCirclePaint);

            mCirclePaint.setAlpha(150);
            mCirclePaint.setStyle(Style.STROKE);
            canvas.drawCircle(mMapCoords.x, mMapCoords.y, radius, mCirclePaint);
            canvas.restore();
        }

        if (UtilConstants.DEBUGMODE) {
            final float tx = (mMapCoords.x + 50);
            final float ty = (mMapCoords.y - 20);
            canvas.drawText("Lat: " + lastFix.getLatitude(), tx, ty + 5, mPaint);
            canvas.drawText("Lon: " + lastFix.getLongitude(), tx, ty + 20, mPaint);
            canvas.drawText("Alt: " + lastFix.getAltitude(), tx, ty + 35, mPaint);
            canvas.drawText("Acc: " + lastFix.getAccuracy(), tx, ty + 50, mPaint);
        }

        if (lastFix.hasBearing()) {
            canvas.save();
            // Rotate the icon
            canvas.rotate(lastFix.getBearing(), mMapCoords.x, mMapCoords.y);
            // Draw the bitmap
            canvas.translate(-mDirectionArrowBitmap.getWidth() * mDirectionHotspot.x,
                    -mDirectionArrowBitmap.getHeight() * mDirectionHotspot.y);

            canvas.drawBitmap(mDirectionArrowBitmap, mMapCoords.x, mMapCoords.y, mPaint);
            canvas.restore();
        } else {
            canvas.save();
            // Unrotate the icon if the maps are rotated so the little man stays upright
            canvas.rotate(-mMapView.getMapOrientation(), mMapCoords.x, mMapCoords.y);
            // Counteract any scaling that may be happening so the icon stays the same size
            canvas.translate(-mPersonBitmap.getWidth() * mPersonHotspot.x,
                    -mPersonBitmap.getHeight() * mPersonHotspot.y);
            // Draw the bitmap
            canvas.drawBitmap(mPersonBitmap, mMapCoords.x, mMapCoords.y, mPaint);
            canvas.restore();
        }
        canvas.restore();
    }

    public PointF getPositionOnScreen(final Projection projection, PointF reuse) {
        if (reuse == null) {
            reuse = new PointF();
        }
        projection.toPixels(mLatLng, reuse);
        return reuse;
    }

    public PointF getDrawingPositionOnScreen(final Projection projection, Location lastFix,
            PointF reuse) {
        reuse = getPositionOnScreen(projection, reuse);
        if (lastFix.hasBearing()) {
            reuse.offset(mPersonHotspot.x * mPersonBitmap.getWidth(),
                    mPersonHotspot.y * mPersonBitmap.getWidth());
        } else {
            reuse.offset(mDirectionHotspot.x * mDirectionArrowBitmap.getWidth(),
                    mDirectionHotspot.y * mDirectionArrowBitmap.getWidth());
        }
        return reuse;
    }

    protected RectF getDrawingBounds(final Projection projection, Location lastFix, RectF reuse) {
        PointF positionOnScreen = getPositionOnScreen(projection, null);
        return getDrawingBounds(positionOnScreen, lastFix, reuse);
    }

    protected RectF getDrawingBounds(PointF positionOnScreen, Location lastFix, RectF reuse) {
        if (reuse == null) {
            reuse = new RectF();
        }
        final Bitmap bitmap = lastFix.hasBearing() ? mDirectionArrowBitmap : mPersonBitmap;
        final PointF scale = lastFix.hasBearing() ? mDirectionHotspot : mPersonHotspot;
        //because of bearing and rotation
        final int w = (int) (Math.sqrt(2) * Math.max(bitmap.getWidth(), bitmap.getHeight()));
        final float x = positionOnScreen.x - scale.x * w;
        final float y = positionOnScreen.y - scale.y * w;
        reuse.set(x, y, x + w, y + w);

        return reuse;
    }

    protected RectF getMyLocationMapDrawingBounds(MapView mv, Location lastFix, RectF reuse) {
        mv.getProjection().toMapPixels(mLatLng, mMapCoords);
        reuse = getDrawingBounds(mMapCoords, lastFix, reuse);
        // Add in the accuracy circle if enabled
        if (mDrawAccuracyEnabled) {
            final float radius = (float) Math.ceil(
                    lastFix.getAccuracy() / (float) Projection.groundResolution(
                            lastFix.getLatitude(), mMapView.getZoomLevel())
            );
            RectF accuracyRect =
                    new RectF(mMapCoords.x - radius, mMapCoords.y - radius, mMapCoords.x + radius,
                            mMapCoords.y + radius);
            final float strokeWidth = (float) Math.ceil(
                    mCirclePaint.getStrokeWidth() == 0 ? 1 : mCirclePaint.getStrokeWidth());
            accuracyRect.inset(-strokeWidth, -strokeWidth);
            reuse.union(accuracyRect);
        }

        return reuse;
    }

    @Override
    protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
        if (shadow) {
            return;
        }

        if (mLocation != null && isMyLocationEnabled()) {
            drawMyLocation(canvas, mapView, mLocation);
        }
    }

    @Override
    public boolean onSnapToItem(final int x, final int y, final Point snapPoint,
            final MapView mapView) {
        if (!isFollowLocationEnabled() && this.mLocation != null) {
            snapPoint.x = (int) mMapCoords.x;
            snapPoint.y = (int) mMapCoords.y;
            final double xDiff = x - mMapCoords.x;
            final double yDiff = y - mMapCoords.y;
            final boolean snap = xDiff * xDiff + yDiff * yDiff < 64;
            if (UtilConstants.DEBUGMODE) {
                Log.d(TAG, "snap=" + snap);
            }
            return snap;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            this.disableFollowLocation();
        }

        return super.onTouchEvent(event, mapView);
    }

    /**
     * Return a LatLng of the last known location, or null if not known.
     */
    public LatLng getMyLocation() {
        return mLatLng;
    }

    public Location getLastFix() {
        return mLocation;
    }

    /**
     * Enables "follow" functionality. The map will center on your current location and
     * automatically scroll as you move. Scrolling the map in the UI will disable.
     */
    public void enableFollowLocation() {
        if (mTrackingMode == TrackingMode.NONE) {
            mTrackingMode = TrackingMode.FOLLOW;
        }
        // set initial location when enabled
        if (isMyLocationEnabled()) {
            updateMyLocation(mMyLocationProvider.getLastKnownLocation());
        }
    }

    /**
     * Disables "follow" functionality.
     */
    public void disableFollowLocation() {
        mTrackingMode = TrackingMode.NONE;
    }


    public void setTrackingMode(TrackingMode mode) {
        mTrackingMode = mode;
        if (mTrackingMode != TrackingMode.NONE && isMyLocationEnabled()) {
            updateMyLocation(mMyLocationProvider.getLastKnownLocation());
        }
    }

    public void setRequiredZoom(final float zoomLevel) {
        mRequiredZoomLevel = zoomLevel;
        mZoomBasedOnAccuracy = false;
    }

    public TrackingMode getTrackingMode() {
        return mTrackingMode;
    }
    /**
     * If enabled, the map will center on your current location and automatically scroll as you
     * move. Scrolling the map in the UI will disable.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isFollowLocationEnabled() {
        return mTrackingMode != TrackingMode.NONE;
    }

    private void updateDrawingPositionRect() {
        getMyLocationMapDrawingBounds(mMapView, mLocation, mMyLocationRect);
    }

    private void invalidate() {
        if (mMapView == null) {
            return; //not on map yet
        }
        // Get new drawing bounds
        mMyLocationPreviousRect.set(mMyLocationRect);
        updateDrawingPositionRect();
        final RectF newRect = new RectF(mMyLocationRect);
        // If we had a previous location, merge in those bounds too
        newRect.union(mMyLocationPreviousRect);
        // Invalidate the bounds
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                mMapView.invalidateMapCoordinates(newRect);
            }
        });
    }

    public void onLocationChanged(Location location, GpsLocationProvider source) {
        // If we had a previous location, let's get those bounds
        if (mLocation != null && mLocation.getBearing() == location.getBearing() && mLocation.distanceTo(location) == 0) {
            return;
        }

        updateMyLocation(location);

        synchronized (mRunOnFirstFix) {
            for (final Runnable runnable : mRunOnFirstFix) {
                new Thread(runnable).start();
            }
            mRunOnFirstFix.clear();
        }
    }

    public boolean enableMyLocation(GpsLocationProvider myLocationProvider) {
        this.setMyLocationProvider(myLocationProvider);
        mIsLocationEnabled = false;
        return enableMyLocation();
    }

    public boolean goToMyPosition(final boolean animated) {
        if (mLocation == null) {
            return false;
        }
        float currentZoom = mMapView.getZoomLevel(false);
        if (currentZoom <= mRequiredZoomLevel) {
            double requiredZoom = mRequiredZoomLevel;
            if (mZoomBasedOnAccuracy && mMapView.isLayedOut()) {
                double delta = (mLocation.getAccuracy() / 110000) * 1.2; // approx. meter per degree latitude, plus some margin
                final Projection projection = mMapView.getProjection();
                LatLng desiredSouthWest = new LatLng(mLocation.getLatitude() - delta,
                        mLocation.getLongitude() - delta);

                LatLng desiredNorthEast = new LatLng(mLocation.getLatitude() + delta,
                        mLocation.getLongitude() + delta);

                float pixelRadius = Math.min(mMapView.getMeasuredWidth(), mMapView.getMeasuredHeight()) / 2;

                BoundingBox currentBox = projection.getBoundingBox();
                if (desiredNorthEast.getLatitude() != currentBox.getLatNorth() ||
                        desiredNorthEast.getLongitude() != currentBox.getLonEast() ||
                        desiredSouthWest.getLatitude() != currentBox.getLatSouth() ||
                        desiredSouthWest.getLongitude() != currentBox.getLonWest()) {
                    mMapView.zoomToBoundingBox(new BoundingBox(desiredNorthEast, desiredSouthWest), true, animated, true);
                }
            } else if (animated) {
                return mMapController.setZoomAnimated((float) requiredZoom, mLatLng, true, false);
            } else {
                mMapController.setZoom((float) requiredZoom, mLatLng, false);
            }
        } else if (animated) {
           return mMapController.animateTo(mLatLng);
        } else {
            return mMapController.goTo(mLatLng, new PointF(0, 0));
        }
        return true;
    }

    private void updateMyLocation(final Location location) {
        mLocation = location;
        if (mLocation == null) {
            mLatLng = null;
            return;
        }
        mLatLng = new LatLng(mLocation);
        //if goToMyPosition return false, it means we are already there
        //which means we have to invalidate ourselves to make sure we are redrawn
        if (!isFollowLocationEnabled() || !goToMyPosition(true)) {
            invalidate();
        }
    }

    /**
     * Enable receiving location updates from the provided GpsLocationProvider and show your
     * location on the maps. You will likely want to call enableMyLocation() from your Activity's
     * Activity.onResume() method, to enable the features of this overlay. Remember to call the
     * corresponding disableMyLocation() in your Activity's Activity.onPause() method to turn off
     * updates when in the background.
     */
    public boolean enableMyLocation() {
        if (mIsLocationEnabled) {
            mMyLocationProvider.stopLocationProvider();
        }

        boolean result = mMyLocationProvider.startLocationProvider(this);
        mIsLocationEnabled = result;

        // set initial location when enabled
        if (result) {
            updateMyLocation(mMyLocationProvider.getLastKnownLocation());
        }
        return result;
    }

    /**
     * Disable location updates
     */
    public void disableMyLocation() {
        mIsLocationEnabled = false;

        if (mMyLocationProvider != null) {
            mMyLocationProvider.stopLocationProvider();
        }

        // Update the screen to see changes take effect
        if (mMapView != null) {
            mMapView.postInvalidate();
        }
    }

    /**
     * If enabled, the map is receiving location updates and drawing your location on the map.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isMyLocationEnabled() {
        return mIsLocationEnabled;
    }

    public boolean runOnFirstFix(final Runnable runnable) {
        if (mMyLocationProvider != null && mLocation != null) {
            new Thread(runnable).start();
            return true;
        } else {
            synchronized (mRunOnFirstFix) {
                mRunOnFirstFix.addLast(runnable);
            }
            return false;
        }
    }

    private static final String TAG = "UserLocationOverlay";

    @Override
    public void onScroll(ScrollEvent event) {
        if (event.getUserAction()) {
            disableFollowLocation();
        }
    }

    @Override
    public void onZoom(ZoomEvent event) {
        if (event.getUserAction()) {
            disableFollowLocation();
        }
    }

    @Override
    public void onRotate(RotateEvent event) {
        if (event.getUserAction()) {
            disableFollowLocation();
        }
    }
}

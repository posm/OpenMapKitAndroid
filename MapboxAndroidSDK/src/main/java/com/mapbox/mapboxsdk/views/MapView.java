package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import com.almeros.android.multitouch.RotateGestureDetector;
import com.cocoahero.android.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GeoJSONPainter;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.ItemizedOverlay;
import com.mapbox.mapboxsdk.overlay.MapEventsOverlay;
import com.mapbox.mapboxsdk.overlay.MapEventsReceiver;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.overlay.OverlayManager;
import com.mapbox.mapboxsdk.overlay.TilesOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBasic;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.util.SimpleInvalidationHandler;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.util.NetworkUtils;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import com.mapbox.mapboxsdk.views.util.OnMapOrientationChangeListener;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.mapbox.mapboxsdk.views.util.TileLoadedListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import com.mapbox.mapboxsdk.views.util.constants.MapViewLayouts;
import org.json.JSONException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The MapView class manages all of the content and
 * state of a single map, including layers, markers,
 * and interaction code.
 */
public class MapView extends ViewGroup implements MapViewConstants, MapEventsReceiver, MapboxConstants {
    /**
     * The default marker Overlay, automatically added to the view to add markers directly.
     */
    private ItemizedIconOverlay defaultMarkerOverlay;
    /**
     * List linked to the default marker overlay.
     */
    private ArrayList<Marker> defaultMarkerList = new ArrayList<Marker>();
    /**
     * Overlay for basic map touch events.
     */
    private MapEventsOverlay eventsOverlay;
    /**
     * A copy of the app context.
     */
    private Context context;
    /**
     * Whether or not a marker has been placed already.
     */
    private boolean firstMarker = true;

    private static final String TAG = "MapBox MapView";
    private static Method sMotionEventTransformMethod;

    /**
     * Current zoom level for map tiles.
     */
    private float mZoomLevel = 11;
    protected float mRequestedMinimumZoomLevel = 0;
    private float mMinimumZoomLevel = 0;
    private float mMaximumZoomLevel = 22;

    /**
     * The MapView listener
     */
    private MapViewListener mMapViewListener;

    private final OverlayManager mOverlayManager;

    private Projection mProjection;
    private boolean mLayedOut;

    private final TilesOverlay mTilesOverlay;

    private final GestureDetector mGestureDetector;

    /**
     * Handles map scrolling
     */
    protected final Scroller mScroller;
    protected boolean mIsFlinging;

    private final AtomicInteger mTargetZoomLevel = new AtomicInteger();
    private final AtomicBoolean mIsAnimating = new AtomicBoolean(false);

    private final MapController mController;

    protected ScaleGestureDetector mScaleGestureDetector;
    protected RotateGestureDetector mRotateGestureDetector;
    protected boolean mMapRotationEnabled;
    protected OnMapOrientationChangeListener mOnMapOrientationChangeListener;

    protected float mMultiTouchScale = 1.0f;
    protected PointF mMultiTouchScalePoint = new PointF();
    protected Matrix mInvTransformMatrix = new Matrix();

    protected List<MapListener> mListeners = new ArrayList<MapListener>();

    private float mapOrientation = 0;
    private final float[] mRotatePoints = new float[2];
    private final Rect mInvalidateRect = new Rect();

    protected BoundingBox mScrollableAreaBoundingBox = null;
    protected RectF mScrollableAreaLimit = null;
    private boolean mConstraintRegionFit;
    protected RectF mTempRect = new RectF();

    private BoundingBox mBoundingBoxToZoomOn = null;
    private boolean mBoundingBoxToZoomOnRegionFit = false;

    // for speed (avoiding allocations)
    protected final MapTileLayerBase mTileProvider;

    private final Handler mTileRequestCompleteHandler;

    /* a point that will be reused to design added views */
    private final PointF mPoint = new PointF();

    private TilesLoadedListener tilesLoadedListener;
    TileLoadedListener tileLoadedListener;
    private InfoWindow currentTooltip;

    private int mDefaultPinRes = R.drawable.defpin;
    private Drawable mDefaultPinDrawable;
    private PointF mDefaultPinAnchor = DEFAULT_PIN_ANCHOR;

    private UserLocationOverlay mLocationOverlay;

    /**
     * Constructor for XML layout calls. Should not be used programmatically.
     *
     * @param aContext A copy of the app context
     * @param attrs    An AttributeSet object to get extra info from the XML, such as mapbox id or
     *                 type
     *                 of baselayer
     */
    protected MapView(final Context aContext, final int tileSizePixels,
                      MapTileLayerBase tileProvider, final Handler tileRequestCompleteHandler,
                      final AttributeSet attrs) {
        super(aContext, attrs);
        setWillNotDraw(false);
        mLayedOut = false;
        mConstraintRegionFit = false;
        this.mController = new MapController(this);
        this.mScroller = new Scroller(aContext);
        Projection.setTileSize(tileSizePixels);

        if (tileProvider == null) {
            tileProvider = new MapTileLayerBasic(aContext, null, this);
        }

        mTileRequestCompleteHandler =
                tileRequestCompleteHandler == null ? new SimpleInvalidationHandler(this)
                        : tileRequestCompleteHandler;
        mTileProvider = tileProvider;
        mTileProvider.setTileRequestCompleteHandler(mTileRequestCompleteHandler);

        mTilesOverlay = new TilesOverlay(mTileProvider);
        mOverlayManager = new OverlayManager(mTilesOverlay);

        this.mGestureDetector =
                new GestureDetector(aContext, new MapViewGestureDetectorListener(this));

        this.mScaleGestureDetector =
                new ScaleGestureDetector(aContext, new MapViewScaleGestureDetectorListener(this));
        this.mRotateGestureDetector =
                new RotateGestureDetector(aContext, new MapViewRotateGestureDetectorListener(this));
        this.context = aContext;
        eventsOverlay = new MapEventsOverlay(aContext, this);
        this.getOverlays().add(eventsOverlay);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MapView);
        String mapid = a.getString(R.styleable.MapView_mapid);
        if (!TextUtils.isEmpty(mapid)) {
            setTileSource(new MapboxTileLayer(mapid));
        } else {
            Log.w(TAG, "mapid not set.");
        }
        String centerLat = a.getString(R.styleable.MapView_centerLat);
        String centerLng = a.getString(R.styleable.MapView_centerLng);
        if (centerLat != null && centerLng != null) {
            double lat, lng;
            lat = Double.parseDouble(centerLat);
            lng = Double.parseDouble(centerLng);
            this.setCenter(new LatLng(lat, lng));
        } else {
            Log.d(TAG, "centerLatLng is not specified in XML.");
        }
        String zoomLvl = a.getString(R.styleable.MapView_zoomLevel);
        if (zoomLvl != null) {
            float lvl = Float.parseFloat(zoomLvl);
            this.setZoom(lvl);
        } else {
            Log.d(TAG, "zoomLevel is not specified in XML.");
        }
        a.recycle();
    }

    public MapView(final Context aContext) {
        this(aContext, 256, null, null, null);
    }

    public MapView(final Context aContext, AttributeSet attrs) {
        this(aContext, 256, null, null, attrs);
    }

    protected MapView(Context aContext, int tileSizePixels, MapTileLayerBase aTileProvider) {
        this(aContext, tileSizePixels, aTileProvider, null, null);
    }

    /**
     * Add a new MapListener that observes changes in this map.
     * @param listener
     */
    public void addListener(final MapListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Remove a listener object that observed changes in this map.
     * @param listener
     */
    public void removeListener(MapListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    /**
     * Add an overlay to this map. If the overlay is already included,
     * does nothing. After adding the overlay, invalidates the map to
     * redraw it.
     * @param overlay
     */
    public void addOverlay(final Overlay overlay) {
        if (!mOverlayManager.contains(overlay)) {
            mOverlayManager.add(overlay);
            if (overlay instanceof MapListener) {
                addListener((MapListener) overlay);
            }
        }
        invalidate();
    }

    /**
     * Remove an overlay from displaying in this map and invalidates
     * the map to trigger a redraw.
     * @param overlay
     */
    public void removeOverlay(final Overlay overlay) {
        if (mOverlayManager.contains(overlay)) {
            mOverlayManager.remove(overlay);
            if (overlay instanceof MapListener) {
                removeListener((MapListener) overlay);
            }
        }
        invalidate();
    }

    private void updateAfterSourceChange() {
        Projection.setTileSize(mTileProvider.getTileSizePixels());
        this.setScrollableAreaLimit(mTileProvider.getBoundingBox());
        this.setMinZoomLevel(mTileProvider.getMinimumZoomLevel());
        this.setMaxZoomLevel(mTileProvider.getMaximumZoomLevel());
        this.setZoom(mZoomLevel);
        if (!isLayedOut()) {
            return;
        }
        this.scrollTo(mDScroll.x, mDScroll.y);
        postInvalidate();
    }

    /**
     * Set the tile source of this map as an array of tile layers,
     * which will be presented on top of each other.
     * @param value
     */
    public void setTileSource(final ITileLayer[] value) {
        if (value != null && mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            ((MapTileLayerBasic) mTileProvider).setTileSources(value);
            updateAfterSourceChange();
        }
    }

    /**
     * Set the tile source of this map as a single source, and trigger
     * an update.
     * @param aTileSource
     */
    public void setTileSource(final ITileLayer aTileSource) {
        if (aTileSource != null && mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            mTileProvider.setTileSource(aTileSource);
            updateAfterSourceChange();
        }
    }

    public void addTileSource(final ITileLayer aTileSource) {
        if (aTileSource != null && mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            ((MapTileLayerBasic) mTileProvider).addTileSource(aTileSource);
            updateAfterSourceChange();
        }
    }

    public void addTileSource(final ITileLayer aTileSource, final int index) {
        if (aTileSource != null && mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            ((MapTileLayerBasic) mTileProvider).addTileSource(aTileSource, index);
            updateAfterSourceChange();
        }
    }

    public void removeTileSource(final ITileLayer aTileSource) {
        if (aTileSource != null && mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            ((MapTileLayerBasic) mTileProvider).removeTileSource(aTileSource);
            updateAfterSourceChange();
        }
    }

    public void removeTileSource(final int index) {
        if (mTileProvider != null && mTileProvider instanceof MapTileLayerBasic) {
            ((MapTileLayerBasic) mTileProvider).removeTileSource(index);
            updateAfterSourceChange();
        }
    }

    /**
     * Adds a marker to the default marker overlay.
     *
     * @param marker the marker object to be added
     * @return the marker object
     */
    public Marker addMarker(final Marker marker) {
        if (firstMarker) {
            defaultMarkerList.add(marker);
            setDefaultItemizedOverlay();
        } else {
            defaultMarkerOverlay.addItem(marker);
        }
        marker.addTo(this);
        firstMarker = false;
        return marker;
    }

    /**
     * Remove a marker from the map's display.
     */
    public void removeMarker(final Marker marker) {
        defaultMarkerList.remove(marker);
        defaultMarkerOverlay.removeItem(marker);
        this.invalidate();
    }

    /**
     * Remove all markers from the map's display.
     */
    public void clear() {
        defaultMarkerList.clear();
        if (defaultMarkerOverlay != null) {
            defaultMarkerOverlay.removeAllItems();
        }
        this.invalidate();
    }

    /**
     * Select a marker, showing a tooltip if the marker has content that would appear within it.
     */
    public void selectMarker(final Marker marker) {
        InfoWindow toolTip = marker.getToolTip(MapView.this);

        if (mMapViewListener != null) {
            mMapViewListener.onTapMarker(MapView.this, marker);
        }
        closeCurrentTooltip();
        if (toolTip != currentTooltip && marker.hasContent()) {
            if (mMapViewListener != null) {
                mMapViewListener.onShowMarker(MapView.this, marker);
            }
            currentTooltip = toolTip;
            marker.showBubble(currentTooltip, MapView.this, true);
        }
    }

    /**
     * Adds a new ItemizedOverlay to the MapView
     *
     * @param itemizedOverlay the itemized overlay
     */
    public void addItemizedOverlay(final ItemizedOverlay itemizedOverlay) {
        if (itemizedOverlay instanceof ItemizedIconOverlay) {
            // Make sure Markers are added to MapView
            ItemizedIconOverlay overlay = (ItemizedIconOverlay) itemizedOverlay;
            for (int lc = 0; lc < overlay.size(); lc++) {
                overlay.getItem(lc).addTo(this);
            }
        }

        this.getOverlays().add(itemizedOverlay);
    }

    /**
     * Get all itemized overlays on the map as an ArrayList.
     * @return
     */
    public ArrayList<ItemizedIconOverlay> getItemizedOverlays() {
        ArrayList<ItemizedIconOverlay> list = new ArrayList<ItemizedIconOverlay>();
        for (Overlay overlay : getOverlays()) {
            if (overlay instanceof ItemizedOverlay) {
                list.add((ItemizedIconOverlay) overlay);
            }
        }
        return list;
    }

    /**
     * Load and parse a GeoJSON file at a given URL
     *
     * @param URL the URL from which to load the GeoJSON file
     */
    public void loadFromGeoJSONURL(String URL) {
        if (NetworkUtils.isNetworkAvailable(getContext())) {
            new GeoJSONPainter(this, null).loadFromURL(URL);
        }
    }

    /**
     * Parse a GeoJSON file at a given URL
     * @param url The URL of GeoJSON string to parse
     * @return FeatureCollection Parsed GeoJSON
     */
    public FeatureCollection parseFromGeoJSONURL(final String url) throws IOException, JSONException {
        return DataLoadingUtils.loadGeoJSONFromUrl(url);
    }

    /**
     * Close the currently-displayed tooltip, if any.
     */
    public void closeCurrentTooltip() {
        if (currentTooltip != null) {
            if (mMapViewListener != null) {
                mMapViewListener.onHideMarker(this, currentTooltip.getBoundMarker());
            }
            currentTooltip.close();
            currentTooltip = null;
        }
    }

    /**
     * Get the current tooltip of this map if there is one being displayed.
     * @return
     */
    public InfoWindow getCurrentTooltip() {
        return currentTooltip;
    }

    /**
     * Sets the default itemized overlay.
     */
    private void setDefaultItemizedOverlay() {
        defaultMarkerOverlay = new ItemizedIconOverlay(getContext(), defaultMarkerList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectMarker(item);
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        if (mMapViewListener != null) {
                            mMapViewListener.onLongPressMarker(MapView.this, item);
                        }
                        return true;
                    }
                }
        );
        addItemizedOverlay(defaultMarkerOverlay);
    }

    /**
     * @param p the position where the event occurred.
     * @return whether the event action is triggered or not
     */
    public boolean singleTapUpHelper(final ILatLng p) {
        closeCurrentTooltip();
        onTap(p);
        return true;
    }

    /**
     * @param p the position where the event occurred.
     * @return whether the event action is triggered or not
     */
    public boolean longPressHelper(final ILatLng p) {
        onLongPress(p);
        return false;
    }

    public void onLongPress(final ILatLng p) {
        if (mMapViewListener != null) {
            mMapViewListener.onLongPressMap(MapView.this, p);
        }
    }

    public void onTap(final ILatLng p) {
        if (mMapViewListener != null) {
            mMapViewListener.onTapMap(MapView.this, p);
        }
    }

    /**
     * Returns the map's controller
     */
    public MapController getController() {
        return this.mController;
    }

    /**
     * Returns the map's overlay
     */
    public TilesOverlay getMapOverlay() {
        return mTilesOverlay;
    }

    /**
     * You can add/remove/reorder your Overlays using the List of {@link Overlay}. The first (index
     * 0) Overlay gets drawn first, the one with the highest as the last one.
     */
    public List<Overlay> getOverlays() {
        return this.getOverlayManager();
    }

    public OverlayManager getOverlayManager() {
        return mOverlayManager;
    }

    public MapTileLayerBase getTileProvider() {
        return mTileProvider;
    }

    /**
     * Returns the map's scroller
     */
    public Scroller getScroller() {
        return mScroller;
    }

    public Handler getTileRequestCompleteHandler() {
        return mTileRequestCompleteHandler;
    }

    public BoundingBox getBoundingBoxInternal() {
        if (getMeasuredWidth() == 0 || getMeasuredHeight() == 0) {
            return null;
        }
        final Rect screenRect = GeometryMath.viewPortRect(getProjection(), null);
        ILatLng neGeoPoint =
                Projection.pixelXYToLatLong(screenRect.right, screenRect.top, mZoomLevel);
        ILatLng swGeoPoint =
                Projection.pixelXYToLatLong(screenRect.left, screenRect.bottom, mZoomLevel);

        return new BoundingBox(neGeoPoint.getLatitude(), neGeoPoint.getLongitude(),
                swGeoPoint.getLatitude(), swGeoPoint.getLongitude());
    }

    /**
     * Returns the current bounding box of the map.
     */
    public BoundingBox getBoundingBox() {
        return getProjection().getBoundingBox();
    }

    /**
     * Get centerpoint of the phone as latitude and longitude.
     *
     * @return centerpoint
     */
    public LatLng getCenter() {
        final int worldSize_current_2 = Projection.mapSize(mZoomLevel) >> 1;
        return Projection.pixelXYToLatLong((float) mDScroll.x
                        + worldSize_current_2, (float) mDScroll.y + worldSize_current_2,
                mZoomLevel
        );
    }

    public Rect getIntrinsicScreenRect(Rect reuse) {
        if (reuse == null) {
            reuse = new Rect();
        }
        final int width_2 = getMeasuredWidth() >> 1;
        final int height_2 = getMeasuredHeight() >> 1;
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        reuse.set(scrollX - width_2, scrollY - height_2, scrollX + width_2, scrollY + height_2);
        return reuse;
    }

    /**
     * Get a projection for converting between screen-pixel coordinates and latitude/longitude
     * coordinates. You should not hold on to this object for more than one draw, since the
     * projection of the map could change.
     *
     * @return The Projection of the map in its current state. You should not hold on to this object
     * for more than one draw, since the projection of the map could change.
     */
    public Projection getProjection() {
        if (mProjection == null) {
            mProjection = new Projection(this);
        }
        return mProjection;
    }

    /**
     * Set the centerpoint of the map view, given a latitude and
     * longitude position.
     *
     * @return the map view, for chaining
     */
    public MapView setCenter(final ILatLng aCenter) {
        return setCenter(aCenter, false);
    }

    public MapView setCenter(final ILatLng aCenter, final boolean userAction) {
        getController().setCurrentlyInUserAction(userAction);
        getController().setCenter(aCenter);
        getController().setCurrentlyInUserAction(false);
        return this;
    }


    /**
     * Pan the map by a given number of pixels in the x and y dimensions.
     */
    public MapView panBy(int x, int y) {
        this.mController.panBy(x, y);
        return this;
    }

    public MapView setScale(float scale) {
        float zoomDelta = (float) (Math.log(scale) / Math.log(2d));
        float newZoom = mZoomLevel + zoomDelta;
        if (newZoom <= mMaximumZoomLevel && newZoom >= mMinimumZoomLevel) {
            mMultiTouchScale = scale;
            updateInversedTransformMatrix();
            invalidate();
        }
        return this;
    }

    public float getScale() {
        return mMultiTouchScale;
    }

    private final void updateInversedTransformMatrix() {
        mInvTransformMatrix.reset();
        mInvTransformMatrix.preScale(1 / mMultiTouchScale, 1 / mMultiTouchScale, mMultiTouchScalePoint.x,
                mMultiTouchScalePoint.y);
    }

    public final Matrix getInversedTransformMatrix() {
        return mInvTransformMatrix;
    }


    private void snapItems() {
        // snap for all snappables
        final Point snapPoint = new Point();
        if (this.getOverlayManager().onSnapToItem(getScrollX(), getScrollY(), snapPoint, this)) {
            scrollTo(snapPoint.x, snapPoint.y);
        }
    }

    /**
     * @param aZoomLevel the zoom level bound by the tile source
     * @return the map view, for chaining
     */
    public MapView setZoom(final float aZoomLevel) {
        return this.mController.setZoom(aZoomLevel);
    }

    protected MapView setZoomInternal(final float aZoomLevel) {
        return setZoomInternal(aZoomLevel, null, null);
    }

    protected MapView setZoomInternal(final float aZoomLevel, ILatLng center, final PointF decale) {

        if (center == null) {
            center = getCenter();
        }

        final float newZoomLevel = getClampedZoomLevel(aZoomLevel);
        final float curZoomLevel = this.mZoomLevel;

        // reset the touchScale because from now on the zoom is the new one
        mMultiTouchScale = 1.0f;
        mInvTransformMatrix.reset();

        if (newZoomLevel != curZoomLevel) {
            this.mZoomLevel = newZoomLevel;
            // just to be sure any one got the right one
            setAnimatedZoom(this.mZoomLevel);
            mScroller.forceFinished(true);
            mIsFlinging = false;
            updateScrollableAreaLimit();
        }

        if (center != null) {
            // we cant use the mProjection because the values are not the right
            // one yet
            final PointF centerPoint = Projection.toMapPixels(
                    center.getLatitude(), center.getLongitude(), newZoomLevel,
                    mDScroll.x, mDScroll.y, null);
            if (decale != null) {
                centerPoint.offset(decale.x, decale.y);
            }
            scrollTo(centerPoint.x, centerPoint.y);
        } else {
            if (newZoomLevel > curZoomLevel) {
                // We are going from a lower-resolution plane to a higher-resolution plane, so we have
                // to do it the hard way.
                final int worldSize_new_2 = Projection.mapSize(newZoomLevel) >> 1;
                final ILatLng centerGeoPoint = getCenter();
                final PointF centerPoint = Projection.latLongToPixelXY(centerGeoPoint.getLatitude(),
                        centerGeoPoint.getLongitude(), newZoomLevel, null);
                scrollTo((int) centerPoint.x - worldSize_new_2, (int) centerPoint.y - worldSize_new_2);
            } else if (newZoomLevel < curZoomLevel) {
                // We are going from a higher-resolution plane to a lower-resolution plane, so we can do
                // it the easy way.
                scrollTo((int) (GeometryMath.rightShift(getScrollX(), curZoomLevel - newZoomLevel)),
                        (int) (GeometryMath.rightShift(getScrollY(), curZoomLevel - newZoomLevel)));
            }
        }


        mProjection = new Projection(this);
        // snap for all snappables
        snapItems();

        if (isLayedOut()) {
            getMapOverlay().rescaleCache(newZoomLevel, curZoomLevel, getProjection());
        }

        // do callback on listener
        if (newZoomLevel != curZoomLevel && mListeners.size() > 0) {
            final ZoomEvent event = new ZoomEvent(this, newZoomLevel, mController.currentlyInUserAction());
            for (MapListener listener : mListeners) {
                listener.onZoom(event);
            }
        }

        // Allows any views fixed to a Location in the MapView to adjust
        this.requestLayout();
        return this;
    }

    /**
     * compute the minimum zoom necessary to show a BoundingBox
     *
     * @param boundingBox the box to compute the zoom for
     * @param regionFit   if true computed zoom will make sure the whole box is visible
     * @param roundedZoom if true the required zoom will be rounded (for better
     *                    graphics)
     * @return the minimum zoom necessary to show the bounding box
     */
    private double minimumZoomForBoundingBox(final BoundingBox boundingBox,
                                             final boolean regionFit, final boolean roundedZoom) {
        final RectF rect = Projection.toMapPixels(boundingBox,
                TileLayerConstants.MAXIMUM_ZOOMLEVEL, mTempRect);
        final float requiredLatitudeZoom = TileLayerConstants.MAXIMUM_ZOOMLEVEL
                - (float) ((Math.log(rect.height() / getMeasuredHeight()) / Math
                .log(2)));
        final float requiredLongitudeZoom = TileLayerConstants.MAXIMUM_ZOOMLEVEL
                - (float) ((Math.log(rect.width() / getMeasuredWidth()) / Math
                .log(2)));
        double result = regionFit ? Math.min(requiredLatitudeZoom,
                requiredLongitudeZoom) : Math.max(requiredLatitudeZoom,
                requiredLongitudeZoom);
        if (roundedZoom) {
            result = regionFit ? Math.floor(result) : Math.round(result);
        }
        return result;
    }

    /**
     * Zoom the map to enclose the specified bounding box, as closely as
     * possible.
     *
     * @param boundingBox the box to compute the zoom for
     * @param regionFit   if true computed zoom will make sure the whole box is visible
     * @param animated    if true the zoom will be animated
     * @param roundedZoom if true the required zoom will be rounded (for better
     *                    graphics)
     * @param userAction  set to true if it comes from a userAction
     * @return the map view, for chaining
     */
    public MapView zoomToBoundingBox(final BoundingBox boundingBox,
                                     final boolean regionFit, final boolean animated,
                                     final boolean roundedZoom, final boolean userAction) {
        BoundingBox inter = (mScrollableAreaBoundingBox != null) ? mScrollableAreaBoundingBox
                .intersect(boundingBox) : boundingBox;
        if (inter == null || !inter.isValid()) {
            return this;
        }
        if (!mLayedOut) {
            mBoundingBoxToZoomOn = inter;
            mBoundingBoxToZoomOnRegionFit = regionFit;
            return this;
        }

        // Zoom to boundingBox center, at calculated maximum allowed zoom level
        final LatLng center = inter.getCenter();
        final float zoom = (float) minimumZoomForBoundingBox(inter, regionFit,
                roundedZoom);

        if (animated) {
            getController().setZoomAnimated(zoom, center, true, userAction);
        } else {
            getController().setZoom(zoom, center, userAction);
        }
        return this;
    }

    /**
     * Zoom the map to enclose the specified bounding box, as closely as
     * possible.
     *
     * @param boundingBox the box to compute the zoom for
     * @param regionFit   if true computed zoom will make sure the whole box is visible
     * @param animated    if true the zoom will be animated
     * @param roundedZoom if true the required zoom will be rounded (for better
     *                    graphics)
     * @return the map view, for chaining
     */
    public MapView zoomToBoundingBox(final BoundingBox boundingBox,
                                     final boolean regionFit, final boolean animated,
                                     final boolean roundedZoom) {
        return zoomToBoundingBox(boundingBox, regionFit, animated, roundedZoom,
                false);
    }

    /**
     * Zoom the map to enclose the specified bounding box, as closely as
     * possible.
     *
     * @param boundingBox the box to compute the zoom for
     * @param regionFit   if true computed zoom will make sure the whole box is visible
     * @param animated    if true the zoom will be animated
     * @return the map view, for chaining
     */
    public MapView zoomToBoundingBox(final BoundingBox boundingBox,
                                     final boolean regionFit, final boolean animated) {
        return zoomToBoundingBox(boundingBox, regionFit, animated, false, false);
    }

    /**
     * Zoom the map to enclose the specified bounding box, as closely as
     * possible.
     *
     * @param boundingBox the box to compute the zoom for
     * @param regionFit   if true computed zoom will make sure the whole box is visible
     * @return the map view, for chaining
     */
    public MapView zoomToBoundingBox(final BoundingBox boundingBox,
                                     final boolean regionFit) {
        return zoomToBoundingBox(boundingBox, regionFit, false, false);
    }

    /**
     * Zoom the map to enclose the specified bounding box, as closely as
     * possible.
     *
     * @param boundingBox the box to compute the zoom for
     * @return the map view, for chaining
     */
    public MapView zoomToBoundingBox(final BoundingBox boundingBox) {
        return zoomToBoundingBox(boundingBox, false);
    }

    public float getClampedZoomLevel(float zoom) {
        final float minZoomLevel = getMinZoomLevel();
        final float maxZoomLevel = getMaxZoomLevel();

        return Math.max(minZoomLevel, Math.min(maxZoomLevel, zoom));
    }

    /**
     * Get the current ZoomLevel for the map tiles.
     *
     * @return the current ZoomLevel between 0 (equator) and 18/19(closest), depending on the tile
     * source chosen.
     */
    public float getZoomLevel() {
        return getZoomLevel(true);
    }

    protected float getAnimatedZoom() {
        return Float.intBitsToFloat(mTargetZoomLevel.get());
    }
    protected void setAnimatedZoom(float value) {
        mTargetZoomLevel.set(Float.floatToIntBits(value));
    }

    protected void clearAnimatedZoom(float value) {
        Float.floatToIntBits(-1);
    }

    protected boolean isAnimatedZoomSet() {
        return  Float.intBitsToFloat(mTargetZoomLevel.get()) != -1;
    }

    /**
     * Get the current ZoomLevel for the map tiles.
     *
     * @param aPending if true and we're animating then return the zoom level that we're animating
     *                 towards, otherwise return the current zoom level
     * @return the zoom level
     */
    public float getZoomLevel(final boolean aPending) {
        if (aPending && isAnimating()) {
            return getAnimatedZoom();
        } else {
            return mZoomLevel;
        }
    }

    /**
     * Get the minimum allowed zoom level for the maps.
     */
    public float getMinZoomLevel() {
        return Math.max(mMinimumZoomLevel, 0);
    }

    /**
     * Get the maximum allowed zoom level for the maps.
     */
    public float getMaxZoomLevel() {
        return mMaximumZoomLevel;
    }

    /**
     * Set the minimum allowed zoom level, or pass null to use the minimum zoom level from the tile
     * provider.
     */
    public void setMinZoomLevel(float zoomLevel) {
        mRequestedMinimumZoomLevel = mMinimumZoomLevel = zoomLevel;
        updateMinZoomLevel();
    }

    /**
     * Set the maximum allowed zoom level, or pass null to use the maximum zoom level from the tile
     * provider.
     */
    public void setMaxZoomLevel(float zoomLevel) {
        mMaximumZoomLevel = zoomLevel;
    }

    /**
     * Determine whether the map is at its maximum zoom
     *
     * @return whether the map can zoom in
     */
    protected boolean canZoomIn() {
        final float maxZoomLevel = getMaxZoomLevel();
        if ((isAnimating() ? getAnimatedZoom() : mZoomLevel) >= maxZoomLevel) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether the map is at its minimum zoom
     *
     * @return whether the map can zoom out
     */
    protected boolean canZoomOut() {
        final float minZoomLevel = getMinZoomLevel();
        if ((isAnimating() ? getAnimatedZoom() : mZoomLevel) <= minZoomLevel) {
            return false;
        }
        return true;
    }

    /**
     * Zoom in by one zoom level.
     */
    public boolean zoomIn() {
        return getController().zoomIn();
    }


    public boolean zoomInFixing(final ILatLng point, final boolean userAction) {
        return getController().zoomInAbout(point, userAction);
    }

    public boolean zoomInFixing(final ILatLng point) {
        return zoomInFixing(point, false);
    }

    /**
     * Zoom out by one zoom level.
     */
    public boolean zoomOut() {
        return getController().zoomOut();
    }

    public boolean zoomOutFixing(final ILatLng point, final boolean userAction) {
        return getController().zoomOutAbout(point, userAction);
    }

    public boolean zoomOutFixing(final ILatLng point) {
        return zoomOutFixing(point, false);
    }

    /**
     * Set the rotation of the map, in degrees. A value of 0, meaning straight up, is default.
     *
     * @param degrees the angle of the map
     */
    public void setMapOrientation(float degrees) {
        this.mapOrientation = degrees % 360.0f;
        this.mProjection = null;
        this.invalidate();
    }

    /**
     * Gets the current angle of rotation of the map
     *
     * @return the current angle in degrees.
     */
    public float getMapOrientation() {
        return mapOrientation;
    }

    /**
     * Gets whether the current map rotation feature is enabled or not
     * default: disabled
     */
    public boolean isMapRotationEnabled() {
        return mMapRotationEnabled;
    }

    /**
     * Sets whether to enable or disable the map rotation features
     * default: disabled
     */
    public void setMapRotationEnabled(boolean enable) {
        mMapRotationEnabled = enable;
    }

    /**
     * Gets the mapView onMapOrientationChangeListener
     * @return the onMapOrientationChangeListener
     */
    public OnMapOrientationChangeListener getOnMapOrientationChangeListener() {
        return mOnMapOrientationChangeListener;
    }

    /**
     * Gets the mapView onMapOrientationChangeListener
     * @Param l the onMapOrientationChangeListener
     */
    public void setOnMapOrientationChangeListener(OnMapOrientationChangeListener l) {
        this.mOnMapOrientationChangeListener = l;
    }

    /**
     * Whether to use the network connection if it's available.
     */
    public boolean useDataConnection() {
        return mTilesOverlay.useDataConnection();
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param aMode if true use the network connection if it's available. if false don't use the
     *              network connection even if it's available.
     */
    public void setUseDataConnection(final boolean aMode) {
        mTilesOverlay.setUseDataConnection(aMode);
    }

    private void updateMinZoomLevel() {
        if (mScrollableAreaBoundingBox == null || !mLayedOut) {
            return;
        }
        mMinimumZoomLevel = (float) Math.max(
                mRequestedMinimumZoomLevel,
                minimumZoomForBoundingBox(mScrollableAreaBoundingBox, mConstraintRegionFit,
                        false)
        );
        if (mZoomLevel < mMinimumZoomLevel) {
            setZoom(mMinimumZoomLevel);
        }
    }

    /**
     * Everytime we update the zoom or the view size we must re compute the real scrollable area
     * limit in pixels
     */
    public void updateScrollableAreaLimit() {
        if (mScrollableAreaBoundingBox == null || !isLayedOut()) {
            return;
        }
        if (mScrollableAreaLimit == null) {
            mScrollableAreaLimit = new RectF();
        }
        Projection.toMapPixels(mScrollableAreaBoundingBox, getZoomLevel(false),
                mScrollableAreaLimit);
//        if (mConstraintRegionFit) {
//            int width = getMeasuredWidth();
//            int height = getMeasuredHeight();
//            float ratioX = mScrollableAreaLimit.width() / (float) width;
//            float ratioY = mScrollableAreaLimit.height() / (float) height;
//
//            if (ratioX != ratioY) {
//                if (ratioX < ratioY)
//                {
//                    float newWidth_2 = mScrollableAreaLimit.height() * width / (float) height / 2;
//                    float centerX = mScrollableAreaLimit.centerX();
//                    mScrollableAreaLimit.set(centerX - newWidth_2, mScrollableAreaLimit.top, centerX + newWidth_2, mScrollableAreaLimit.bottom);
//                } else {
//                    float newHeight_2 = width * ratioX / 2;
//                    float centerY = mScrollableAreaLimit.centerY();
//                    mScrollableAreaLimit.set(mScrollableAreaLimit.left, centerY - newHeight_2, mScrollableAreaLimit.right, centerY + newHeight_2);
//                }
//            }
//
//        }

    }

    /**
     * Set the map to limit it's scrollable view to the specified BoundingBox. Note this does not
     * limit zooming so it will be possible for the user to zoom to an area that is larger than the
     * limited area.
     *
     * @param boundingBox A lat/long bounding box to limit scrolling to, or null to remove any
     *                    scrolling
     *                    limitations
     */
    public void setScrollableAreaLimit(BoundingBox boundingBox) {

        mScrollableAreaBoundingBox = boundingBox;

        // Clear scrollable area limit if null passed.
        if (mScrollableAreaBoundingBox == null) {
            mMinimumZoomLevel = mRequestedMinimumZoomLevel;
            mScrollableAreaLimit = null;
        } else {
            updateScrollableAreaLimit();
            updateMinZoomLevel();
        }
    }

    /**
     * Returns if the map can go to a specified geo point
     */
    public boolean canGoTo(ILatLng point) {
        return (mScrollableAreaBoundingBox == null || mScrollableAreaBoundingBox.contains(point));
    }

    /**
     * Returns if the map can go to a specified point (in map coordinates)
     */
    public boolean canGoTo(final float x, final float y) {
        return (mScrollableAreaLimit == null || mScrollableAreaLimit.contains(x, y));
    }

    /**
     * Returns the map current scrollable bounding box
     */
    public BoundingBox getScrollableAreaBoundingBox() {
        return mScrollableAreaBoundingBox;
    }

    /**
     * Returns the map current scrollable bounding limit int map PX
     */
    public RectF getScrollableAreaLimit() {
        return mScrollableAreaLimit;
    }


    /**
     * Returns true if the view has been layed out
     */
    public boolean isLayedOut() {
        return mLayedOut;
    }

    public void invalidateMapCoordinates(final Rect dirty) {
        mInvalidateRect.set(dirty);
        final int width_2 = this.getWidth() / 2;
        final int height_2 = this.getHeight() / 2;

        // Since the canvas is shifted by getWidth/2, we can just return our natural scrollX/Y value
        // since that is the same as the shifted center.
        int centerX = this.getScrollX();
        int centerY = this.getScrollY();

        if (this.getMapOrientation() != 0) {
            GeometryMath.getBoundingBoxForRotatedRectangle(mInvalidateRect, centerX, centerY,
                    this.getMapOrientation() + 180, mInvalidateRect);
        }
        mInvalidateRect.offset(width_2, height_2);

        super.invalidate(mInvalidateRect);
    }

    public void invalidateMapCoordinates(final RectF dirty) {
        dirty.roundOut(mInvalidateRect);
        final int width_2 = this.getWidth() / 2;
        final int height_2 = this.getHeight() / 2;

        // Since the canvas is shifted by getWidth/2, we can just return our natural scrollX/Y value
        // since that is the same as the shifted center.
        int centerX = this.getScrollX();
        int centerY = this.getScrollY();

        if (this.getMapOrientation() != 0) {
            GeometryMath.getBoundingBoxForRotatedRectangle(mInvalidateRect, centerX, centerY,
                    this.getMapOrientation() + 180, mInvalidateRect);
        }
        mInvalidateRect.offset(width_2, height_2);

        super.invalidate(mInvalidateRect);
    }

    /**
     * Returns a set of layout parameters with a width of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}, a height of
     * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} at the {@link
     * com.mapbox.mapboxsdk.geometry.LatLng} (0, 0) align
     * with {@link MapView.LayoutParams#BOTTOM_CENTER}.
     */
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER, 0,
                0);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(final AttributeSet attrs) {
        return new MapView.LayoutParams(getContext(), attrs);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
        return p instanceof MapView.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
        return new MapView.LayoutParams(p);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int count = getChildCount();

        int maxHeight = 0;
        int maxWidth = 0;

        // Find out how big everyone wants to be
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        final Projection projection = getProjection();
        // Find rightmost and bottom-most child
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                final MapView.LayoutParams lp = (MapView.LayoutParams) child.getLayoutParams();
                final int childHeight = child.getMeasuredHeight();
                final int childWidth = child.getMeasuredWidth();
                projection.toMapPixels(lp.geoPoint, mPoint);
                final int x = (int) mPoint.x + getWidth() / 2;
                final int y = (int) mPoint.y + getHeight() / 2;
                int childRight = x;
                int childBottom = y;
                switch (lp.alignment) {
                    case MapView.LayoutParams.TOP_LEFT:
                        childRight = x + childWidth;
                        childBottom = y;
                        break;
                    case MapView.LayoutParams.TOP_CENTER:
                        childRight = x + childWidth / 2;
                        childBottom = y;
                        break;
                    case MapView.LayoutParams.TOP_RIGHT:
                        childRight = x;
                        childBottom = y;
                        break;
                    case MapView.LayoutParams.CENTER_LEFT:
                        childRight = x + childWidth;
                        childBottom = y + childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER:
                        childRight = x + childWidth / 2;
                        childBottom = y + childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER_RIGHT:
                        childRight = x;
                        childBottom = y + childHeight / 2;
                        break;
                    case MapView.LayoutParams.BOTTOM_LEFT:
                        childRight = x + childWidth;
                        childBottom = y + childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_CENTER:
                        childRight = x + childWidth / 2;
                        childBottom = y + childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_RIGHT:
                        childRight = x;
                        childBottom = y + childHeight;
                        break;
                }
                childRight += lp.offsetX;
                childBottom += lp.offsetY;

                maxWidth = Math.max(maxWidth, childRight);
                maxHeight = Math.max(maxHeight, childBottom);
            }
        }

        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        // Check against minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                resolveSize(maxHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0) {
            mProjection = null;
            if (!mLayedOut) {
                mLayedOut = true;
                //first layout: if some actions were triggered before, they were enqueued
                //let's trigger them again!
                mController.mapViewLayedOut();
            }
            updateScrollableAreaLimit();
            updateMinZoomLevel();

            if (mBoundingBoxToZoomOn != null) {
                zoomToBoundingBox(mBoundingBoxToZoomOn, mBoundingBoxToZoomOnRegionFit);
                mBoundingBoxToZoomOn = null;
            }
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
                            final int b) {
        final int count = getChildCount();

        final Projection projection = getProjection();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {

                final MapView.LayoutParams lp = (MapView.LayoutParams) child.getLayoutParams();
                final int childHeight = child.getMeasuredHeight();
                final int childWidth = child.getMeasuredWidth();
                projection.toMapPixels(lp.geoPoint, mPoint);
                final int x = (int) mPoint.x + getWidth() / 2;
                final int y = (int) mPoint.y + getHeight() / 2;
                int childLeft = x;
                int childTop = y;
                switch (lp.alignment) {
                    case MapView.LayoutParams.TOP_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.TOP_CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.TOP_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y;
                        break;
                    case MapView.LayoutParams.CENTER_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.CENTER_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y - childHeight / 2;
                        break;
                    case MapView.LayoutParams.BOTTOM_LEFT:
                        childLeft = getPaddingLeft() + x;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_CENTER:
                        childLeft = getPaddingLeft() + x - childWidth / 2;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                    case MapView.LayoutParams.BOTTOM_RIGHT:
                        childLeft = getPaddingLeft() + x - childWidth;
                        childTop = getPaddingTop() + y - childHeight;
                        break;
                }
                childLeft += lp.offsetX;
                childTop += lp.offsetY;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
        }
    }

    public void onDetach() {
        this.getOverlayManager().onDetach(this);
        mTileProvider.detach();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        final boolean result = this.getOverlayManager().onKeyDown(keyCode, event, this);

        return result || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        final boolean result = this.getOverlayManager().onKeyUp(keyCode, event, this);

        return result || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTrackballEvent(final MotionEvent event) {

        if (this.getOverlayManager().onTrackballEvent(event, this)) {
            return true;
        }

        scrollBy((int) (event.getX() * 25), (int) (event.getY() * 25));

        return super.onTrackballEvent(event);
    }

    private boolean canTapTwoFingers = false;
    private int multiTouchDownCount = 0;

    private boolean handleTwoFingersTap(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    multiTouchDownCount = 0;
                    break;
                case MotionEvent.ACTION_UP:
                    if (!isAnimating() && canTapTwoFingers) {
                        final ILatLng center =
                                getProjection().fromPixels(event.getX(), event.getY());
                        mController.zoomOutAbout(center);
                        canTapTwoFingers = false;
                        multiTouchDownCount = 0;
                        return true;
                    }
                    canTapTwoFingers = false;
                    multiTouchDownCount = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    multiTouchDownCount++;
                    canTapTwoFingers = multiTouchDownCount > 1;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    multiTouchDownCount--;
                    //                    canTapTwoFingers = multiTouchDownCount > 1;
                    break;
                default:
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If map rotation is enabled, propagate onTouchEvent to the rotate gesture detector
        if (mMapRotationEnabled) {
            mRotateGestureDetector.onTouchEvent(event);
        }
        // Get rotated event for some touch listeners.
        MotionEvent rotatedEvent = rotateTouchEvent(event);

        try {
            if (this.getOverlayManager().onTouchEvent(rotatedEvent, this)) {
                Log.d(TAG, "OverlayManager handled onTouchEvent");
                return true;
            }

            // can't use the scale detector's onTouchEvent() result as it always returns true (Android issue #42591)
            //Android seems to be able to recognize a scale with one pointer ...
            // what a smart guy... let's prevent this
            if (rotatedEvent.getPointerCount() != 1) {
                mScaleGestureDetector.onTouchEvent(rotatedEvent);
            }
            boolean result = mScaleGestureDetector.isInProgress();
            if (!result) {
                result = mGestureDetector.onTouchEvent(rotatedEvent);
            } else {
                //needs to cancel two fingers tap
                canTapTwoFingers = false;
            }
            //handleTwoFingersTap should always be called because it counts pointers up/down
            result |= handleTwoFingersTap(rotatedEvent);

            return result;
        } finally {
            if (rotatedEvent != event) {
                rotatedEvent.recycle();
            }
        }
    }

    private MotionEvent rotateTouchEvent(MotionEvent ev) {
        if (this.getMapOrientation() == 0) {
            return ev;
        }
        MotionEvent rotatedEvent = MotionEvent.obtain(ev);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mRotatePoints[0] = ev.getX();
            mRotatePoints[1] = ev.getY();
            getProjection().rotatePoints(mRotatePoints);
            rotatedEvent.setLocation(mRotatePoints[0], mRotatePoints[1]);
        } else {
            // This method is preferred since it will rotate historical touch events too
            try {
                if (sMotionEventTransformMethod == null) {
                    sMotionEventTransformMethod = MotionEvent.class.getDeclaredMethod("transform",
                            new Class[]{Matrix.class});
                }
                sMotionEventTransformMethod.invoke(rotatedEvent,
                        getProjection().getRotationMatrix());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rotatedEvent;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScroller.isFinished()) {
                // One last scrollTo to get to the final destination
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                // snapping-to any Snappable points.
                if (!isAnimating()) {
                    snapItems();
                }
                mIsFlinging = false;
            } else {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate(); // Keep on drawing until the animation has
            // finished.
        }
    }

    public void updateScrollDuringAnimation() {
        // updateScrollableAreaLimit();
        // scrollTo(getScrollX(), getScrollY());
    }

    private PointF mDScroll = new PointF();

    public final PointF getScrollPoint() {
        return mDScroll;
    }

    public final void setScrollPoint(final PointF point) {
        scrollTo((double) point.x, (double) point.y);
    }

    public final PointF getScalePoint() {
        return mMultiTouchScalePoint;
    }

    public final void setScalePoint(final PointF point) {
        mMultiTouchScalePoint.set(point);
        updateInversedTransformMatrix();
    }

    @Override
    public void scrollTo(int x, int y) {
        // a trick for everyone to go through the double version of the method
        scrollTo((double) x, (double) y);
    }

    public void scrollBy(double x, double y) {
        scrollTo(mDScroll.x + x, mDScroll.y + y);
    }

    public void scrollTo(double x, double y) {
        if (mScrollableAreaLimit != null) {
            final RectF currentLimit = mScrollableAreaLimit;

            final double xToTestWith = x;
            final double yToTestWith = y;
            final float width_2 = this.getMeasuredWidth() / 2;
            final float height_2 = this.getMeasuredHeight() / 2;
            // Adjust if we are outside the scrollable area
            if (currentLimit.width() <= width_2 * 2) {
                x = currentLimit.centerX();
            } else if (xToTestWith - width_2 < currentLimit.left) {
                x = (currentLimit.left + width_2);
            } else if (xToTestWith + width_2 > currentLimit.right) {
                x = (currentLimit.right - width_2);
            }

            if (currentLimit.height() <= height_2 * 2) {
                y = currentLimit.centerY();
            } else if (yToTestWith - height_2 < currentLimit.top) {
                y = (currentLimit.top + height_2);
            } else if (yToTestWith + height_2 > currentLimit.bottom) {
                y = (currentLimit.bottom - height_2);
            }
        }

        if (!isAnimating()) {
            float deltaX = (float) (x - mDScroll.x);
            float deltaY = (float) (y - mDScroll.y);
            mController.offsetDeltaScroll(deltaX, deltaY);
        }
        mDScroll.set((float) x, (float) y);

        final int intX = (int) Math.round(x);
        final int intY = (int) Math.round(y);

        // make sure the next time someone wants the projection it is the
        // correct one!
        mProjection = null;

        super.scrollTo(intX, intY);

        // do callback on listener
        if (mListeners.size() > 0) {
            final ScrollEvent event = new ScrollEvent(this, intX, intY, mController.currentlyInUserAction());
            for (MapListener listener : mListeners) {
                listener.onScroll(event);
            }
        }
    }

    @Override
    public void setBackgroundColor(final int pColor) {
        mTilesOverlay.setLoadingBackgroundColor(pColor);
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas c) {
        super.onDraw(c);

        mProjection = updateProjection();

        // Save the current canvas matrix
        c.save();

        c.translate(getWidth() / 2, getHeight() / 2);
        c.scale(mMultiTouchScale, mMultiTouchScale, mMultiTouchScalePoint.x,
                mMultiTouchScalePoint.y);

        // rotate Canvas
        c.rotate(mapOrientation, mProjection.getScreenRect().exactCenterX(),
                mProjection.getScreenRect().exactCenterY());

        // Draw all Overlays.
        this.getOverlayManager().draw(c, this);

        c.restore();
    }

    /**
     * Private Helper Method for onDraw().
     *
     * @return New Projection object
     */
    private Projection updateProjection() {
        return new Projection(this);
    }

    /**
     * Returns true if the safe drawing canvas is being used.
     *
     * @see {@link com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas}
     */
    public boolean isUsingSafeCanvas() {
        return this.getOverlayManager().isUsingSafeCanvas();
    }

    /**
     * Sets whether the safe drawing canvas is being used.
     *
     * @see {@link com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas}
     */
    public void setUseSafeCanvas(boolean useSafeCanvas) {
        this.getOverlayManager().setUseSafeCanvas(useSafeCanvas);
    }

    /**
     * Sets whether the scrollable area limit should take the view
     * ratio into account (keeping the same ratio as the screen)
     * If yes you will be able to zoom out to see the whole area
     * whatever the screen ratio.
     */
    public MapView setConstraintRegionFit(boolean value) {
        this.mConstraintRegionFit = value;
        if (isLayedOut()) {
            updateScrollableAreaLimit();
            updateMinZoomLevel();
        }
        return this;
    }

    private UserLocationOverlay getOrCreateLocationOverlay() {
        if (mLocationOverlay == null) {
            mLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(getContext()), this);
            addOverlay(mLocationOverlay);
        }
        return mLocationOverlay;
    }

    /**
     * Show or hide the user location overlay
     */
    public MapView setUserLocationEnabled(final boolean value) {
        if (value) {
            getOrCreateLocationOverlay().enableMyLocation();
        } else if (mLocationOverlay != null) {
            mLocationOverlay.disableMyLocation();
            removeOverlay(mLocationOverlay);
            mLocationOverlay = null;
        }
        return this;
    }

    /**
     * Show or hide the user location overlay
     */
    public final boolean getUserLocationEnabled() {
        if (mLocationOverlay != null) {
            return mLocationOverlay.isMyLocationEnabled();
        }
        return false;
    }

    /**
     * Set the user location tracking mode
     */
    public MapView setUserLocationTrackingMode(final UserLocationOverlay.TrackingMode mode) {
        getOrCreateLocationOverlay().setTrackingMode(mode);
        return this;
    }

    /**
     * Set the user location tracking mode
     */
    public MapView setUserLocationRequiredZoom(final float zoomLevel) {
        getOrCreateLocationOverlay().setRequiredZoom(zoomLevel);
        return this;
    }

    /**
     * get the user location tracking mode
     */
    public UserLocationOverlay.TrackingMode getUserLocationTrackingMode() {
        if (mLocationOverlay != null) {
            return mLocationOverlay.getTrackingMode();
        }
        return UserLocationOverlay.TrackingMode.NONE;
    }

    /**
     * Go to user location
     */
    public void goToUserLocation(final boolean animated) {
        if (mLocationOverlay != null) {
            mLocationOverlay.goToMyPosition(animated);
        }
    }

    /**
     * Get the user location overlay if created
     */
    public UserLocationOverlay getUserLocationOverlay() {
        return mLocationOverlay;
    }

    /**
     * Get the user location overlay if created
     */
    public LatLng getUserLocation() {
        if (mLocationOverlay != null) {
            return mLocationOverlay.getMyLocation();
        }
        return null;
    }

    public boolean isUserLocationVisible() {
        if (mLocationOverlay != null) {
            final Location pos = mLocationOverlay.getLastFix();
            if (pos != null && isLayedOut()) {
                final Projection projection = getProjection();
                final float accuracyInPixels = pos.getAccuracy() / (float) projection.groundResolution(
                        pos.getLatitude());
                final PointF point = projection.toMapPixels(pos.getLatitude(), pos.getLongitude(), null);
                return projection.getScreenRect().intersects((int) (point.x - accuracyInPixels),
                        (int) (point.y - accuracyInPixels),
                        (int) (point.x + accuracyInPixels),
                        (int) (point.y + accuracyInPixels));
            }
        }
        return false;
    }

    /**
     * Enable or disable the diskCache
     */
    public void setDiskCacheEnabled(final boolean enabled) {
        if (mTileProvider != null) {
            mTileProvider.setDiskCacheEnabled(enabled);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        this.onDetach();
        super.onDetachedFromWindow();
    }

    /**
     * Determines if maps are animating a zoom operation. Useful for overlays to avoid
     * recalculating
     * during an animation sequence.
     *
     * @return boolean indicating whether view is animating.
     */
    public boolean isAnimating() {
        return mIsAnimating.get();
    }

    protected void setIsAnimating(final boolean value) {
        mIsAnimating.set(value);
    }

    public TileLoadedListener getTileLoadedListener() {
        return tileLoadedListener;
    }

    public static void setDebugMode(boolean value) {
        UtilConstants.setDebugMode(value);
    }

    /**
     * Per-child layout information associated with OpenStreetMapView.
     */
    public static class LayoutParams extends ViewGroup.LayoutParams implements MapViewLayouts {
        /**
         * The location of the child within the map view.
         */
        public ILatLng geoPoint;

        /**
         * The alignment the alignment of the view compared to the location.
         */
        public int alignment;

        public int offsetX;
        public int offsetY;

        /**
         * Creates a new set of layout parameters with the specified width, height and location.
         *
         * @param width      the width, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed
         *                   size
         *                   in pixels
         * @param height     the height, either {@link #FILL_PARENT}, {@link #WRAP_CONTENT} or a fixed
         *                   size
         *                   in pixels
         * @param aGeoPoint  the location of the child within the map view
         * @param aAlignment the alignment of the view compared to the location {@link
         *                   #BOTTOM_CENTER},
         *                   {@link #BOTTOM_LEFT}, {@link #BOTTOM_RIGHT} {@link #TOP_CENTER},
         *                   {@link #TOP_LEFT}, {@link #TOP_RIGHT}
         * @param aOffsetX   the additional X offset from the alignment location to draw the child
         *                   within
         *                   the map view
         * @param aOffsetY   the additional Y offset from the alignment location to draw the child
         *                   within
         *                   the map view
         */
        public LayoutParams(final int width, final int height, final ILatLng aGeoPoint,
                            final int aAlignment, final int aOffsetX, final int aOffsetY) {
            super(width, height);
            if (aGeoPoint != null) {
                this.geoPoint = aGeoPoint;
            } else {
                this.geoPoint = new LatLng(0, 0);
            }
            this.alignment = aAlignment;
            this.offsetX = aOffsetX;
            this.offsetY = aOffsetY;
        }

        /**
         * Since we cannot use XML files in this project this constructor is useless. Creates a new
         * set of layout parameters. The values are extracted from the supplied attributes set and
         * context.
         *
         * @param c     the application environment
         * @param attrs the set of attributes fom which to extract the layout parameters values
         */
        public LayoutParams(final Context c, final AttributeSet attrs) {
            super(c, attrs);
            this.geoPoint = new LatLng(0, 0);
            this.alignment = BOTTOM_CENTER;
        }

        public LayoutParams(final ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public void setMapViewListener(MapViewListener listener) {
        this.mMapViewListener = listener;
    }

    public void setOnTileLoadedListener(TileLoadedListener aTileLoadedListener) {
        this.tileLoadedListener = aTileLoadedListener;
    }

    public void setOnTilesLoadedListener(TilesLoadedListener aTilesLoadedListener) {
        this.tilesLoadedListener = aTilesLoadedListener;
    }

    public TilesLoadedListener getTilesLoadedListener() {
        return tilesLoadedListener;
    }

    @Override
    public String toString() {
        return "MapView {" + getTileProvider() + "}";
    }

    public void setDefaultPinRes(int res) {
        mDefaultPinRes = res;
    }

    public void setDefaultPinDrawable(Drawable drawable) {
        mDefaultPinDrawable = drawable;
    }

    public Drawable getDefaultPinDrawable() {
        if (mDefaultPinDrawable == null && mDefaultPinRes != 0) {
            BitmapFactory.Options opts =
                    BitmapUtils.getBitmapOptions(getResources().getDisplayMetrics());
            mDefaultPinDrawable = new BitmapDrawable(getResources(),
                    BitmapFactory.decodeResource(context.getResources(), mDefaultPinRes, opts));
        }
        return mDefaultPinDrawable;
    }

    public void setDefaultPinAnchor(PointF point) {
        mDefaultPinAnchor = point;
    }

    public PointF getDefaultPinAnchor() {
        return mDefaultPinAnchor;
    }
}

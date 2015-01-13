package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

/**
 * Immutable class describing a LatLng with a Title and a Description.
 */
public class Marker {
    public static final int ITEM_STATE_FOCUSED_MASK = 4;
    public static final int ITEM_STATE_PRESSED_MASK = 1;
    public static final int ITEM_STATE_SELECTED_MASK = 2;

    private int group = 0;
    private boolean mClustered;

    private final RectF mMyLocationRect = new RectF(0, 0, 0, 0);
    private final RectF mMyLocationPreviousRect = new RectF(0, 0, 0, 0);
    protected final PointF mCurMapCoords = new PointF();

    private Context context;
    private MapView mapView;
    private Icon icon;

    protected String mUid;
    protected LatLng mLatLng;
    protected Drawable mMarker;
    protected PointF mAnchor = null;

    private String mTitle = "";
    private String mDescription = "";
    private String mSubDescription = "";
    //a third field that can be displayed in the infowindow, on a third line
    private Drawable mImage; //that will be shown in the infowindow.
    //private GeoPoint mGeoPoint //unfortunately, this is not so simple...
    private Object mRelatedObject; //reference to an object (of any kind) linked to this item.
    private boolean bubbleShowing;
    private ItemizedOverlay mParentHolder;

    /**
     * Construct a new Marker, given title, description, and place
     * @param title Marker title
     * @param description Marker description
     * @param latLng Marker position
     */
    public Marker(String title, String description, LatLng latLng) {
        this(null, title, description, latLng);
    }

    /**
     * Initialize a new marker object, adding it to a MapView and attaching a tooltip
     *
     * @param mv a mapview
     * @param aTitle the title of the marker, in a potential tooltip
     * @param aDescription the description of the marker, in a tooltip
     * @param aLatLng the location of the marker
     */
    public Marker(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        super();
        this.mapView = mv;
        this.setTitle(aTitle);
        this.setDescription(aDescription);
        this.mLatLng = aLatLng;
        Log.d(getClass().getCanonicalName(), "markerconst" + mv + aTitle + aDescription + aLatLng);
        if (mv != null) {
            mAnchor = mv.getDefaultPinAnchor();
        }
        mParentHolder = null;
    }

    /**
     * Attach this marker to a given mapview and that mapview's context
     * @param mv the mapview to add this marker to
     * @return
     */
    public Marker addTo(MapView mv) {
        if (mMarker == null) {
            //if there is an icon it means it's not loaded yet
            //thus change the drawable while waiting
            setMarker(mv.getDefaultPinDrawable());
        }
        mapView = mv;
        context = mv.getContext();
        if (mAnchor == null) {
            mAnchor = mv.getDefaultPinAnchor();
        }
        return this;
    }

    /**
     * Determine if this marker has a title, description, subdescription,
     * or image that could be displayed
     * @return true if the marker has content
     */
    public boolean hasContent() {
        return !TextUtils.isEmpty(this.mTitle) ||
                !TextUtils.isEmpty(this.mDescription) ||
                !TextUtils.isEmpty(this.mSubDescription) ||
                this.mImage != null;
    }

    protected InfoWindow createTooltip(MapView mv) {
        return new InfoWindow(R.layout.tooltip, mv);
    }

    private InfoWindow mToolTip;

    /**
     * Get this marker's tooltip, creating it if it doesn't exist yet.
     * @param mv
     * @return
     */
    public InfoWindow getToolTip(MapView mv) {
        if (mToolTip == null || mToolTip.getMapView() != mv) {
            mToolTip = createTooltip(mv);
        }
        return mToolTip;
    }

    public void closeToolTip() {
        if (mToolTip != null && mToolTip.equals(mToolTip.getMapView().getCurrentTooltip())) {
            mToolTip.getMapView().closeCurrentTooltip();
        }
    }

    public void blur() {
        if (mParentHolder != null) {
            mParentHolder.blurItem(this);
        }
    }

    /**
     * Indicates a hotspot for an area. This is where the origin (0,0)of a point will be located
     * relative to the area. In otherwords this acts as an offset. NONE indicates that no
     * adjustment
     * should be made.
     */
    public enum HotspotPlace {
        NONE, CENTER, BOTTOM_CENTER, TOP_CENTER, RIGHT_CENTER,
        LEFT_CENTER, UPPER_RIGHT_CORNER, LOWER_RIGHT_CORNER,
        UPPER_LEFT_CORNER, LOWER_LEFT_CORNER
    }

    public String getUid() {
        return mUid;
    }

    public String getTitle() {
        return mTitle;
    }

    public LatLng getPoint() {
        return mLatLng;
    }

    public void setTitle(String aTitle) {
        mTitle = aTitle;
    }

    public void setDescription(String aDescription) {
        mDescription = aDescription;
    }

    public void setSubDescription(String aSubDescription) {
        mSubDescription = aSubDescription;
    }

    public void setImage(Drawable anImage) {
        mImage = anImage;
    }

    public void setRelatedObject(Object o) {
        mRelatedObject = o;
    }

    /**
     * Set the centerpoint of this marker in geographical coordinates
     * @param point
     */
    public void setPoint(LatLng point) {
        mLatLng = point;
        invalidate();
    }

    /**
     * Set the description attached to this marker
     * @return
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Set the sub-description attached to this marker
     * @return
     */
    public String getSubDescription() {
        return mSubDescription;
    }

    /**
     * Set the image attached to this marker
     * @return
     */
    public Drawable getImage() {
        return mImage;
    }

    public Object getRelatedObject() {
        return mRelatedObject;
    }

    public ItemizedOverlay getParentHolder() {
        return mParentHolder;
    }

    public void setParentHolder(ItemizedOverlay o) {
        mParentHolder = o;
    }

    /**
     * Gets the drawable for the marker
     * @param stateBitset
     * @return marker drawable corresponding to stateBitset
     */
    public Drawable getMarker(final int stateBitset) {
        // marker not specified
        if (mMarker == null) {
            return null;
        }

        // set marker state appropriately
        setState(mMarker, stateBitset);
        return mMarker;
    }

    public void setMarker(final Drawable marker) {
        this.mMarker = marker;
        if (marker != null) {
            marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
        }
        invalidate();
    }

    /**
     * Sets the marker hotspot
     * @param place
     */
    public void setHotspot(HotspotPlace place) {
        if (place == null) {
            place = HotspotPlace.BOTTOM_CENTER; //use same default than in osmdroid.
        }
        switch (place) {
            case NONE:
            case UPPER_LEFT_CORNER:
                mAnchor.set(0, 0);
                break;
            case BOTTOM_CENTER:
                mAnchor.set(0.5f, 1f);
                break;
            case LOWER_LEFT_CORNER:
                mAnchor.set(0, 1);
                break;
            case LOWER_RIGHT_CORNER:
                mAnchor.set(1, 1);
                break;
            case CENTER:
                mAnchor.set(0.5f, 0.5f);
                break;
            case LEFT_CENTER:
                mAnchor.set(0, 0.5f);
                break;
            case RIGHT_CENTER:
                mAnchor.set(1, 0.5f);
                break;
            case TOP_CENTER:
                mAnchor.set(0.5f, 0);
                break;
            case UPPER_RIGHT_CORNER:
                mAnchor.set(1, 0);
                break;
        }
        invalidate();
    }

    public Point getAnchor() {
        if (mAnchor != null) {
            int markerWidth = getWidth(), markerHeight = getHeight();
            return new Point((int) (-mAnchor.x * markerWidth), (int) (-mAnchor.y * markerHeight));
        }
        return new Point(0, 0);
    }

    public Point getAnchor(HotspotPlace place) {
        int markerWidth = getWidth(), markerHeight = getHeight();
        return getHotspot(place, markerWidth, markerHeight);
    }

    public void setAnchor(final PointF anchor) {
        this.mAnchor = anchor;
        invalidate();
    }

    public static void setState(final Drawable drawable, final int stateBitset) {
        final int[] states = new int[3];
        int index = 0;
        if ((stateBitset & ITEM_STATE_PRESSED_MASK) > 0) {
            states[index++] = android.R.attr.state_pressed;
        }
        if ((stateBitset & ITEM_STATE_SELECTED_MASK) > 0) {
            states[index++] = android.R.attr.state_selected;
        }
        if ((stateBitset & ITEM_STATE_FOCUSED_MASK) > 0) {
            states[index++] = android.R.attr.state_focused;
        }

        drawable.setState(states);
    }

    public Drawable getDrawable() {
        return this.mMarker;
    }

    /**
     * Get the width of the marker, based on the width of the image backing it.
     */
    public int getWidth() {
        return this.mMarker.getIntrinsicWidth();
    }

    public int getHeight() {
        return this.mMarker.getIntrinsicHeight() / 2;
    }

    /**
     * Get the current position of the marker in pixels
     * @param projection
     * @param reuse
     */
    public PointF getPositionOnScreen(final Projection projection, final PointF reuse) {
        return projection.toPixels(mCurMapCoords, reuse);
    }

    public PointF getDrawingPositionOnScreen(final Projection projection, PointF reuse) {
        reuse = getPositionOnScreen(projection, reuse);
        Point point = getAnchor();
        reuse.offset(point.x, point.y);
        return reuse;
    }

    protected RectF getDrawingBounds(final Projection projection, RectF reuse) {
        if (reuse == null) {
            reuse = new RectF();
        }
        final PointF position = getPositionOnScreen(projection, null);
        final int w = getWidth();
        final int h = getHeight();
        final float x = position.x - mAnchor.x * w;
        final float y = position.y - mAnchor.y * h;
        reuse.set(x, y, x + w, y + h * 2);
        return reuse;
    }

    protected RectF getMapDrawingBounds(final Projection projection, RectF reuse) {
        if (reuse == null) {
            reuse = new RectF();
        }
        projection.toMapPixels(mLatLng, mCurMapCoords);
        final int w = getWidth();
        final int h = getHeight();
        final float x = mCurMapCoords.x - mAnchor.x * w;
        final float y = mCurMapCoords.y - mAnchor.y * h;
        reuse.set(x, y, x + w, y + h * 2);
        return reuse;
    }

    public PointF getHotspotScale(HotspotPlace place, PointF reuse) {
        if (reuse == null) {
            reuse = new PointF();
        }
        if (place == null) {
            place = HotspotPlace.BOTTOM_CENTER; //use same default than in osmdroid.
        }
        switch (place) {
            case NONE:
            case UPPER_LEFT_CORNER:
                reuse.set(0, 0);
                break;
            case BOTTOM_CENTER:
                reuse.set(0.5f, 1f);
                break;
            case LOWER_LEFT_CORNER:
                reuse.set(0, 1);
                break;
            case LOWER_RIGHT_CORNER:
                reuse.set(1, 1);
                break;
            case CENTER:
                reuse.set(0.5f, 0.5f);
                break;
            case LEFT_CENTER:
                reuse.set(0, 0.5f);
                break;
            case RIGHT_CENTER:
                reuse.set(1, 0.5f);
                break;
            case TOP_CENTER:
                reuse.set(0.5f, 0);
                break;
            case UPPER_RIGHT_CORNER:
                reuse.set(1, 0);
                break;
        }
        return reuse;
    }

    /**
     * From a HotspotPlace and drawable dimensions (width, height), return the hotspot position.
     * Could be a public method of HotspotPlace or OverlayItem...
     */
    public Point getHotspot(HotspotPlace place, int w, int h) {
        PointF scale = getHotspotScale(place, null);
        return new Point((int) (-w * scale.x), (int) (-h * scale.y));
    }

    /**
     * Populates this tooltip with all item info:
     * <ul>title and description in any case, </ul>
     * <ul>image and sub-description if any.</ul>
     * and centers the map view on the item if panIntoView is true. <br>
     */
    public void showBubble(InfoWindow tooltip, MapView aMapView, boolean panIntoView) {
        //offset the tooltip to be top-centered on the marker:
        Point markerH = getAnchor();
        Point tooltipH = getAnchor(HotspotPlace.TOP_CENTER);
        markerH.offset(-tooltipH.x, tooltipH.y);
        tooltip.open(this, this.getPoint(), markerH.x, markerH.y);
        if (panIntoView) {
            aMapView.getController().animateTo(getPoint());
        }

        bubbleShowing = true;
        tooltip.setBoundMarker(this);
    }

    /**
     * Sets the Icon image that represents this marker on screen.
     */
    public Marker setIcon(Icon aIcon) {
        this.icon = aIcon;
        icon.setMarker(this);
        return this;
    }

    public PointF getPositionOnMap() {
        return mCurMapCoords;
    }

    public void updateDrawingPosition() {
        if (mapView == null) {
            return; //not on map yet
        }
        getMapDrawingBounds(mapView.getProjection(), mMyLocationRect);
    }

    /**
     * Sets the marker to be redrawn.
     */
    public void invalidate() {
        if (mapView == null) {
            return; //not on map yet
        }
        // Get new drawing bounds
        mMyLocationPreviousRect.set(mMyLocationRect);
        updateDrawingPosition();
        final RectF newRect = new RectF(mMyLocationRect);
        // If we had a previous location, merge in those bounds too
        newRect.union(mMyLocationPreviousRect);
        // Invalidate the bounds
        mapView.post(new Runnable() {
            @Override
            public void run() {
                mapView.invalidateMapCoordinates(newRect);
            }
        });
    }
}

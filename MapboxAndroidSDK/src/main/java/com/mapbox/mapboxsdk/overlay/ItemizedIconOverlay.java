package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemizedIconOverlay extends ItemizedOverlay {

    protected final List<Marker> mItemList;
    protected OnItemGestureListener<Marker> mOnItemGestureListener;
    private int mDrawnItemsLimit = Integer.MAX_VALUE;
    private MapView view;
    private Context context;

    public ItemizedIconOverlay(final Context pContext, final List<Marker> pList,
                               final com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay.OnItemGestureListener<Marker> pOnItemGestureListener) {
        this(pContext, pList, pOnItemGestureListener, false);
    }

    public ItemizedIconOverlay(final Context pContext,
                               final List<Marker> pList,
                               final com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay.OnItemGestureListener<Marker> pOnItemGestureListener,
                               boolean sortList) {
        super();
        this.context = pContext;
        this.mItemList = pList;
        this.mOnItemGestureListener = pOnItemGestureListener;
        if (sortList) {
            sortListByLatitude();
        }
        populate();
    }

    /**
     * Sorts List of Marker by Latitude
     */
    private void sortListByLatitude() {
        Collections.sort(mItemList, new Comparator<Marker>() {
            public int compare(Marker a, Marker b) {
                return Double.valueOf(a.getPoint().getLatitude()).compareTo(b.getPoint().getLatitude());
            }
        });
    }

    @Override
    public boolean onSnapToItem(final int pX, final int pY, final Point pSnapPoint,
                                final MapView pMapView) {
        // TODO Implement this!
        return false;
    }

    @Override
    protected Marker createItem(final int index) {
        return mItemList.get(index);
    }

    @Override
    public int size() {
        return Math.min(mItemList.size(), mDrawnItemsLimit);
    }

    public boolean addItem(final Marker item) {
        item.setParentHolder(this);
        final boolean result = mItemList.add(item);
        populate();
        return result;
    }

//    public void addItem(final int location, final Marker item) {
//        item.setParentHolder(this);
//        mItemList.add(location, item);
//        populate();
//    }

    /**
     * When a content sensitive action is performed the content item needs to be identified. This
     * method does that and then performs the assigned task on that item.
     *
     * @return true if event is handled false otherwise
     */
    private boolean activateSelectedItems(final MotionEvent event,
                                          final MapView mapView,
                                          final ActiveItem task) {
        final Projection projection = mapView.getProjection();
        final float x = event.getX();
        final float y = event.getY();
        for (int i = 0; i < this.mItemList.size(); ++i) {
            final Marker item = getItem(i);
            if (markerHitTest(item, projection, x, y)) {
                if (task.run(i)) {
                    this.setFocus(item);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addItems(final List<Marker> items) {
        for (Object item : items) {
            if (item instanceof Marker) {
                ((Marker) item).setParentHolder(this);
            }
        }
        final boolean result = mItemList.addAll(items);
        populate();
        return result;
    }

    public void removeAllItems() {
        removeAllItems(true);
    }

    public void removeAllItems(final boolean withPopulate) {
        for (Marker item : mItemList) {
            item.setParentHolder(null);
        }
        mItemList.clear();
        if (withPopulate) {
            populate();
        }
    }

    protected void onItemRemoved(final Marker item) {
        blurItem(item);
        item.setParentHolder(null);
    }

    public boolean removeItem(final Marker item) {
        final boolean result = mItemList.remove(item);
        if (getFocus() == item) {
            setFocus(null);
        }
        if (result) {
            onItemRemoved(item);
        }
        populate();
        return result;
    }

    public void clearFocus() {
        setFocus(null);
    }

    public Marker removeItem(final int position) {
        final Marker item = mItemList.remove(position);
        if (item != null) {
            onItemRemoved(item);
        }
        populate();
        return item;
    }

    public void removeItems(final List items) {
        for (Object item : items) {
            if (item instanceof Marker) {
                final boolean result = mItemList.remove(item);
                if (result) {
                    onItemRemoved((Marker) item);
                }
            }
        }
        populate();
    }

    /**
     * Each of these methods performs a item sensitive check. If the item is located its
     * corresponding method is called. The result of the call is returned.
     * <p/>
     * Helper methods are provided so that child classes may more easily override behavior without
     * resorting to overriding the ItemGestureListener methods.
     */
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(final int index) {
                final ItemizedIconOverlay that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onSingleTapUpHelper(index, that.mItemList.get(index), mapView);
            }
        }));
    }

    protected boolean onSingleTapUpHelper(final int index, final Marker item,
                                          final MapView mapView) {
        return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        return (activateSelectedItems(event, mapView, new ActiveItem() {
            @Override
            public boolean run(final int index) {
                final ItemizedIconOverlay that = ItemizedIconOverlay.this;
                if (that.mOnItemGestureListener == null) {
                    return false;
                }
                return onLongPressHelper(index, getItem(index));
            }
        }));
    }

    protected boolean onLongPressHelper(final int index, final Marker item) {
        return this.mOnItemGestureListener.onItemLongPress(index, item);
    }

    private double getThreshold() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return (double) dm.widthPixels;
    }

    private ArrayList<LatLng> getCoordinateList(List<Marker> list) {
        ArrayList<LatLng> theList = new ArrayList<LatLng>();
        for (Marker element : list) {
            theList.add(element.getPoint());
        }
        return theList;
    }

    private float screenX(Marker item) {
        return view.getProjection().toMapPixels(item.getPoint(), null).x;
    }

    private float screenY(Marker item) {
        return view.getProjection().toMapPixels(item.getPoint(), null).y;
    }

    public int getDrawnItemsLimit() {
        return this.mDrawnItemsLimit;
    }

    public void setDrawnItemsLimit(final int aLimit) {
        this.mDrawnItemsLimit = aLimit;
    }

    /**
     * When the item is touched one of these methods may be invoked depending on the type of touch.
     * Each of them returns true if the event was completely handled.
     */
    public static interface OnItemGestureListener<T> {
        public boolean onItemSingleTapUp(final int index, final T item);

        public boolean onItemLongPress(final int index, final T item);
    }

    public static interface ActiveItem {
        public boolean run(final int aIndex);
    }
}

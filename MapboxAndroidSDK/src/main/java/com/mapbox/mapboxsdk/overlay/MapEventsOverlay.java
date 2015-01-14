package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;

/**
 * Empty overlay than can be used to detect events on the map,
 * and to throw them to a MapEventsReceiver.
 *
 * @author M.Kergall
 * @see MapEventsReceiver
 */
public class MapEventsOverlay extends Overlay {

    private MapEventsReceiver mReceiver;

    /**
     * @param ctx the context
     * @param receiver the object that will receive/handle the events.
     * It must implement MapEventsReceiver interface.
     */
    public MapEventsOverlay(Context ctx, MapEventsReceiver receiver) {
        super(ctx);
        mReceiver = receiver;
        setOverlayIndex(MAPEVENTSOVERLAY_INDEX);
    }

    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        //Nothing to draw
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
        Projection proj = mapView.getProjection();
        ILatLng p = proj.fromPixels(e.getX(), e.getY());
        return mReceiver.singleTapUpHelper(p);
    }

    @Override
    public boolean onLongPress(MotionEvent e, MapView mapView) {
        Projection proj = mapView.getProjection();
        ILatLng p = proj.fromPixels(e.getX(), e.getY());
        //throw event to the receiver:
        return mReceiver.longPressHelper(p);
    }
}

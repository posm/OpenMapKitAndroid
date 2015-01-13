package com.mapbox.mapboxsdk.events;

import com.mapbox.mapboxsdk.views.MapView;

/**
 * The event generated when a map has finished zooming to the level zoomLevel
 */
public class ZoomEvent implements MapEvent {

    protected MapView source;
    protected float zoomLevel;
    protected boolean userAction;

    public ZoomEvent(final MapView aSource, final float aZoomLevel, final boolean userAction) {
        this.source = aSource;
        this.zoomLevel = aZoomLevel;
        this.userAction = userAction;
    }

    /**
     * Return the map which generated this event.
     */
    public MapView getSource() {
        return source;
    }

    /**
     * Return the zoom level zoomed to.
     */
    public float getZoomLevel() {
        return zoomLevel;
    }

    /**
     * @return true if it was a user action (touch action).
     */
    public boolean getUserAction() {
        return userAction;
    }

    @Override
    public String toString() {
        return "ZoomEvent [source=" + source + ", zoomLevel=" + zoomLevel + ", userAction=" + userAction + "]";
    }
}

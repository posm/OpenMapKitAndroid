package com.mapbox.mapboxsdk.events;

/**
 * The listener interface for receiving map movement events. To process a map event, either
 * implement this interface or extend MapAdapter, then register with the MapView using
 * setMapListener.
 *
 * @author Theodore Hong
 */
public interface MapListener {
    /**
     * Called when a map is scrolled.
     */
    public void onScroll(ScrollEvent event);

    /**
     * Called when a map is zoomed.
     */
    public void onZoom(ZoomEvent event);

    /**
     * Called when a map is rotated.
     */
    public void onRotate(RotateEvent event);

}

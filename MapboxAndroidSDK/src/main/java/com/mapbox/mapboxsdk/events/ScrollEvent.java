package com.mapbox.mapboxsdk.events;

import com.mapbox.mapboxsdk.views.MapView;

/**
 * The event generated when a map has finished scrolling to the coordinates
 * (<code>x</code>,<code>y</code>).
 */
public class ScrollEvent implements MapEvent {
    protected MapView source;
    protected int x;
    protected int y;
    protected boolean userAction;

    public ScrollEvent(final MapView aSource, final int ax, final int ay, final boolean userAction) {
        this.source = aSource;
        this.x = ax;
        this.y = ay;
        this.userAction = userAction;
    }

    /**
     * Return the map which generated this event.
     */
    public MapView getSource() {
        return source;
    }

    /**
     * Return the x-coordinate scrolled to.
     */
    public int getX() {
        return x;
    }

    /**
     * Return the y-coordinate scrolled to.
     */
    public int getY() {
        return y;
    }

    /**
     * @return true if it was a user action (touch action).
     */
    public boolean getUserAction() {
        return userAction;
    }

    @Override
    public String toString() {
        return "ScrollEvent [source=" + source + ", x=" + x + ", y=" + y + "]";
    }
}

package com.mapbox.mapboxsdk.events;

import com.mapbox.mapboxsdk.views.MapView;

/**
 * The event generated when a map has finished rotating
 */
public class RotateEvent implements MapEvent {

    protected MapView source;
    protected float angle;
    protected boolean userAction;

    public RotateEvent(final MapView aSource, final float aAngle, final boolean userAction) {
        this.source = aSource;
        this.angle = aAngle;
        this.userAction = userAction;
    }

    /**
     * Return the map which generated this event.
     */
    public MapView getSource() {
        return source;
    }

    /**
     * Return the map angle.
     */
    public float getAngle() {
        return angle;
    }

    /**
     * @return true if it was a user action (touch action).
     */
    public boolean getUserAction() {
        return userAction;
    }

    @Override
    public String toString() {
        return "RotateEvent [source=" + source + ", angle=" + angle + ", userAction=" + userAction + "]";
    }
}

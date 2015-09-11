package com.mapbox.mapboxsdk.views;

import com.almeros.android.multitouch.RotateGestureDetector;
import com.mapbox.mapboxsdk.views.util.OnMapOrientationChangeListener;

/**
 * A custom rotate gesture detector that processes gesture events and dispatches them
 * to the map's overlay system.
 */
public class MapViewRotateGestureDetectorListener implements RotateGestureDetector.OnRotateGestureListener {

    private static String TAG = "MapViewRotateListener";

    private final MapView mapView;
    /**
     * This holds a reference to the first rotation angle
     */
    private float firstAngle;
    private float currentDelta;


    /**
     * Bind a new gesture detector to a map
     *
     * @param mv a map view
     */
    public MapViewRotateGestureDetectorListener(final MapView mv) {
        this.mapView = mv;
    }

    @Override
    public boolean onRotate(RotateGestureDetector detector) {
        float delta = detector.getRotationDegreesDelta();
        currentDelta += delta;
        float newAngle = firstAngle - currentDelta;
        mapView.setMapOrientation(newAngle);

        // If a listener has been set, callback
        OnMapOrientationChangeListener l = mapView.getOnMapOrientationChangeListener();
        if (l != null) {
            l.onMapOrientationChange(newAngle);
        }
        return true;
    }

    @Override
    public boolean onRotateBegin(RotateGestureDetector detector) {
        firstAngle = mapView.getMapOrientation();
        currentDelta = 0;
        return true;
    }

    @Override
    public void onRotateEnd(RotateGestureDetector detector) {

    }
}

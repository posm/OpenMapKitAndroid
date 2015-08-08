package com.mapbox.mapboxsdk.views;

import android.os.Handler;
import android.view.ScaleGestureDetector;

/**
 * https://developer.android.com/training/gestures/scale.html
 * A custom gesture detector that processes gesture events and dispatches them
 * to the map's overlay system.
 */
public class MapViewScaleGestureDetectorListener implements ScaleGestureDetector.OnScaleGestureListener {

    private static String TAG = "MapViewScaleListener";

    /**
     * This is the active focal point in terms of the viewport. Could be a local
     * variable but kept here to minimize per-frame allocations.
     */

    private float lastFocusX;
    private float lastFocusY;
    private float firstSpan;
    private final MapView mapView;
    private boolean scaling;
    private float currentScale;

    /**
     * Bind a new gesture detector to a map
     *
     * @param mv a map view
     */
    public MapViewScaleGestureDetectorListener(final MapView mv) {
        this.mapView = mv;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        lastFocusX = detector.getFocusX();
        lastFocusY = detector.getFocusY();
        firstSpan = detector.getCurrentSpan();
        currentScale = 1.0f;
        if (!this.mapView.isAnimating()) {
            this.mapView.setIsAnimating(true);
            this.mapView.getController().aboutToStartAnimation(lastFocusX, lastFocusY);
            scaling = true;
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!scaling) {
            return true;
        }
        currentScale = detector.getCurrentSpan() / firstSpan;

        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();

        this.mapView.setScale(currentScale);
        this.mapView.getController().offsetDeltaScroll(lastFocusX - focusX, lastFocusY - focusY);
        this.mapView.getController().panBy(lastFocusX - focusX, lastFocusY - focusY, true);

        lastFocusX = focusX;
        lastFocusY = focusY;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (!scaling) {
            return;
        }

        //delaying the "end" will prevent some crazy scroll events when finishing
        //scaling by getting 2 fingers very close to each other
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                float preZoom = mapView.getZoomLevel(false);
                float newZoom = (float) (Math.log(currentScale) / Math.log(2d) + preZoom);
                //set animated zoom so that animationEnd will correctly set it in the mapView
                mapView.setAnimatedZoom(newZoom);
                mapView.getController().onAnimationEnd();
                scaling = false;
            }
        }, 100);

    }
}

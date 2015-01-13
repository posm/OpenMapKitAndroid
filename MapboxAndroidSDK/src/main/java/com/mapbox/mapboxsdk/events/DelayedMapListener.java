package com.mapbox.mapboxsdk.events;

import android.os.Handler;
import android.util.Log;

/**
 * A MapListener that aggregates multiple events called in quick succession.
 * After an event arrives, if another event arrives within <code>delay</code> milliseconds,
 * the original event is discarded.  Otherwise, the event is propagated to the wrapped
 * MapListener.  Note: This class is not thread-safe.]
 */
public class DelayedMapListener implements MapListener {

    /**
     * Default listening delay
     */
    protected static final int DEFAULT_DELAY = 100;

    /**
     * The wrapped MapListener
     */
    MapListener wrappedListener;

    /**
     * Listening delay, in milliseconds
     */
    protected long delay;

    protected Handler handler;
    protected CallbackTask callback;

    /**
     * @param aWrappedListener The wrapped MapListener
     * @param aDelay Listening delay, in milliseconds
     */
    public DelayedMapListener(final MapListener aWrappedListener, final long aDelay) {
        this.wrappedListener = aWrappedListener;
        this.delay = aDelay;
        this.handler = new Handler();
        this.callback = null;
    }

    /**
     * Constructor with default delay.
     *
     * @param aWrappedListener The wrapped MapListener
     */
    public DelayedMapListener(final MapListener aWrappedListener) {
        this(aWrappedListener, DEFAULT_DELAY);
    }

    @Override
    public void onScroll(final ScrollEvent event) {
        dispatch(event);
    }

    @Override
    public void onZoom(final ZoomEvent event) {
        dispatch(event);
    }

    @Override
    public void onRotate(RotateEvent event) {
        dispatch(event);
    }

    /*
     * Process an incoming MapEvent.
     */
    protected void dispatch(final MapEvent event) {
        // cancel any pending callback
        if (callback != null) {
            handler.removeCallbacks(callback);
        }
        callback = new CallbackTask(event);

        // set timer
        handler.postDelayed(callback, delay);
    }

    // Callback tasks
    private class CallbackTask implements Runnable {
        private final MapEvent event;

        public CallbackTask(final MapEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            // do the callback
            if (event instanceof ScrollEvent) {
                wrappedListener.onScroll((ScrollEvent) event);
            } else if (event instanceof ZoomEvent) {
                wrappedListener.onZoom((ZoomEvent) event);
            } else if (event instanceof RotateEvent) {
                wrappedListener.onRotate((RotateEvent) event);
            } else {
                // unknown event; discard
                Log.i(TAG, "Unknown event received: " + event);
            }
        }
    }

    private static final String TAG = "DelayedMapListener";
}

package com.mapbox.mapboxsdk.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.os.Process;
import android.util.Log;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileRequestState;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * An abstract base class for modular tile providers
 *
 * @author Marc Kurtz
 * @author Neil Boyd
 */
public abstract class MapTileModuleLayerBase implements TileLayerConstants {

    /**
     * Gets the human-friendly name assigned to this tile provider.
     *
     * @return the thread name
     */
    protected abstract String getName();

    /**
     * Gets the name assigned to the thread for this provider.
     *
     * @return the thread name
     */
    protected abstract String getThreadGroupName();

    /**
     * It is expected that the implementation will construct an internal member which internally
     * implements a {@link TileLoader}. This method is expected to return a that internal member to
     * methods of the parent methods.
     *
     * @return the internal member of this tile provider.
     */
    protected abstract Runnable getTileLoader();

    /**
     * Returns true if implementation uses a data connection, false otherwise. This value is used
     * to
     * determine if this provider should be skipped if there is no data connection.
     *
     * @return true if implementation uses a data connection, false otherwise
     */
    public abstract boolean getUsesDataConnection();

    /**
     * Gets the minimum zoom level this tile provider can provide
     *
     * @return the minimum zoom level
     */
    public abstract float getMinimumZoomLevel();

    /**
     * Gets the maximum zoom level this tile provider can provide
     *
     * @return the maximum zoom level
     */
    public abstract float getMaximumZoomLevel();

    /**
     * Get the tile provider bounding box.
     *
     * @return the tile source bounding box
     */
    public abstract BoundingBox getBoundingBox();

    /**
     * Get the tile provider center.
     *
     * @return the tile source center
     */
    public abstract LatLng getCenterCoordinate();

    /**
     * Get the tile provider suggested starting zoom.
     *
     * @return the tile suggested starting zoom
     */
    public abstract float getCenterZoom();

    /**
     * Get the tile provider size in pixels.
     *
     * @return the tile size in pixels
     */
    public abstract int getTileSizePixels();

    /**
     * Sets the tile source for this tile provider.
     *
     * @param tileSource the tile source
     */
    public abstract void setTileSource(ITileLayer tileSource);

    public abstract ITileLayer getTileSource();

    public abstract String getCacheKey();

    private final ExecutorService mExecutor;

    protected final Object mQueueLockObject = new Object();
    protected final HashMap<MapTile, MapTileRequestState> mWorking;
    protected final LinkedHashMap<MapTile, MapTileRequestState> mPending;

    public MapTileRequestState popFirstPending() {
        for (MapTile tile : mPending.keySet()) {
            return mPending.remove(tile);
        }
        return null;
    }

    /**
     * Initialize a new tile provider, given a thread pool and a pending queue size. The pending
     * queue
     * size must be larger than or equal to the thread pool size.
     */
    public MapTileModuleLayerBase(int pThreadPoolSize, final int pPendingQueueSize) {
        if (pPendingQueueSize < pThreadPoolSize) {
            Log.w(TAG,
                    "The pending queue size is smaller than the thread pool size. Automatically reducing the thread pool size.");
            pThreadPoolSize = pPendingQueueSize;
        }
        mExecutor = Executors.newFixedThreadPool(pThreadPoolSize,
                new ConfigurablePriorityThreadFactory(Thread.NORM_PRIORITY, getThreadGroupName()));

        mWorking = new HashMap<MapTile, MapTileRequestState>();
        mPending =
                new LinkedHashMap<MapTile, MapTileRequestState>(pPendingQueueSize + 2, 0.1f, true) {

                    private static final long serialVersionUID = 6455337315681858866L;

                    @Override
                    protected boolean removeEldestEntry(
                            final Map.Entry<MapTile, MapTileRequestState> pEldest) {
                        while (size() > pPendingQueueSize) {
                            MapTileRequestState state = popFirstPending();
                            state.getCallback().mapTileRequestFailed(state);
                        }
                        return false;
                    }
                };
    }

    /**
     * Loads a map tile asynchronously, adding it to the queue and calling getTileLoader.
     */
    public void loadMapTileAsync(final MapTileRequestState pState) {
        synchronized (mQueueLockObject) {
/*
                Log.d(TAG, "MapTileModuleLayerBase.loadMaptileAsync() on provider: " + getName() + " for tile: " + pState.getMapTile());
                if (mPending.containsKey(pState.getMapTile())) {
                    Log.d(TAG, "MapTileModuleLayerBase.loadMaptileAsync() tile already exists in request queue for modular provider. Moving to front of queue.");
                } else {
                    Log.d(TAG, "MapTileModuleLayerBase.loadMaptileAsync() adding tile to request queue for modular provider.");
                }
*/
            // this will put the tile in the queue, or move it to the front of
            // the queue if it's already present
            mPending.put(pState.getMapTile(), pState);
        }

        try {
            mExecutor.execute(getTileLoader());
        } catch (final RejectedExecutionException e) {
            Log.w(TAG, "RejectedExecutionException", e);
        }
    }

    /**
     * Clears both pending and working queues.
     */
    protected void clearQueue() {
        synchronized (mQueueLockObject) {
            mPending.clear();
            mWorking.clear();
        }
    }

    /**
     * Detach, we're shutting down - Stops all workers.
     */
    public void detach() {
        this.clearQueue();
        this.mExecutor.shutdown();
    }

    /**
     * Marks a given map tile as neither being downloaded or worked on.
     */
    void removeTileFromQueues(final MapTile mapTile) {
        synchronized (mQueueLockObject) {
            if (DEBUG_TILE_PROVIDERS) {
                Log.d(TAG, "MapTileModuleLayerBase.removeTileFromQueues() on provider: "
                        + getName()
                        + " for tile: "
                        + mapTile);
            }
            mPending.remove(mapTile);
            mWorking.remove(mapTile);
        }
    }

    /**
     * Load the requested tile. An abstract internal class whose objects are used by worker threads
     * to acquire tiles from servers. It processes tiles from the 'pending' set to the 'working'
     * set
     * as they become available. The key unimplemented method is 'loadTile'.
     */
    protected abstract class TileLoader implements Runnable {
        /**
         * Load the requested tile.
         *
         * @return the tile if it was loaded successfully, or null if failed to
         * load and other tile providers need to be called
         * @throws CantContinueException
         */
        protected abstract Drawable loadTile(MapTileRequestState pState)
                throws CantContinueException;

        protected void onTileLoaderInit() {
            // Do nothing by default
        }

        protected void onTileLoaderShutdown() {
            // Do nothing by default
        }

        protected MapTileRequestState nextTile() {

            synchronized (mQueueLockObject) {
                // get the most recently accessed tile
                // - the last item in the iterator that's not already being
                // processed
                MapTileRequestState state = popFirstPending();
                if (state != null) {
                    mWorking.put(state.getMapTile(), state);
                    if (DEBUG_TILE_PROVIDERS) {
                        Log.d(TAG, "TileLoader.nextTile() on provider: "
                                + getName()
                                + " adding tile to working queue: "
                                + state.getMapTile());
                    }
                }
                return state;
            }
        }

        /**
         * A tile has loaded.
         */
        protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
            removeTileFromQueues(pState.getMapTile());
            pState.getCallback().mapTileRequestCompleted(pState, pDrawable);
        }

        /**
         * A tile has loaded but it's expired.
         * Return it <b>and</b> send request to next provider.
         */
        protected void tileLoadedExpired(final MapTileRequestState pState,
                final CacheableBitmapDrawable pDrawable) {
            if (DEBUG_TILE_PROVIDERS) {
                Log.d(TAG, "TileLoader.tileLoadedExpired() on provider: "
                        + getName()
                        + " with tile: "
                        + pState.getMapTile());
            }
            removeTileFromQueues(pState.getMapTile());
            pState.getCallback().mapTileRequestExpiredTile(pState, pDrawable);
        }

        protected void tileLoadedFailed(final MapTileRequestState pState) {
            if (DEBUG_TILE_PROVIDERS) {
                Log.i(TAG, "TileLoader.tileLoadedFailed() on provider: "
                        + getName()
                        + " with tile: "
                        + pState.getMapTile());
            }
            removeTileFromQueues(pState.getMapTile());
            pState.getCallback().mapTileRequestFailed(pState);
        }

        /**
         * This is a functor class of type Runnable. The run method is the encapsulated function.
         */
        @Override
        public void run() {
            // Make sure we're running with a background priority
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            onTileLoaderInit();

            MapTileRequestState state;
            Drawable result = null;
            while ((state = nextTile()) != null) {
                try {
                    result = null;
                    result = loadTile(state);
                } catch (final CantContinueException e) {
                    Log.e(TAG, "Tile loader can't continue: " + state.getMapTile(), e);
                    clearQueue();
                } catch (final Throwable e) {
                    Log.e(TAG, "Error downloading tile: " + state.getMapTile(), e);
                }

                if (result == null) {
                    tileLoadedFailed(state);
                } else if (BitmapUtils.isCacheDrawableExpired(result)) {
                    tileLoadedExpired(state, (CacheableBitmapDrawable) result);
                } else {
                    tileLoaded(state, result);
                }
            }

            onTileLoaderShutdown();
        }
    }

    /**
     * Thrown by a tile provider module in TileLoader.loadTile() to signal that it can no longer
     * function properly. This will typically clear the pending queue.
     */
    public class CantContinueException extends Exception {
        private static final long serialVersionUID = 146526524087765133L;

        public CantContinueException(final String pDetailMessage) {
            super(pDetailMessage);
        }

        public CantContinueException(final Throwable pThrowable) {
            super(pThrowable);
        }
    }

    private static final String TAG = "MapTileModuleLayerBase";
}

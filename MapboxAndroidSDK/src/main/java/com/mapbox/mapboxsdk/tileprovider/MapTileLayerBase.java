// Created by plusminus on 21:46:22 - 25.09.2008
package com.mapbox.mapboxsdk.tileprovider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.util.BitmapUtils;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * This is an abstract class. The tile provider is responsible for:
 * <ul>
 * <li>determining if a map tile is available,</li>
 * <li>notifying the client, via a callback handler</li>
 * </ul>
 * see {@link MapTile} for an overview of how tiles are served by this provider.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 */
public abstract class MapTileLayerBase implements IMapTileProviderCallback, TileLayerConstants {
    protected Context context;
    protected final MapTileCache mTileCache;
    private Handler mTileRequestCompleteHandler;
    private boolean mUseDataConnection = true;

    private ITileLayer mTileSource;
    protected String mCacheKey = "";

    /**
     * Attempts to get a Drawable that represents a {@link MapTile}. If the tile is not immediately
     * available this will return null and attempt to get the tile from known tile sources for
     * subsequent future requests. Note that this may return a {@link CacheableBitmapDrawable} in
     * which case you should follow proper handling procedures for using that Drawable or it may
     * reused while you are working with it.
     *
     * @see CacheableBitmapDrawable
     */
    public abstract Drawable getMapTile(MapTile pTile, boolean allowRemote);

    public abstract void detach();

    /**
     * Gets the minimum zoom level this tile provider can provide
     *
     * @return the minimum zoom level
     */
    public float getMinimumZoomLevel() {
        return mTileSource.getMinimumZoomLevel();
    }

    /**
     * Get the maximum zoom level this tile provider can provide.
     *
     * @return the maximum zoom level
     */
    public float getMaximumZoomLevel() {
        return mTileSource.getMaximumZoomLevel();
    }

    /**
     * Get the tile size in pixels this tile provider provides.
     *
     * @return the tile size in pixels
     */
    public int getTileSizePixels() {
        return mTileSource.getTileSizePixels();
    }

    /**
     * Get the tile provider bounding box.
     *
     * @return the tile source bounding box
     */
    public BoundingBox getBoundingBox() {
        return mTileSource.getBoundingBox();
    }

    /**
     * Get the tile provider center.
     *
     * @return the tile source center
     */
    public LatLng getCenterCoordinate() {
        return mTileSource.getCenterCoordinate();
    }

    /**
     * Get the tile provider suggested starting zoom.
     *
     * @return the tile suggested starting zoom
     */
    public float getCenterZoom() {
        return mTileSource.getCenterZoom();
    }

    /**
     * Sets the tile source for this tile provider.
     *
     * @param pTileSource the tile source
     */
    public void setTileSource(final ITileLayer pTileSource) {
        if (mTileSource != null) {
            mTileSource.detach();
        }
        mTileSource = pTileSource;
        if (mTileSource != null) {
            mCacheKey = mTileSource.getCacheKey();
        }
    }

    /**
     * Gets the tile source for this tile provider.
     *
     * @return the tile source
     */
    public ITileLayer getTileSource() {
        return mTileSource;
    }

    /**
     * Gets the cache key for that layer
     *
     * @return the cache key
     */
    public String getCacheKey() {
        return mCacheKey;
    }

    /**
     * Creates a {@link MapTileCache} to be used to cache tiles in memory.
     */
    public MapTileCache createTileCache(final Context aContext) {
        return new MapTileCache(aContext);
    }

    public MapTileLayerBase(final Context aContext, final ITileLayer pTileSource) {
        this(aContext, pTileSource, null);
    }

    public MapTileLayerBase(final Context aContext, final ITileLayer pTileSource,
            final Handler pDownloadFinishedListener) {
        this.context = aContext;
        mTileRequestCompleteHandler = pDownloadFinishedListener;
        mTileSource = pTileSource;
        mTileCache = this.createTileCache(aContext);
    }

    /**
     * Called by implementation class methods indicating that they have completed the request as
     * best it can. The tile is added to the cache, and a MAPTILE_SUCCESS_ID message is sent.
     *
     * @param pState the map tile request state object
     * @param pDrawable the Drawable of the map tile
     */
    @Override
    public void mapTileRequestCompleted(final MapTileRequestState pState,
            final Drawable pDrawable) {
        // tell our caller we've finished and it should update its view
        if (mTileRequestCompleteHandler != null) {
            Message msg = new Message();
            msg.obj = pState.getMapTile().getTileRect();
            msg.what = MapTile.MAPTILE_SUCCESS_ID;
            mTileRequestCompleteHandler.sendMessage(msg);
        } else {
            Log.e(TAG, "Failed to send map update request because mTileRequestCompleteHandler == null");
        }

        if (DEBUG_TILE_PROVIDERS) {
            Log.d(TAG, "MapTileLayerBase.mapTileRequestCompleted(): " + pState.getMapTile());
        }
    }

    /**
     * Called by implementation class methods indicating that they have failed to retrieve the
     * requested map tile. a MAPTILE_FAIL_ID message is sent.
     *
     * @param pState the map tile request state object
     */
    @Override
    public void mapTileRequestFailed(final MapTileRequestState pState) {
        if (mTileRequestCompleteHandler != null) {
            mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_FAIL_ID);
        }

        if (DEBUG_TILE_PROVIDERS) {
            Log.d(TAG, "MapTileLayerBase.mapTileRequestFailed(): " + pState.getMapTile());
        }
    }

    /**
     * Called by implementation class methods indicating that they have produced an expired result
     * that can be used but better results may be delivered later. The tile is added to the cache,
     * and a MAPTILE_SUCCESS_ID message is sent.
     *
     * @param pState the map tile request state object
     * @param pDrawable the Drawable of the map tile
     */
    @Override
    public void mapTileRequestExpiredTile(MapTileRequestState pState,
            CacheableBitmapDrawable pDrawable) {
        // Put the expired tile into the cache
        putExpiredTileIntoCache(pState.getMapTile(), pDrawable.getBitmap());

        // tell our caller we've finished and it should update its view
        if (mTileRequestCompleteHandler != null) {
            mTileRequestCompleteHandler.sendEmptyMessage(MapTile.MAPTILE_SUCCESS_ID);
        }

        if (DEBUG_TILE_PROVIDERS) {
            Log.i(TAG, "MapTileLayerBase.mapTileRequestExpiredTile(): " + pState.getMapTile());
        }
    }

    private void putTileIntoCacheInternal(final MapTile pTile, final Drawable pDrawable) {
        mTileCache.putTile(pTile, pDrawable);
    }

    private class CacheTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            putTileIntoCacheInternal((MapTile) params[0], (Drawable) params[1]);
            return null;
        }
    }

    private void putTileIntoCache(final MapTile pTile, final Drawable pDrawable) {
        if (pDrawable != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                (new CacheTask()).execute(pTile, pDrawable);
            } else {
                putTileIntoCacheInternal(pTile, pDrawable);
            }
        }
    }

    protected void putTileIntoCache(final MapTileRequestState pState, final Drawable pDrawable) {
        putTileIntoCache(pState.getMapTile(), pDrawable);
    }

    protected void removeTileFromCache(final MapTileRequestState pState) {
        mTileCache.removeTileFromMemory(pState.getMapTile());
    }

    public void putExpiredTileIntoCache(final MapTile pTile, final Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        CacheableBitmapDrawable drawable = mTileCache.putTileInMemoryCache(pTile, bitmap);
        BitmapUtils.setCacheDrawableExpired(drawable);
    }

    public void setTileRequestCompleteHandler(final Handler handler) {
        mTileRequestCompleteHandler = handler;
    }

    public void clearTileMemoryCache() {
        mTileCache.purgeMemoryCache();
    }

    public void memoryCacheNeedsMoreMemory(int numberOfTiles) {
        mTileCache.getCache().resizeMemoryForTiles(numberOfTiles);
    }

    public void clearTileDiskCache() {
        mTileCache.purgeDiskCache();
    }

    public void setDiskCacheEnabled(final boolean enabled) {
        mTileCache.setDiskCacheEnabled(enabled);
    }

    /**
     * Whether to use the network connection if it's available.
     */
    @Override
    public boolean useDataConnection() {
        return mUseDataConnection;
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param pMode if true use the network connection if it's available. if false don't use the
     * network connection even if it's available.
     */
    public void setUseDataConnection(final boolean pMode) {
        mUseDataConnection = pMode;
    }

    public boolean hasNoSource() {
        return mTileSource == null;
    }

    public CacheableBitmapDrawable getMapTileFromMemory(MapTile pTile) {
        return (mTileCache != null) ? mTileCache.getMapTileFromMemory(pTile) : null;
    }

    public CacheableBitmapDrawable createCacheableBitmapDrawable(Bitmap bitmap, MapTile aTile) {
        return (mTileCache != null) ? mTileCache.createCacheableBitmapDrawable(bitmap, aTile)
                : null;
    }

    public Bitmap getBitmapFromRemoved(final int width, final int height) {
        return (mTileCache != null) ? mTileCache.getBitmapFromRemoved(width, height) : null;
    }

    /**
     * If a given MapTile is present in this cache, remove it from memory.
     * @param aTile
     */
    public void removeTileFromMemory(final MapTile aTile) {
        if (mTileCache != null) {
            mTileCache.removeTileFromMemory(aTile);
        }
    }

    private static final String TAG = "MapTileLayerBase";
}

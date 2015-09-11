package com.mapbox.mapboxsdk.tileprovider.modules;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileCache;
import com.mapbox.mapboxsdk.tileprovider.MapTileRequestState;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.util.AppUtils;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.TileLoadedListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;
import java.util.concurrent.atomic.AtomicReference;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * The {@link MapTileDownloader} loads tiles from an HTTP server.
 */
public class MapTileDownloader extends MapTileModuleLayerBase {
    private static final String TAG = "MapTileDownloader";

    private final AtomicReference<TileLayer> mTileSource = new AtomicReference<>();
    private final AtomicReference<MapTileCache> mTileCache = new AtomicReference<>();

    private final NetworkAvailabilityCheck mNetworkAvailabilityCheck;
    private MapView mMapView;
    private boolean mUseDataConnection;
    boolean hdpi;

    public MapTileDownloader(final ITileLayer pTileSource, final MapTileCache pTileCache,
                             final NetworkAvailabilityCheck pNetworkAvailabilityCheck, final MapView mapView) {
        super(NUMBER_OF_TILE_DOWNLOAD_THREADS, TILE_DOWNLOAD_MAXIMUM_QUEUE_SIZE);
        mMapView = mapView;
        mUseDataConnection = true;
        if (pTileSource instanceof MBTilesLayer) {
            mUseDataConnection = false;
        }
        mTileCache.set(pTileCache);

        hdpi = AppUtils.isRunningOn2xOrGreaterScreen(mapView.getContext());
        Log.d(TAG, String.format("Going to use @2x tiles? '%b'", hdpi));

        mNetworkAvailabilityCheck = pNetworkAvailabilityCheck;
        setTileSource(pTileSource);
    }

    public ITileLayer getTileSource() {
        return mTileSource.get();
    }

    public MapTileCache getCache() {
        return mTileCache.get();
    }

    public boolean isNetworkAvailable() {
        return (mNetworkAvailabilityCheck == null
                || mNetworkAvailabilityCheck.getNetworkAvailable());
    }

    public TilesLoadedListener getTilesLoadedListener() {
        return mMapView.getTilesLoadedListener();
    }

    public TileLoadedListener getTileLoadedListener() {
        return mMapView.getTileLoadedListener();
    }

    @Override
    public boolean getUsesDataConnection() {
        return mUseDataConnection;
    }

    @Override
    protected String getName() {
        return "Online Tile Download Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "downloader";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public float getMinimumZoomLevel() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null ? tileLayer.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL);
    }

    @Override
    public float getMaximumZoomLevel() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null ? tileLayer.getMaximumZoomLevel() : MAXIMUM_ZOOMLEVEL);
    }

    @Override
    public BoundingBox getBoundingBox() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null ? tileLayer.getBoundingBox() : null);
    }

    @Override
    public LatLng getCenterCoordinate() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null ? tileLayer.getCenterCoordinate() : null);
    }

    @Override
    public float getCenterZoom() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null ? tileLayer.getCenterZoom()
                : (getMaximumZoomLevel() + getMinimumZoomLevel()) / 2);
    }

    @Override
    public int getTileSizePixels() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null) ? tileLayer.getTileSizePixels() : 0;
    }

    @Override
    public void setTileSource(final ITileLayer tileSource) {
        if (mTileSource.get() != null) {
            mTileSource.get().detach();
        }
        // We are only interested in TileLayer tile sources
        if (tileSource instanceof TileLayer) {
            mTileSource.set((TileLayer) tileSource);
        } else {
            // Otherwise shut down the tile downloader
            mTileSource.set(null);
        }
    }

    @Override
    public String getCacheKey() {
        TileLayer tileLayer = mTileSource.get();
        return (tileLayer != null) ? tileLayer.getCacheKey() : "";
    }

    protected class TileLoader extends MapTileModuleLayerBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState aState) throws CantContinueException {
            final MapTile tile = aState.getMapTile();
//            Log.d(TAG, "loadTile() with tile = '" + tile + "'");
            if (mTileCache != null && mTileCache.get().containsTileInDiskCache(tile)) {
//                Log.d(TAG, "tile found in Disk Cache, so returning it. tile = '" + tile + "'");
                return mTileCache.get().getMapTileFromDisk(tile);
            }
            TileLayer tileLayer = mTileSource.get();
            Drawable result =
                    (tileLayer != null) ? tileLayer.getDrawableFromTile(MapTileDownloader.this,
                            tile, hdpi) : null;
//            Log.d(TAG, "tileLayer.getDrawable() returning result = '" + result + "'");
            return result;
        }
    }

    private CacheableBitmapDrawable onTileLoaded(CacheableBitmapDrawable pDrawable) {
        return mMapView.getTileLoadedListener().onTileLoaded(pDrawable);
    }
}

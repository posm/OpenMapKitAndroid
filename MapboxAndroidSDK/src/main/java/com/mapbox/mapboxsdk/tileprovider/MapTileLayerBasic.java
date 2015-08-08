package com.mapbox.mapboxsdk.tileprovider;

import android.content.Context;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileDownloader;
import com.mapbox.mapboxsdk.tileprovider.modules.MapTileModuleLayerBase;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.util.SimpleRegisterReceiver;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * A base class for tile layers to built, this is a simple collection of tile sources.
 */
public class MapTileLayerBasic extends MapTileLayerArray implements IMapTileProviderCallback {
    Context mContext;
    MapView mMapView;

    /**
     * @param pContext
     * @param pTileSource
     * @param mapView
     */
    public MapTileLayerBasic(final Context pContext, final ITileLayer pTileSource,
            MapView mapView) {
        super(pContext, pTileSource, new SimpleRegisterReceiver(pContext));
        this.mContext = pContext;
        this.mMapView = mapView;

        this.setTileRequestCompleteHandler(mMapView.getTileRequestCompleteHandler());

        final MapTileDownloader downloaderProvider =
                new MapTileDownloader(pTileSource, mTileCache, mNetworkAvailabilityCheck, mMapView);

        for (MapTileModuleLayerBase provider : mTileProviderList) {
            if (provider.getClass().isInstance(MapTileDownloader.class)) {
                mTileProviderList.remove(provider);
            }
        }
        addTileSource(pTileSource);
    }

    @Override
    public void setTileSource(final ITileLayer aTileSource) {
        super.setTileSource(aTileSource);
        addTileSource(aTileSource);
    }

    public void setTileSources(final ITileLayer[] aTileSources) {
        super.setTileSource(null);
        synchronized (mTileProviderList) {
            mTileProviderList.clear();
        }
        for (ITileLayer source : aTileSources) {
            addTileSource(source);
        }
    }
    public void addTileSource(final ITileLayer pTileSource) {
        addTileSource(pTileSource, mTileProviderList.size());
    }

    public void addTileSource(final ITileLayer pTileSource, final int index) {
        if (pTileSource == null) {
            return;
        }
        final MapTileDownloader downloaderProvider =
                new MapTileDownloader(pTileSource, mTileCache, mNetworkAvailabilityCheck, mMapView);
        if (hasNoSource()) {
            mCacheKey = pTileSource.getCacheKey();
        }
        synchronized (mTileProviderList) {
            if (index < 0 || index > mTileProviderList.size()) {
                return;
            }
            mTileProviderList.add(index, downloaderProvider);
        }
    }

    public void removeTileSource(final int index) {
        synchronized (mTileProviderList) {
            if (index >= 0 & index < mTileProviderList.size()) {
                mTileProviderList.remove(index);
            }
        }
    }

    public void removeTileSource(final ITileLayer pTileSource) {
        synchronized (mTileProviderList) {
            for (MapTileModuleLayerBase provider : mTileProviderList) {
                if (provider.getTileSource() == pTileSource) {
                    mTileProviderList.remove(provider);
                    return;
                }
            }
        }
    }
}
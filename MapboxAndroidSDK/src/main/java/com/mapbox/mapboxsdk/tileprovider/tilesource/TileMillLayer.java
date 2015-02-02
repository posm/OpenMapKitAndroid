package com.mapbox.mapboxsdk.tileprovider.tilesource;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;

public class TileMillLayer extends WebSourceTileLayer implements MapboxConstants {

    private static final String BASE_URL = "http://%s:20008/tile/%s";

    public TileMillLayer(final String pHost, final String pMap, final float pMinZoom,
            final float pMaxZoom) {
        super(pHost, String.format(MAPBOX_LOCALE, BASE_URL, pHost, pMap));
        mName = "TileMill";
        mMinimumZoomLevel = pMinZoom;
        mMaximumZoomLevel = pMaxZoom;
    }

    public TileMillLayer(final String pHost, final String pMap) {
        this(pHost, pMap, TileLayerConstants.MINIMUM_ZOOMLEVEL,
                TileLayerConstants.MAXIMUM_ZOOMLEVEL);
    }

    public TileMillLayer(final String pMap) {
        this("localhost", pMap);
    }

    @Override
    public TileLayer setURL(final String aUrl) {
        super.setURL(aUrl + "/%d/%d/%d.png?updated=%d");
        return this;
    }

    @Override
    public String getTileURL(final MapTile aTile, boolean hdpi) {
        return String.format(MAPBOX_LOCALE, mUrl, aTile.getZ(), aTile.getX(), aTile.getY(),
                System.currentTimeMillis() / 1000L);
    }
}

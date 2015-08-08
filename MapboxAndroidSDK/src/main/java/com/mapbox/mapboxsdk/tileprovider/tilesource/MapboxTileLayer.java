package com.mapbox.mapboxsdk.tileprovider.tilesource;

import android.text.TextUtils;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.util.MapboxUtils;
import com.mapbox.mapboxsdk.views.util.constants.MapViewConstants;
import java.util.Locale;

/**
 * A convenience class to initialize tile layers that use Mapbox services.
 * Underneath, this initializes a WebSourceTileLayer, but provides conveniences
 * for retina tiles, initialization by ID, and loading over SSL.
 */
public class MapboxTileLayer extends TileJsonTileLayer implements MapViewConstants, MapboxConstants {
    private static final String TAG = "MapboxTileLayer";
    private String mId;

    /**
     * Initialize a new tile layer, directed at a hosted Mapbox tilesource.
     *
     * @param mapId a valid mapid, of the form account.map
     */
    public MapboxTileLayer(String mapId) {
        this(mapId, true);
    }

    public MapboxTileLayer(String mapId, boolean enableSSL) {
        super(mapId, mapId, enableSSL);
    }

    @Override
    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        mId = pId;
        super.initialize(pId, aUrl, enableSSL);
    }

    @Override
    public TileLayer setURL(final String aUrl) {
        if (!TextUtils.isEmpty(aUrl) && !aUrl.toLowerCase(Locale.US).contains("http://") && !aUrl.toLowerCase(Locale.US).contains("https://")) {
            super.setURL(MAPBOX_BASE_URL_V4 + aUrl + "/{z}/{x}/{y}{2x}.png?access_token=" + MapboxUtils.getAccessToken());
        } else {
            super.setURL(aUrl);
        }
        return this;
    }

    @Override
    protected String getBrandedJSONURL() {
        String url = String.format(MAPBOX_LOCALE, MAPBOX_BASE_URL_V4 + "%s.json?access_token=%s&secure=1", mId, MapboxUtils.getAccessToken());
        if (!mEnableSSL) {
            url = url.replace("https://", "http://");
            url = url.replace("&secure=1", "");
        }

        return url;
    }

    public String getCacheKey() {
        return mId;
    }
}

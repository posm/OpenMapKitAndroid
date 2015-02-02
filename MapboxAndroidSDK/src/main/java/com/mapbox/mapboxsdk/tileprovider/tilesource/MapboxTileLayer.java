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

    /**
     * Initialize a new Mapbox tile layer using V4 API requiring Access Tokens*
     * @param mapId MapID
     * @param accessToken Access Token
     */
    public MapboxTileLayer(String mapId,  String accessToken) {
        this(mapId);
        MapboxUtils.setAccessToken(accessToken);
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
        if (!TextUtils.isEmpty(aUrl) && !aUrl.toLowerCase(Locale.US).contains("http://")
                && !aUrl.toLowerCase(Locale.US).contains("https://")) {
            if (!TextUtils.isEmpty(MapboxUtils.getAccessToken())) {
                super.setURL(MAPBOX_BASE_URL_V4 + aUrl + "/{z}/{x}/{y}{2x}.png?access_token=" + MapboxUtils.getAccessToken());
            } else {
                super.setURL(MAPBOX_BASE_URL_V3 + aUrl + "/{z}/{x}/{y}{2x}.png");
            }
        } else {
            super.setURL(aUrl);
        }
        return this;
    }

    @Override
    protected String getBrandedJSONURL() {
        if (!TextUtils.isEmpty(MapboxUtils.getAccessToken())) {
            return String.format(MAPBOX_LOCALE, "http%s://api.tiles.mapbox.com/v4/%s.json?access_token=%s%s", (mEnableSSL ? "s" : ""),
                    mId, MapboxUtils.getAccessToken(), (mEnableSSL ? "&secure" : ""));
        }

        return String.format(MAPBOX_LOCALE, "http%s://api.tiles.mapbox.com/v3/%s.json%s", (mEnableSSL ? "s" : ""),
                mId, (mEnableSSL ? "?secure" : ""));
    }

    public String getCacheKey() {
        return mId;
    }
}

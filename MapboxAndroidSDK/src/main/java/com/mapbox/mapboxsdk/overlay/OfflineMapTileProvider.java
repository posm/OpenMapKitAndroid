package com.mapbox.mapboxsdk.overlay;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.exceptions.OfflineDatabaseException;
import com.mapbox.mapboxsdk.offline.OfflineMapDatabase;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import com.mapbox.mapboxsdk.util.MapboxUtils;
import java.io.ByteArrayInputStream;

public class OfflineMapTileProvider extends MapTileLayerBase implements MapboxConstants {

    private static final String TAG = "OfflineMapTileProvider";

    private OfflineMapDatabase offlineMapDatabase = null;

    public OfflineMapTileProvider(Context context, OfflineMapDatabase offlineMapDatabase) {
        super(context, null);
        this.offlineMapDatabase = offlineMapDatabase;
    }

    @Override
    public Drawable getMapTile(MapTile pTile, boolean allowRemote) {
        Log.d(TAG, String.format(MAPBOX_LOCALE, "getMapTile() with maptile path = '%s'", pTile.getPath()));
        try {
            // Build URL to match url in database
            String url = MapboxUtils.getMapTileURL(context, offlineMapDatabase.getMapID(), pTile.getZ(), pTile.getX(), pTile.getY(), offlineMapDatabase.getImageQuality());
            byte[] data = offlineMapDatabase.dataForURL(url);

            if (data == null || data.length == 0) {
                // No data found, just return null so that default gray screen is displayed.
                return null;
            }
            // Return the tile image
            return new BitmapDrawable(context.getResources(), new ByteArrayInputStream(data));
        } catch (OfflineDatabaseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void detach() {
        if (getTileSource() != null) {
            getTileSource().detach();
        }
    }
}

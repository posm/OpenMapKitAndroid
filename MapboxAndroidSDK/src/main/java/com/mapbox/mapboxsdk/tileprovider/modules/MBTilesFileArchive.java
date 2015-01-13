package com.mapbox.mapboxsdk.tileprovider.modules;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * An access layer to the MBTiles format. This is useful for offline tiles
 * that one would pre-package with an app.
 */
public class MBTilesFileArchive implements IArchiveFile {

    private final SQLiteDatabase mDatabase;

    // TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB);
    public static final String TABLE_TILES = "tiles";
    public static final String TABLE_METADATA = "metadata";
    public static final String COL_TILES_TILE_DATA = "tile_data";
    public static final String COL_VALUE = "value";

    public MBTilesFileArchive(final SQLiteDatabase pDatabase) {
        mDatabase = pDatabase;
    }

    public static MBTilesFileArchive getDatabaseFileArchive(final File pFile)
            throws SQLiteException {
        return new MBTilesFileArchive(SQLiteDatabase.openDatabase(pFile.getAbsolutePath(), null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY)
        );
    }

    @Override
    public InputStream getInputStream(final ITileLayer pTileSource, final MapTile pTile) {

        try {
            InputStream ret = null;
            final String[] tile = { COL_TILES_TILE_DATA };
            final String[] xyz = {
                    Integer.toString(pTile.getX()),
                    Double.toString(Math.pow(2, pTile.getZ()) - pTile.getY() - 1),
                    Integer.toString(pTile.getZ())
            };

            final Cursor cur = mDatabase.query(TABLE_TILES, tile,
                    "tile_column=? and tile_row=? and zoom_level=?", xyz, null, null, null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                ret = new ByteArrayInputStream(cur.getBlob(0));
            }
            cur.close();
            if (ret != null) {
                return ret;
            }
        } catch (final Throwable e) {
            Log.e(TAG, "Error getting db stream: " + pTile, e);
        }

        return null;
    }

    @Override
    public String toString() {
        return "MBTiles [mDatabase=" + mDatabase.getPath() + "]";
    }

    private String getStringValue(String key) {
        final String[] column = { COL_VALUE };
        final String[] query = { key };

        Cursor c =
                this.mDatabase.query(TABLE_METADATA, column, "name = ?", query, null, null, null);
        try {
            c.moveToFirst();
            return c.getString(0);
        } catch (Exception e) {
            return null;
        } finally {
            c.close();
        }
    }

    public float getMinZoomLevel() {
        String result = getStringValue("minzoom");
        if (result != null) {
            return Float.parseFloat(result);
        }
        return 0;
    }

    public float getMaxZoomLevel() {
        String result = getStringValue("maxzoom");
        if (result != null) {
            return Float.parseFloat(result);
        }
        return 22;
    }

    public String getName() {
        return getStringValue("name");
    }

    public String getType() {
        return getStringValue("template");
    }

    public String getVersion() {
        return getStringValue("version");
    }

    public String getDescription() {
        return getStringValue("description");
    }

    public String getAttribution() {
        return getStringValue("attribution");
    }

    public BoundingBox getBounds() {
        String result = getStringValue("bounds");
        if (result != null) {
            String[] boundsArray = result.split(",\\s*");
            return new BoundingBox(Double.parseDouble(boundsArray[3]),
                    Double.parseDouble(boundsArray[2]), Double.parseDouble(boundsArray[1]),
                    Double.parseDouble(boundsArray[0]));
        }
        return null;
    }

    public LatLng getCenter() {
        String result = getStringValue("center");
        if (result != null) {
            String[] centerArray = result.split(",\\s*");
            return new LatLng(Double.parseDouble(centerArray[0]),
                    Double.parseDouble(centerArray[1]), Double.parseDouble(centerArray[2]));
        }
        return null;
    }

    public void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    private static final String TAG = "MBTilesFileArchive";
}

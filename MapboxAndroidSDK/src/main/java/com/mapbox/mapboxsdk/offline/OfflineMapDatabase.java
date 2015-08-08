package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.exceptions.OfflineDatabaseException;
import java.util.Date;

public class OfflineMapDatabase implements MapboxConstants {

    private static final String TAG = "OfflineMapDatabase";

    private Context context;

    private SQLiteDatabase db;

    private String uniqueID;
    private String mapID;
    private boolean includesMetadata;
    private boolean includesMarkers;
    private RasterImageQuality imageQuality;
    private String path;
    private boolean invalid;
    private boolean initializedProperly = false;

    /**
     * Default Constructor
     *
     * @param context Context of Android app
     */
    public OfflineMapDatabase(Context context) {
        super();
        this.context = context;
    }

    /**
     * Constructor
     * @param context Context of Android app
     * @param mapID MapId
     */
    public OfflineMapDatabase(Context context, String mapID) {
        super();
        this.context = context;
        this.mapID = mapID;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getMapID() {
        return mapID;
    }

    public String getPath() {
        return path;
    }

    public RasterImageQuality getImageQuality() {
        return imageQuality;
    }

    public boolean initializeDatabase() {

        String uniqueID = sqliteMetadataForName("uniqueID");
        String mapID = sqliteMetadataForName("mapID");
        String includesMetadata = sqliteMetadataForName("includesMetadata");
        String includesMarkers = sqliteMetadataForName("includesMarkers");
        String imageQuality = sqliteMetadataForName("imageQuality");

        if (TextUtils.isEmpty(uniqueID)) {
            uniqueID = String.format(MAPBOX_LOCALE, "%s-%d", mapID, new Date().getTime() / 1000L);
        }

        if (!TextUtils.isEmpty(mapID) && !TextUtils.isEmpty(includesMetadata) && !TextUtils.isEmpty(includesMarkers) && !TextUtils.isEmpty(imageQuality)) {
            // Reaching this point means that the specified database file at path pointed to an sqlite file which had
            // all the required values in its metadata table. That means the file passed the test for being a valid
            // offline map database.
            //
            this.uniqueID = uniqueID;
            this.mapID = mapID;
            this.includesMetadata = "YES".equalsIgnoreCase(includesMetadata);
            this.includesMarkers = "YES".equalsIgnoreCase(includesMarkers);

            this.imageQuality = RasterImageQuality.getEnumForValue(Integer.parseInt(imageQuality));

            SQLiteDatabase db = database();
            this.path = db.getPath();

            this.initializedProperly = true;
        } else {
            // Reaching this point means the file at path isn't a valid offline map database, so we can't use it.
            Log.w(TAG, "Invalid offline map database.  Can't be used.");
        }
        return initializedProperly;
    }

    public byte[] dataForURL(String url) throws OfflineDatabaseException {
        byte[] data = sqliteDataForURL(url);
/*
        if (data == null || data.length == 0) {
            String reason = String.format("The offline database has no data for %s", url);
            throw new OfflineDatabaseException(reason);
        }
*/
        return data;
    }

    public void invalidate() {
        this.invalid = false;
    }

    public String sqliteMetadataForName(String name) {
        if (mapID == null) {
            return null;
        }

        SQLiteDatabase db = database();
        if (db == null) {
            return null;
        }

        String query = "SELECT " + OfflineDatabaseHandler.FIELD_METADATA_VALUE + " FROM " + OfflineDatabaseHandler.TABLE_METADATA + " WHERE " + OfflineDatabaseHandler.FIELD_METADATA_NAME + "=?;";
        String[] selectionArgs = new String[] { name };
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor == null) {
            return null;
        }

        String res = null;
        if (cursor.moveToFirst()) {
            res = cursor.getString(cursor.getColumnIndex(OfflineDatabaseHandler.FIELD_METADATA_VALUE));
        }
        cursor.close();
        return res;
    }

    public byte[] sqliteDataForURL(String url) {
        if (mapID == null) {
            return null;
        }
        SQLiteDatabase db = database();
        if (db == null) {
            return null;
        }

        String query = "SELECT " + OfflineDatabaseHandler.FIELD_RESOURCES_DATA + " FROM " + OfflineDatabaseHandler.TABLE_RESOURCES + " WHERE " + OfflineDatabaseHandler.FIELD_RESOURCES_URL + "=?;";
        String[] selectionArgs = new String[] { url };
        Cursor cursor = db.rawQuery(query, selectionArgs);
        if (cursor == null) {
            return null;
        }

        byte[] res = null;
        if (cursor.moveToFirst()) {
            res = cursor.getBlob(cursor.getColumnIndex(OfflineDatabaseHandler.FIELD_RESOURCES_DATA));
        }
        cursor.close();
        return res;
    }

    private SQLiteDatabase database() {
        if (db == null) {
            db = OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(mapID).getReadableDatabase();
        }
        if (!db.isOpen()) {
            db = null;
        }
        return db;
    }

    public void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        db = null;
    }
}

package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OfflineDatabaseHandler extends SQLiteOpenHelper {
    private static OfflineDatabaseHandler offlineDatabaseHandler;

    private static final String TAG = "OfflineDatabaseHandler";

    // All Static variables
    // Database Version
    public static final int DATABASE_VERSION = 2;

    // Table name(s)
    public static final String TABLE_METADATA = "metadata";
    public static final String TABLE_RESOURCES = "resources";

    // Table Fields
    public static final String FIELD_METADATA_NAME = "name";
    public static final String FIELD_METADATA_VALUE = "value";

    public static final String FIELD_RESOURCES_URL = "url";
    public static final String FIELD_RESOURCES_DATA = "data";
    public static final String FIELD_RESOURCES_STATUS = "status";

    /**
     * Constructor
     *
     * @param context Context
     */
    public OfflineDatabaseHandler(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate() called... Setting up application's database.");
        // Create The table(s)
        String metadata = "CREATE TABLE " + TABLE_METADATA + " (" + FIELD_METADATA_NAME + " TEXT UNIQUE, " + FIELD_METADATA_VALUE + " TEXT);";
        String resources = "CREATE TABLE " + TABLE_RESOURCES + " (" + FIELD_RESOURCES_URL + " TEXT UNIQUE, " + FIELD_RESOURCES_DATA + " BLOB, " + FIELD_RESOURCES_STATUS + " TEXT);";

        db.beginTransaction();

        try {
            db.execSQL(metadata);
            db.execSQL(resources);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "Error creating database: " + e.toString());
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("drop table if exists " + TABLE_METADATA);
        db.execSQL("drop table if exists " + TABLE_RESOURCES);
        onCreate(db);
    }
}

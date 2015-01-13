package com.mapbox.mapboxsdk.offline;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.constants.MathConstants;
import com.mapbox.mapboxsdk.geometry.CoordinateRegion;
import com.mapbox.mapboxsdk.util.AppUtils;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.mapbox.mapboxsdk.util.MapboxUtils;
import com.mapbox.mapboxsdk.util.NetworkUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OfflineMapDownloader implements MapboxConstants {

    private static final String TAG = "OfflineMapDownloader";

    private static OfflineMapDownloader offlineMapDownloader;

    private ArrayList<OfflineMapDownloaderListener> listeners;

    private Context context;

    /**
     * The possible states of the offline map downloader.
     */
    public enum MBXOfflineMapDownloaderState {
        /**
         * An offline map download job is in progress.
         */
        MBXOfflineMapDownloaderStateRunning,
        /**
         * An offline map download job is suspended and can be either resumed or canceled.
         */
        MBXOfflineMapDownloaderStateSuspended,
        /**
         * An offline map download job is being canceled.
         */
        MBXOfflineMapDownloaderStateCanceling,
        /**
         * The offline map downloader is ready to begin a new offline map download job.
         */
        MBXOfflineMapDownloaderStateAvailable
    }

    private String uniqueID;
    private String mapID;
    private boolean includesMetadata;
    private boolean includesMarkers;
    private RasterImageQuality imageQuality;
    private CoordinateRegion mapRegion;
    private int minimumZ;
    private int maximumZ;
    private MBXOfflineMapDownloaderState state;
    private int totalFilesWritten;
    private int totalFilesExpectedToWrite;


    private ArrayList<OfflineMapDatabase> mutableOfflineMapDatabases;

/*
    // Don't appear to be needed as there's one database per app for offline maps
    @property (nonatomic) NSString *partialDatabasePath;
    @property (nonatomic) NSURL *offlineMapDirectory;

    // Don't appear to be needed as as Android and Mapbox Android SDK provide these
    @property (nonatomic) NSOperationQueue *backgroundWorkQueue;
    @property (nonatomic) NSOperationQueue *sqliteQueue;
    @property (nonatomic) NSURLSession *dataSession;
    @property (nonatomic) NSInteger activeDataSessionTasks;
*/


    private OfflineMapDownloader(Context context) {
        super();
        this.context = context;

        listeners = new ArrayList<OfflineMapDownloaderListener>();

        mutableOfflineMapDatabases = new ArrayList<OfflineMapDatabase>();
        // Load OfflineMapDatabases from File System
        ContextWrapper cw = new ContextWrapper(context);
        for (String s : cw.databaseList()) {
            if (!s.toLowerCase().contains("partial") && !s.toLowerCase().contains("journal")) {
                // Setup Database Handler
                OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(s, true);

                // Create the Database Object
                OfflineMapDatabase omd = new OfflineMapDatabase(context, s);
                omd.initializeDatabase();
                mutableOfflineMapDatabases.add(omd);
            }
        }

        this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable;
    }

    public static OfflineMapDownloader getOfflineMapDownloader(Context context) {
        if (offlineMapDownloader == null) {
            offlineMapDownloader = new OfflineMapDownloader(context);
        }
        return offlineMapDownloader;
    }

    public boolean addOfflineMapDownloaderListener(OfflineMapDownloaderListener listener) {
        return listeners.add(listener);
    }

    public boolean removeOfflineMapDownloaderListener(OfflineMapDownloaderListener listener) {
        return listeners.remove(listener);
    }

/*
    Delegate Notifications
*/

    public void notifyDelegateOfStateChange() {
        for (OfflineMapDownloaderListener listener : listeners) {
            listener.stateChanged(this.state);
        }
    }

    public void notifyDelegateOfInitialCount() {
        for (OfflineMapDownloaderListener listener : listeners) {
            listener.initialCountOfFiles(this.totalFilesExpectedToWrite);
        }
    }

    public void notifyDelegateOfProgress() {
        for (OfflineMapDownloaderListener listener : listeners) {
            listener.progressUpdate(this.totalFilesWritten, this.totalFilesExpectedToWrite);
        }
    }

    public void notifyDelegateOfNetworkConnectivityError(Throwable error) {
        for (OfflineMapDownloaderListener listener : listeners) {
            listener.networkConnectivityError(error);
        }
    }

    public void notifyDelegateOfSqliteError(Throwable error) {
        for (OfflineMapDownloaderListener listener : listeners) {
            listener.sqlLiteError(error);
        }
    }

    public void notifyDelegateOfHTTPStatusError(int status, String url) {
        for (OfflineMapDownloaderListener listener : listeners) {
            listener.httpStatusError(new Exception(String.format("HTTP Status Error %d, for url = %s", status, url)));
        }
    }

    public void notifyDelegateOfCompletionWithOfflineMapDatabase(OfflineMapDatabase offlineMap) {
        for (OfflineMapDownloaderListener listener : listeners) {
            listener.completionOfOfflineDatabaseMap(offlineMap);
        }
    }
/*
    Implementation: download urls
*/

    public OfflineMapDatabase completeDatabaseAndInstantiateOfflineMapWithError() {
/*
        if (AppUtils.runningOnMainThread()) {
            Log.w(TAG, "completeDatabaseAndInstantiateOfflineMapWithError() running on main thread.  Returning null.");
            return null;
        }
*/
        // Rename database file (remove -PARTIAL) and update path in db object, update path in OfflineMapDatabase, create new Handler
        SQLiteDatabase db = OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(mapID).getReadableDatabase();
        String dbPath = db.getPath();
        db.close();

        if (dbPath.endsWith("-PARTIAL")) {
            // Rename SQLlite database file
            File oldDb = new File(dbPath);
            String newDb = dbPath.substring(0, dbPath.indexOf("-PARTIAL"));
            boolean result = oldDb.renameTo(new File(newDb));
            Log.i(TAG, "Result of rename = " + result + " for oldDb = '" + dbPath + "'; newDB = '" + newDb + "'");
        }

        // Update Database Handler
        OfflineDatabaseManager.getOfflineDatabaseManager(context).switchHandlerFromPartialToRegular(mapID);

        // Create DB object and return
        OfflineMapDatabase offlineMapDatabase = new OfflineMapDatabase(context, mapID);
        // Initialized with data from database
        offlineMapDatabase.initializeDatabase();
        return offlineMapDatabase;

        // Create new OfflineMapDatabase and load with recently downloaded data
/*
        // Rename the file using a unique prefix
        //
        CFUUIDRef uuid = CFUUIDCreate(kCFAllocatorDefault);
        CFStringRef uuidString = CFUUIDCreateString(kCFAllocatorDefault, uuid);
        NSString *newFilename = [NSString stringWithFormat:@"%@.complete",uuidString];
        NSString *newPath = [[_offlineMapDirectory URLByAppendingPathComponent:newFilename] path];
        CFRelease(uuidString);
        CFRelease(uuid);
        [[NSFileManager defaultManager] moveItemAtPath:_partialDatabasePath toPath:newPath error:error];

        // If the move worked, instantiate and return offline map database
        //
        if(error && *error)
        {
            return nil;
        }
        else
        {
            return [[MBXOfflineMapDatabase alloc] initWithContentsOfFile:newPath];
        }
*/
    }


    public void startDownloading() {
/*
        // Shouldn't need to check as all downloading will happen in background thread
        if (AppUtils.runningOnMainThread()) {
            Log.w(TAG, "startDownloading() is running on main thread.  Returning.");
            return;
        }
*/

        // Update expected files numbers (totalFilesExpectedToWrite and totalFilesWritten)
        sqliteQueryWrittenAndExpectedCountsWithError();
        Log.d(TAG, String.format("totalFilesExpectedToWrite = %d, totalFilesWritten = %d", this.totalFilesExpectedToWrite, this.totalFilesWritten));

//        [_sqliteQueue addOperationWithBlock:^{
        // Get the actual URLs
        ArrayList<String> urls = sqliteReadArrayOfOfflineMapURLsToBeDownloadLimit(-1);
        Log.d(TAG, String.format("number of urls to download = %d", urls.size()));

        int totalDiff = this.totalFilesExpectedToWrite - this.totalFilesWritten;
        if (urls.size() != totalDiff) {
            // Something is off
            Log.w(TAG, String.format("totalDiff %d does not equal urls size of %d.  This is a problem.  Returning.", totalDiff, urls.size()));
            return;
        } else if (urls.size() == 0 && totalDiff == 0) {
            // All files are downloaded, but hasn't been persisted yet.
            finishUpDownloadProcess();
            return;
        }

        for (final String url : urls) {
/*
            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.w(TAG, "Network is no longer available.");
//                    [self notifyDelegateOfNetworkConnectivityError:error];
            }
*/

            AsyncTask<String, Void, Void> foo = new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    HttpURLConnection conn = null;
                    try {
                        conn = NetworkUtils.getHttpURLConnection(new URL(params[0]));
                        Log.d(TAG, "URL to download = " + conn.getURL().toString());
                        conn.setConnectTimeout(60000);
                        conn.connect();
                        int rc = conn.getResponseCode();
                        if (rc != HttpURLConnection.HTTP_OK) {
                            String msg = String.format("HTTP Error connection.  Response Code = %d", rc);
                            Log.w(TAG, msg);
                            notifyDelegateOfHTTPStatusError(rc, params[0]);
                            throw new IOException(msg);
                        }

                        ByteArrayOutputStream bais = new ByteArrayOutputStream();
                        InputStream is = null;
                        try {
                            is = conn.getInputStream();
                            // Read 4K at a time
                            byte[] byteChunk = new byte[4096];
                            int n;

                            while ((n = is.read(byteChunk)) > 0) {
                                bais.write(byteChunk, 0, n);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, String.format("Failed while reading bytes from %s: %s", conn.getURL().toString(), e.getMessage()));
                            e.printStackTrace();
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            conn.disconnect();
                        }
                        sqliteSaveDownloadedData(bais.toByteArray(), url);
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }

                    return null;
                }
            };
            foo.execute(url);
            // This is the last line of the for loop
        }
    }

/*
    Implementation: sqlite stuff
*/

    public void sqliteSaveDownloadedData(byte[] data, String url) {
        if (AppUtils.runningOnMainThread()) {
            Log.w(TAG, "trying to run sqliteSaveDownloadedData() on main thread. Return.");
            return;
        }
//        assert(_activeDataSessionTasks > 0);

//        [_sqliteQueue addOperationWithBlock:^{

        // Bail out if the state has changed to canceling, suspended, or available
        //
        if (this.state != MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning) {
            Log.w(TAG, "sqliteSaveDownloadedData() is not in a Running state so bailing.  State = " + this.state);
            return;
        }

        // Open the database read-write and multi-threaded. The slightly obscure c-style variable names here and below are
        // used to stay consistent with the sqlite documentaion.
        // Continue by inserting an image blob into the data table
        //
        SQLiteDatabase db = OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(mapID).getWritableDatabase();
        db.beginTransaction();

//      String query2 = "INSERT INTO data(value) VALUES(?);";
        ContentValues values = new ContentValues();
        values.put(OfflineDatabaseHandler.FIELD_DATA_VALUE, data);
        db.insert(OfflineDatabaseHandler.TABLE_DATA, null, values);

//      [query appendFormat:@"UPDATE resources SET status=200,id=last_insert_rowid() WHERE url='%@';\n",[url absoluteString]];
        db.execSQL(String.format("UPDATE %s SET %s=200, %s=last_insert_rowid() WHERE %s='%s';", OfflineDatabaseHandler.TABLE_RESOURCES, OfflineDatabaseHandler.FIELD_RESOURCES_STATUS, OfflineDatabaseHandler.FIELD_RESOURCES_ID, OfflineDatabaseHandler.FIELD_RESOURCES_URL, url));
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

/*
        if(error)
        {
            // Oops, that didn't work. Notify the delegate.
            //
            [self notifyDelegateOfSqliteError:error];
        }
        else
        {
*/
        // Update the progress
        //
        this.totalFilesWritten += 1;
        notifyDelegateOfProgress();
        Log.d(TAG, "totalFilesWritten = " + this.totalFilesWritten + "; totalFilesExpectedToWrite = " + this.totalFilesExpectedToWrite);

        // If all the downloads are done, clean up and notify the delegate
        //
        if (this.totalFilesWritten >= this.totalFilesExpectedToWrite) {
            finishUpDownloadProcess();
        }
/*
        }
*/

        // If this was the last of a batch of urls in the data session's download queue, and there are more urls
        // to be downloaded, get another batch of urls from the database and keep working.
        //
/*
        if(activeDataSessionTasks > 0)
        {
            _activeDataSessionTasks -= 1;
        }
        if(_activeDataSessionTasks == 0 && _totalFilesWritten < _totalFilesExpectedToWrite)
        {
            [self startDownloading];
        }
*/
    }

    private void finishUpDownloadProcess() {
        if (this.state == MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning) {
            Log.i(TAG, "Just finished downloading all materials.  Persist the OfflineMapDatabase, change the state, and call it a day.");
            // This is what to do when we've downloaded all the files
            //
            // Populate OfflineMapDatabase object and persist it
            OfflineMapDatabase offlineMap = completeDatabaseAndInstantiateOfflineMapWithError();
            if (offlineMap != null) {
                this.mutableOfflineMapDatabases.add(offlineMap);
            }
            notifyDelegateOfCompletionWithOfflineMapDatabase(offlineMap);

            this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable;
            notifyDelegateOfStateChange();
        }
    }

    public ArrayList<String> sqliteReadArrayOfOfflineMapURLsToBeDownloadLimit(int limit) {
        ArrayList<String> results = new ArrayList<String>();
        if (AppUtils.runningOnMainThread()) {
            Log.w(TAG, "Attempting to run sqliteReadArrayOfOfflineMapURLsToBeDownloadLimit() on main thread.  Returning.");
            return results;
        }

        // Read up to limit undownloaded urls from the offline map database
        //
        String query = String.format("SELECT %s FROM %s WHERE %s IS NULL", OfflineDatabaseHandler.FIELD_RESOURCES_URL, OfflineDatabaseHandler.TABLE_RESOURCES, OfflineDatabaseHandler.FIELD_RESOURCES_STATUS);
        if (limit > 0) {
            query = query + String.format(" LIMIT %d", limit);
        }
        query = query + ";";

        // Open the database
        SQLiteDatabase db = OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(mapID).getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                results.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return results;
    }

    public boolean sqliteQueryWrittenAndExpectedCountsWithError() {
        // NOTE: Unlike most of the sqlite code, this method is written with the expectation that it can and will be called on the main
        //       thread as part of init. This is also meant to be used in other contexts throught the normal serial operation queue.

        // Calculate how many files need to be written in total and how many of them have been written already
        //
        String query = "SELECT COUNT(url) AS totalFilesExpectedToWrite, (SELECT COUNT(url) FROM resources WHERE status IS NOT NULL) AS totalFilesWritten FROM resources;";

        boolean success = false;
        SQLiteDatabase db = OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(mapID).getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        this.totalFilesExpectedToWrite = cursor.getInt(0);
        this.totalFilesWritten = cursor.getInt(1);
        cursor.close();
        db.close();
        success = true;

        return success;
    }

    public boolean sqliteCreateDatabaseUsingMetadata(Hashtable<String, String> metadata, List<String> urlStrings) {
        if (AppUtils.runningOnMainThread()) {
            Log.w(TAG, "sqliteCreateDatabaseUsingMetadata() running on main thread.  Returning.");
            return false;
        }

        boolean success = false;

        // Build a query to populate the database (map metadata and list of map resource urls)
        //
/*
        NSMutableString *query = [[NSMutableString alloc] init];
        [query appendString:@"PRAGMA foreign_keys=ON;\n"];
        [query appendString:@"BEGIN TRANSACTION;\n"];
        [query appendString:@"CREATE TABLE metadata (name TEXT UNIQUE, value TEXT);\n"];
        [query appendString:@"CREATE TABLE data (id INTEGER PRIMARY KEY, value BLOB);\n"];
        [query appendString:@"CREATE TABLE resources (url TEXT UNIQUE, status TEXT, id INTEGER REFERENCES data);\n"];
*/
        SQLiteDatabase db = OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(mapID).getWritableDatabase();
        db.beginTransaction();
        for (String key : metadata.keySet()) {
            ContentValues cv = new ContentValues();
            cv.put(OfflineDatabaseHandler.FIELD_METADATA_NAME, key);
            cv.put(OfflineDatabaseHandler.FIELD_METADATA_VALUE, metadata.get(key));
            db.replace(OfflineDatabaseHandler.TABLE_METADATA, null, cv);
        }
        for (String url : urlStrings) {
            ContentValues cv = new ContentValues();
            cv.put(OfflineDatabaseHandler.FIELD_RESOURCES_URL, url);
            db.insert(OfflineDatabaseHandler.TABLE_RESOURCES, null, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        this.totalFilesExpectedToWrite = urlStrings.size();
        this.totalFilesWritten = 0;
        success = true;
/*
        // Open the database read-write and multi-threaded. The slightly obscure c-style variable names here and below are
        // used to stay consistent with the sqlite documentaion.
        sqlite3 *db;
        int rc;
        const char *filename = [_partialDatabasePath cStringUsingEncoding:NSUTF8StringEncoding];
        rc = sqlite3_open_v2(filename, &db, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, NULL);
        if (rc)
        {
            // Opening the database failed... something is very wrong.
            //
            if(error != NULL)
            {
                *error = [NSError mbx_errorCannotOpenOfflineMapDatabase:_partialDatabasePath sqliteError:sqlite3_errmsg(db)];
            }
            sqlite3_close(db);
        }
        else
        {
            // Success! Creating the database file worked, so now populate the tables we'll need to hold the offline map
            //
            const char *zSql = [query cStringUsingEncoding:NSUTF8StringEncoding];
            char *errmsg;
            sqlite3_exec(db, zSql, NULL, NULL, &errmsg);
            if(error && errmsg != NULL)
            {
                *error = [NSError mbx_errorQueryFailedForOfflineMapDatabase:_partialDatabasePath sqliteError:errmsg];
                sqlite3_free(errmsg);
            }
            sqlite3_close(db);
            success = YES;
        }
*/
        return success;
    }

/*
    API: Begin an offline map download
*/

    public void beginDownloadingMapID(String mapID, CoordinateRegion mapRegion, Integer minimumZ, Integer maximumZ) {
        beginDownloadingMapID(mapID, mapRegion, minimumZ, maximumZ, true, true, RasterImageQuality.MBXRasterImageQualityFull);
    }

    public void beginDownloadingMapID(String mapID, CoordinateRegion mapRegion, Integer minimumZ, Integer maximumZ, boolean includeMetadata, boolean includeMarkers) {
        beginDownloadingMapID(mapID, mapRegion, minimumZ, maximumZ, includeMetadata, includeMarkers, RasterImageQuality.MBXRasterImageQualityFull);
    }

    public void beginDownloadingMapID(String mapID, CoordinateRegion mapRegion, Integer minimumZ, Integer maximumZ,
                                      boolean includeMetadata, boolean includeMarkers, RasterImageQuality imageQuality) {
        if (state != MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable) {
            Log.w(TAG, "state doesn't equal MBXOfflineMapDownloaderStateAvailable so return.  state = " + state);
            return;
        }

        // Make sure this completed map doesn't exist already
        if (isMapIdAlreadyAnOfflineMapDatabase(mapID)) {
            Log.w(TAG, String.format("MapId '%s' has already been downloaded.  Please delete it before trying to download again.", mapID));
            return;
        }

//        [self setUpNewDataSession];

//        [_backgroundWorkQueue addOperationWithBlock:^{

        // Start a download job to retrieve all the resources needed for using the specified map offline
        //
        this.uniqueID = UUID.randomUUID().toString();
        this.mapID = mapID;
        this.includesMetadata = includeMetadata;
        this.includesMarkers = includeMarkers;
        this.imageQuality = imageQuality;
        this.mapRegion = mapRegion;
        this.minimumZ = minimumZ;
        this.maximumZ = maximumZ;
        this.state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning;
//        [self notifyDelegateOfStateChange];

        final Hashtable<String, String> metadataDictionary = new Hashtable<String, String>();
        metadataDictionary.put("uniqueID", this.uniqueID);
        metadataDictionary.put("mapID", this.mapID);
        metadataDictionary.put("includesMetadata", this.includesMetadata ? "YES" : "NO");
        metadataDictionary.put("includesMarkers", this.includesMarkers ? "YES" : "NO");
        metadataDictionary.put("imageQuality", String.format("%d", this.imageQuality.getValue()));
        metadataDictionary.put("region_latitude", String.format("%.8f", this.mapRegion.getCenter().getLatitude()));
        metadataDictionary.put("region_longitude", String.format("%.8f", this.mapRegion.getCenter().getLongitude()));
        metadataDictionary.put("region_latitude_delta", String.format("%.8f", this.mapRegion.getSpan().getLatitudeSpan()));
        metadataDictionary.put("region_longitude_delta", String.format("%.8f", this.mapRegion.getSpan().getLongitudeSpan()));
        metadataDictionary.put("minimumZ", String.format("%d", this.minimumZ));
        metadataDictionary.put("maximumZ", String.format("%d", this.maximumZ));

        final ArrayList<String> urls = new ArrayList<String>();

        String version = "v3";
        String dataName = "markers.geojson";    // Only using API v3 for now
//        NSString *dataName = ([MBXMapKit accessToken] ? @"features.json" : @"markers.geojson");
//        NSString *accessToken = ([MBXMapKit accessToken] ? [@"access_token=" stringByAppendingString:[MBXMapKit accessToken]] : nil);

        // Include URLs for the metadata and markers json if applicable
        //
        if (includeMetadata) {
            urls.add(String.format(MAPBOX_BASE_URL + "%s.json?secure%s", this.mapID, ""));
        }
        if (includeMarkers) {
            urls.add(String.format(MAPBOX_BASE_URL + "%s/%s%s", this.mapID, dataName, ""));
        }

        // Loop through the zoom levels and lat/lon bounds to generate a list of urls which should be included in the offline map
        //
        double minLat = this.mapRegion.getCenter().getLatitude() - (this.mapRegion.getSpan().getLatitudeSpan() / 2.0);
        double maxLat = minLat + this.mapRegion.getSpan().getLatitudeSpan();
        double minLon = this.mapRegion.getCenter().getLongitude() - (this.mapRegion.getSpan().getLongitudeSpan() / 2.0);
        double maxLon = minLon + this.mapRegion.getSpan().getLongitudeSpan();
        int minX;
        int maxX;
        int minY;
        int maxY;
        int tilesPerSide;
        for (int zoom = minimumZ; zoom <= maximumZ; zoom++) {
            tilesPerSide = Double.valueOf(Math.pow(2.0, zoom)).intValue();
            minX = Double.valueOf(Math.floor(((minLon + 180.0) / 360.0) * tilesPerSide)).intValue();
            maxX = Double.valueOf(Math.floor(((maxLon + 180.0) / 360.0) * tilesPerSide)).intValue();
            minY = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(maxLat * MathConstants.PI / 180.0) + 1.0 / Math.cos(maxLat * MathConstants.PI / 180.0)) / MathConstants.PI)) / 2.0 * tilesPerSide)).intValue();
            maxY = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(minLat * MathConstants.PI / 180.0) + 1.0 / Math.cos(minLat * MathConstants.PI / 180.0)) / MathConstants.PI)) / 2.0 * tilesPerSide)).intValue();
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    urls.add(MapboxUtils.getMapTileURL(this.mapID, zoom, x, y, this.imageQuality));
                }
            }
        }
        Log.i(TAG, "Number of URLs so far: " + urls.size());

        // Determine if we need to add marker icon urls (i.e. parse markers.geojson/features.json), and if so, add them
        //
        if (includeMarkers) {
            String dName = "markers.geojson";
            final String geojson = String.format(MAPBOX_BASE_URL + "%s/%s", this.mapID, dName);

            if (!NetworkUtils.isNetworkAvailable(context)) {
                // We got a session level error which probably indicates a connectivity problem such as airplane mode.
                // Since we must fetch and parse markers.geojson/features.json in order to determine which marker icons need to be
                // added to the list of urls to download, the lack of network connectivity is a non-recoverable error
                // here.
                //
                // TODO
/*
                [self notifyDelegateOfNetworkConnectivityError:error];
                [self cancelImmediatelyWithError:error];
*/
                return;
            }

            AsyncTask<Void, Void, Void> foo = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        HttpURLConnection conn = NetworkUtils.getHttpURLConnection(new URL(geojson));
                        conn.setConnectTimeout(60000);
                        conn.connect();
                        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            throw new IOException();
                        }

                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
                        String jsonText = DataLoadingUtils.readAll(rd);

                        // The marker geojson was successfully retrieved, so parse it for marker icons. Note that we shouldn't
                        // try to save it here, because it may already be in the download queue and saving it twice will mess
                        // up the count of urls to be downloaded!
                        //
                        Set<String> markerIconURLStrings = new HashSet<String>();
                        markerIconURLStrings.addAll(parseMarkerIconURLStringsFromGeojsonData(jsonText));
                        Log.i(TAG, "Number of markerIconURLs = " + markerIconURLStrings.size());
                        if (markerIconURLStrings.size() > 0) {
                            urls.addAll(markerIconURLStrings);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        // The url for markers.geojson/features.json didn't work (some maps don't have any markers). Notify the delegate of the
                        // problem, and stop attempting to add marker icons, but don't bail out on whole the offline map download.
                        // The delegate can decide for itself whether it wants to continue or cancel.
                        //
                        // TODO
                        e.printStackTrace();
/*
                        [self notifyDelegateOfHTTPStatusError:((NSHTTPURLResponse *)response).statusCode url:response.URL];
*/
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Log.i(TAG, "Done figuring out marker icons, so now start downloading everything.");

                    // ==========================================================================================================
                    // == WARNING! WARNING! WARNING!                                                                           ==
                    // == This stuff is a duplicate of the code immediately below it, but this copy is inside of a completion  ==
                    // == block while the other isn't. You will be sad and confused if you try to eliminate the "duplication". ==
                    //===========================================================================================================
                    startDownloadProcess(metadataDictionary, urls);
                }
            };
            foo.execute();
        } else {
            Log.i(TAG, "No marker icons to worry about, so just start downloading.");
            // There aren't any marker icons to worry about, so just create database and start downloading
            startDownloadProcess(metadataDictionary, urls);
        }
    }

    /**
     * Private method for Starting the Whole Download Process
     *
     * @param metadata Metadata
     * @param urls     Map urls
     */
    private void startDownloadProcess(final Hashtable<String, String> metadata, final List<String> urls) {
        AsyncTask<Void, Void, Thread> startDownload = new AsyncTask<Void, Void, Thread>() {
            @Override
            protected Thread doInBackground(Void... params) {
                // Do database creation / io on background thread
                if (!sqliteCreateDatabaseUsingMetadata(metadata, urls)) {
                    cancelImmediatelyWithError("Map Database wasn't created");
                    return null;
                }
                notifyDelegateOfInitialCount();
                startDownloading();
                return null;
            }

        };

        // Create the database and start the download
        startDownload.execute();
    }


    public Set<String> parseMarkerIconURLStringsFromGeojsonData(String data) {
        HashSet<String> iconURLStrings = new HashSet<String>();

        JSONObject simplestyleJSONDictionary = null;
        try {
            simplestyleJSONDictionary = new JSONObject(data);

            // Find point features in the markers dictionary (if there are any) and add them to the map.
            //
            JSONArray markers = simplestyleJSONDictionary.getJSONArray("features");

            if (markers != null && markers.length() > 0) {
                for (int lc = 0; lc < markers.length(); lc++) {
                    Object value = markers.get(lc);
                    if (value instanceof JSONObject) {
                        JSONObject feature = (JSONObject) value;
                        String type = feature.getJSONObject("geometry").getString("type");

                        if ("Point".equals(type)) {
                            String size = feature.getJSONObject("properties").getString("marker-size");
                            String color = feature.getJSONObject("properties").getString("marker-color");
                            String symbol = feature.getJSONObject("properties").getString("marker-symbol");
                            if (!TextUtils.isEmpty(size) && !TextUtils.isEmpty(color) && !TextUtils.isEmpty(symbol)) {
                                String markerURL = MapboxUtils.markerIconURL(size, symbol, color);
                                if (!TextUtils.isEmpty(markerURL)) {
                                    iconURLStrings.add(markerURL);

                                }
                            }
                        }
                    }
                    // This is the last line of the loop
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return only the unique icon urls
        //
        return iconURLStrings;
    }

    public void cancelImmediatelyWithError(String error) {
        // TODO
/*
        // Creating the database failed for some reason, so clean up and change the state back to available
        //
        state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateCanceling;
        [self notifyDelegateOfStateChange];

        if([_delegate respondsToSelector:@selector(offlineMapDownloader:didCompleteOfflineMapDatabase:withError:)])
        {
            dispatch_async(dispatch_get_main_queue(), ^(void){
                    [_delegate offlineMapDownloader:self didCompleteOfflineMapDatabase:nil withError:error];
            });
        }

        [_dataSession invalidateAndCancel];
        [_sqliteQueue cancelAllOperations];

        [_sqliteQueue addOperationWithBlock:^{
        [self setUpNewDataSession];
        _totalFilesWritten = 0;
        _totalFilesExpectedToWrite = 0;

        [[NSFileManager defaultManager] removeItemAtPath:_partialDatabasePath error:nil];

        state = MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable;
        [self notifyDelegateOfStateChange];
    }];
*/
    }

/*
    API: Control an in-progress offline map download
*/

    public void cancel() {
        Log.d(TAG, "cancel called with state = " + state);
/*
        if (state != MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateCanceling && state != MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateAvailable) {
            // Stop a download job and discard the associated files
            //
            [_backgroundWorkQueue addOperationWithBlock:^{
            _state = MBXOfflineMapDownloaderStateCanceling;
            [self notifyDelegateOfStateChange];

            [_dataSession invalidateAndCancel];
            [_sqliteQueue cancelAllOperations];

            [_sqliteQueue addOperationWithBlock:^{
                [self setUpNewDataSession];
                _totalFilesWritten = 0;
                _totalFilesExpectedToWrite = 0;
                [[NSFileManager defaultManager] removeItemAtPath:_partialDatabasePath error:nil];

                if([_delegate respondsToSelector:@selector(offlineMapDownloader:didCompleteOfflineMapDatabase:withError:)])
                {
                    NSError *canceled = [NSError mbx_errorWithCode:MBXMapKitErrorCodeDownloadingCanceled reason:@"The download job was canceled" description:@"Download canceled"];
                    dispatch_async(dispatch_get_main_queue(), ^(void){
                            [_delegate offlineMapDownloader:self didCompleteOfflineMapDatabase:nil withError:canceled];
                    });
                }

                _state = MBXOfflineMapDownloaderStateAvailable;
                [self notifyDelegateOfStateChange];
            }];

            }
        }
*/
    }

    public void resume() {
        if (state != MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateSuspended) {
            return;
        }
/*
        // Resume a previously suspended download job
        //
        [_backgroundWorkQueue addOperationWithBlock:^{
            _state = MBXOfflineMapDownloaderStateRunning;
            [self startDownloading];
            [self notifyDelegateOfStateChange];
        }];
*/
    }

    public void suspend() {
        Log.d(TAG, "suspend called with state = " + state);
/*
        if (state == MBXOfflineMapDownloaderState.MBXOfflineMapDownloaderStateRunning) {
            // Stop a download job, preserving the necessary state to resume later
            //
            [_backgroundWorkQueue addOperationWithBlock:^{
                [_sqliteQueue cancelAllOperations];
                _state = MBXOfflineMapDownloaderStateSuspended;
                _activeDataSessionTasks = 0;
                [self notifyDelegateOfStateChange];
            }];
        }
*/
    }

/*
    API: Access or delete completed offline map databases on disk
*/

    public ArrayList<OfflineMapDatabase> getMutableOfflineMapDatabases() {
        // Return an array with offline map database objects representing each of the *complete* map databases on disk
        return mutableOfflineMapDatabases;
    }

    public boolean isMapIdAlreadyAnOfflineMapDatabase(String mapId) {
        for (OfflineMapDatabase db : getMutableOfflineMapDatabases()) {
            if (db.getMapID().equals(mapId)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeOfflineMapDatabase(OfflineMapDatabase offlineMapDatabase) {
        // Mark the offline map object as invalid in case there are any references to it still floating around
        //
        offlineMapDatabase.invalidate();

        // Remove the offline map object from the array and delete it's backing database
        //
        mutableOfflineMapDatabases.remove(offlineMapDatabase);

        // Remove Offline Database SQLite file
        SQLiteDatabase db = OfflineDatabaseManager.getOfflineDatabaseManager(context).getOfflineDatabaseHandlerForMapId(offlineMapDatabase.getMapID()).getReadableDatabase();
        String dbPath = db.getPath();
        db.close();

        File dbFile = new File(dbPath);
        boolean result = dbFile.delete();
        Log.i(TAG, String.format("Result of removing database file: %s", result));
        return result;
    }

    public boolean removeOfflineMapDatabaseWithID(String mid) {
        for (OfflineMapDatabase database : getMutableOfflineMapDatabases()) {
            if (database.getMapID().equals(mid)) {
                return removeOfflineMapDatabase(database);
            }
        }
        return false;
    }
}

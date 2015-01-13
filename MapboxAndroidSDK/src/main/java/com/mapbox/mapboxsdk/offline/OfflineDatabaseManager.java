package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.text.TextUtils;

import java.util.Hashtable;

public class OfflineDatabaseManager {

    private static OfflineDatabaseManager offlineDatabaseManager = null;

    private Hashtable<String, OfflineDatabaseHandler> databaseHandlers = null;

    private static Context context = null;

    private OfflineDatabaseManager() {
        super();
        databaseHandlers = new Hashtable<String, OfflineDatabaseHandler>();
    }

    public static OfflineDatabaseManager getOfflineDatabaseManager(Context ctx) {
        if (offlineDatabaseManager == null) {
            offlineDatabaseManager = new OfflineDatabaseManager();
        }
        context = ctx;
        return offlineDatabaseManager;
    }

    public OfflineDatabaseHandler getOfflineDatabaseHandlerForMapId(String mapId) {
        if (databaseHandlers.containsKey(mapId.toLowerCase())) {
            return databaseHandlers.get(mapId);
        }

        OfflineDatabaseHandler dbh = new OfflineDatabaseHandler(context, mapId.toLowerCase() + "-PARTIAL");
        databaseHandlers.put(mapId.toLowerCase(), dbh);
        return dbh;
    }

    public OfflineDatabaseHandler getOfflineDatabaseHandlerForMapId(String mapId, boolean fromFileSystem) {
        if (!fromFileSystem) {
            return getOfflineDatabaseHandlerForMapId(mapId);
        }

        String key = mapId.toLowerCase();
        if (databaseHandlers.containsKey(key)) {
            return databaseHandlers.get(key);
        }

        OfflineDatabaseHandler dbh = new OfflineDatabaseHandler(context, key);
        databaseHandlers.put(key, dbh);
        return dbh;
    }

    public boolean switchHandlerFromPartialToRegular(String mapId) {
        if (TextUtils.isEmpty(mapId)) {
            return false;
        }
        String key = mapId.toLowerCase();
        if (!databaseHandlers.containsKey(key)) {
            return false;
        }

        OfflineDatabaseHandler dbh = new OfflineDatabaseHandler(context, key);
        databaseHandlers.remove(key);
        databaseHandlers.put(key, dbh);
        return true;
    }
}

package org.redcross.openmapkit;


import android.app.Application;
import android.util.Log;

import org.redcross.openmapkit.server.MBTilesServer;

import java.io.IOException;

public class OMKApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initializeMBTilesServer();
    }

    private void initializeMBTilesServer() {
        try {
            MBTilesServer.singleton().start();
        } catch(IOException ioe) {
            Log.w("Httpd", "MBTiles HTTP server could not start.");
        }
        Log.w("MBTilesServer", "MBTiles HTTP server initialized.");
    }
}

package org.redcross.openmapkit.server;

import android.util.Log;

import org.redcross.openmapkit.ExternalStorage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;

public class MBTilesServer extends NanoHTTPD {
    private static final String TAG = "MBTilesServer";
    private static final int PORT = 3210;
    private static final Pattern TILE_PATTERN = Pattern.compile("/(.*?)/([0-9]+)/([0-9]+)/([0-9]+)\\.");

    private static MBTilesServer singleton;
    private Map<String, MBTiles> layers = new HashMap<>();


    public static MBTilesServer singleton() {
        if (singleton == null) {
            singleton = new MBTilesServer();
        }
        return singleton;
    }

    private MBTilesServer() {
        super(PORT);
        ExternalStorage.checkOrCreateAppDirs();
        initializeMBTilesFiles();
    }

    private void initializeMBTilesFiles() {
        File[] files = ExternalStorage.fetchMBTilesFiles();
        if (files == null) return;
        for (File f : files) {
            addTiles(f.getAbsolutePath());
        }
    }

    private double[] addTiles(String filePath) {
        double[] bounds = null;
        try {
            MBTiles mbTiles = new MBTiles(filePath);
            bounds = mbTiles.getBounds();
            layers.put(fileNameFromPath(filePath), mbTiles);
        } catch(Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return bounds;
    }

    @Override
    public Response serve(IHTTPSession session) {
        NanoHTTPD.Response response;
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.d(TAG, method + " '" + uri + "' ");

        Matcher matcher = TILE_PATTERN.matcher(uri);
        if(!matcher.find()) {
            response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found");
        } else {
            String layerName = matcher.group(1);
            int z = Integer.parseInt(matcher.group(2));
            int x = Integer.parseInt(matcher.group(3));
            int y = Integer.parseInt(matcher.group(4));

            MBTiles mbTiles = layers.get(layerName);

            if (mbTiles != null) {
                try {
                    byte[] tile = mbTiles.getTile(z, x, y);
                    if (tile != null) {
                        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(tile));
                    } else {
                        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Tile not found");
                    }
                } catch(Exception ex) {
                    Log.e(TAG, ex.toString());
                    response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, ex.toString());
                }
            } else {
                response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Layer not found");
            }
        }

        return response;
    }

    private static String fileNameFromPath(String path) {
        int slashIdx = path.lastIndexOf("/");
        return path.substring(slashIdx+1);
    }
}
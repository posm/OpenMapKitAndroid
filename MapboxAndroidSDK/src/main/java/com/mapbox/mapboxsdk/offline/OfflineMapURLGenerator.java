package com.mapbox.mapboxsdk.offline;

import android.content.Context;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.constants.MathConstants;
import com.mapbox.mapboxsdk.util.MapboxUtils;

import java.util.ArrayList;

public class OfflineMapURLGenerator {

    private static class Bounds {
        public int minX;
        public int maxX;
        public int minY;
        public int maxY;
        public Bounds(int minX, int maxX, int minY, int maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }

    private ArrayList<Bounds> bounds;
    private int minimumZoom;
    private int maximumZoom;
    private int urlCount;

    public OfflineMapURLGenerator(double minLat, double maxLat, double minLon, double maxLon, int minimumZ, int maximumZ) {
        this.bounds = new ArrayList<Bounds>();
        this.minimumZoom = minimumZ;
        this.maximumZoom = maximumZ;
        this.urlCount = 0;
        for (int zoom = minimumZ; zoom <= maximumZ; zoom++) {
            int tilesPerSide = Double.valueOf(Math.pow(2.0, zoom)).intValue();
            int minX = Double.valueOf(Math.floor(((minLon + 180.0) / 360.0) * tilesPerSide)).intValue();
            int maxX = Double.valueOf(Math.floor(((maxLon + 180.0) / 360.0) * tilesPerSide)).intValue();
            int minY = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(Math.toRadians(maxLat)) + 1.0 / Math.cos(Math.toRadians(maxLat))) / MathConstants.PI)) / 2.0 * tilesPerSide)).intValue();
            int maxY = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(Math.toRadians(minLat)) + 1.0 / Math.cos(Math.toRadians(minLat))) / MathConstants.PI)) / 2.0 * tilesPerSide)).intValue();
            this.bounds.add(new Bounds(minX, maxX, minY, maxY));
            this.urlCount += (maxX - minX + 1) * (maxY - minY + 1);
        }
    }

    public int getURLCount() {
        return urlCount;
    }

    public String getURLForIndex(Context context, String mapID, MapboxConstants.RasterImageQuality imageQuality, int index) {
        if (index >= getURLCount()) {
            return null;
        }

        int zoom, x, y;
        // Middle condition intentionally 'less than' instead of 'less than or equal'.
        // If the 'break' is never hit, the last increment will make zoom equal to this.maximumZoom and end the loop.
        for (zoom = this.minimumZoom; zoom < this.maximumZoom; zoom++) {
            int boundsIndex = zoom - this.minimumZoom;
            Bounds bounds = this.bounds.get(boundsIndex);
            int urlsInThisLevel = (bounds.maxX - bounds.minX + 1) * (bounds.maxY - bounds.minY + 1);
            if (index < urlsInThisLevel) {
                break;
            } else {
                index -= urlsInThisLevel;
            }
        }

        Bounds bounds = this.bounds.get(zoom - this.minimumZoom);
        int yCount = bounds.maxY - bounds.minY + 1;
        x = (index / yCount) + bounds.minX;
        y = (index % yCount) + bounds.minY;
        return MapboxUtils.getMapTileURL(context, mapID, zoom, x, y, imageQuality);
    }
}

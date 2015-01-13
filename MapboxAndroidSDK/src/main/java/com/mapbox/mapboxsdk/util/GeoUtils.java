package com.mapbox.mapboxsdk.util;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import java.util.List;

public class GeoUtils {

    /**
     * Build a BoundingBox for a List of LatLng
     * @param coordinates List of coordinates
     * @param padding Option padding.  Recommended 0.01.  Send in null to have no padding applied
     * @return BoundingBox containing the given List of LatLng
     */
    public static BoundingBox findBoundingBoxForGivenLocations(List<LatLng> coordinates, Double padding) {
        double west = 0.0;
        double east = 0.0;
        double north = 0.0;
        double south = 0.0;

        for (int lc = 0; lc < coordinates.size(); lc++) {
            LatLng loc = coordinates.get(lc);
            if (lc == 0) {
                north = loc.getLatitude();
                south = loc.getLatitude();
                west = loc.getLongitude();
                east = loc.getLongitude();
            } else {
                if (loc.getLatitude() > north) {
                    north = loc.getLatitude();
                } else if (loc.getLatitude() < south) {
                    south = loc.getLatitude();
                }
                if (loc.getLongitude() < west) {
                    west = loc.getLongitude();
                } else if (loc.getLongitude() > east) {
                    east = loc.getLongitude();
                }
            }
        }

        // OPTIONAL - Add some extra "padding" for better map display
        if (padding != null) {
            north = north + padding;
            south = south - padding;
            west = west - padding;
            east = east + padding;
        }

        return new BoundingBox(north, east, south, west);
    }

}

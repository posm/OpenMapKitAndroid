package com.mapbox.mapboxsdk.util;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class MathUtils {
    public static double getDistance(LatLng cluster, LatLng marker) {
        return Math.pow(marker.getLatitude() * 2 - cluster.getLatitude() * 2, 2) + Math.pow(marker.getLongitude() - cluster.getLongitude(), 2);
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double factorial(double n) {
        double fact = 1; // this  will be the result
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }
}

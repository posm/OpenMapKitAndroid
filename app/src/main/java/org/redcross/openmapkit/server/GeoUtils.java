package org.redcross.openmapkit.server;

public class GeoUtils {

    public static double[] tile2deg(int x, int y, int z) {
        double[] coords = new double[2];

        coords[0] = tile2lon(x, z);
        coords[1] = tile2lat(y, z);

        return coords;
    }

    public static double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
package org.redcross.openmapkit.server;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MBTiles {
    private SQLiteDatabase tilesDB;

    public MBTiles(String fileName) throws Exception {
        try {
            tilesDB = SQLiteDatabase.openDatabase(fileName, null, SQLiteDatabase.OPEN_READONLY);
        } catch(Exception ex) {
            Log.e("HTTPMBTiles", ex.toString());
            throw (ex);
        }
    }

    private double[] getBoundsFromMetadata() {
        double[] bounds = null;

        String query = "SELECT value FROM metadata WHERE name = 'bounds'";
        Cursor cursor = tilesDB.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            // Use the bounds provided in the metadata
            String[] boundsStr = cursor.getString(0).split(",", 4);
            bounds = new double[4];

            if (bounds.length == 4) {
                bounds[0] = Double.parseDouble(boundsStr[0]); // left
                bounds[1] = Double.parseDouble(boundsStr[1]); // bottom
                bounds[2] = Double.parseDouble(boundsStr[2]); // top
                bounds[3] = Double.parseDouble(boundsStr[3]); // right
            }
        }
        cursor.close();
        return bounds;
    }

    private double[] calculateBounds() {
        double[] bounds = null;

        String query =
                "SELECT MIN(tile_column), " +
                        "MAX(tile_column), " +
                        "MIN(tile_row), " +
                        "MAX(tile_row), " +
                        "zoom_level " +
                        "FROM tiles " +
                        "WHERE zoom_level = (SELECT MIN(zoom_level) FROM tiles)";

        Cursor cursor = tilesDB.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            int zoomLevel = cursor.getInt(4);
            bounds = new double[4];
            double[] southwest;
            double[] northeast;
            // The mbtiles as stored as TMS, flip y axis to do calculations
            int numRows = (1 << zoomLevel);
            southwest = GeoUtils.tile2deg(cursor.getInt(0), numRows - 1 - cursor.getInt(1), zoomLevel);
            northeast = GeoUtils.tile2deg(cursor.getInt(2), numRows - 1 - cursor.getInt(3), zoomLevel);

            bounds[0] = southwest[0];
            bounds[1] = southwest[1];
            bounds[2] = northeast[0];
            bounds[3] = northeast[1];
        }
        cursor.close();
        return bounds;
    }

    public double[] getBounds() {
        double[] bounds = null;

        bounds = getBoundsFromMetadata();
        if (bounds == null) {
            bounds = calculateBounds();
        }

        return bounds;
    }


    public byte[] getTile(int z, int x, int y) throws Exception {
        // Most maps want Google tile schema, but MBTiles stores as TMS.
        // We have to flip the Y axis and ask for Y in TMS tile schema.
        int tmsY = (1 << z) - 1 - y; // (1 << z) is the same as Math.pow(2, z), but faster.
        String query = String.format(
                "SELECT tile_data " +
                        "FROM tiles " +
                        "WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d", z, x, tmsY);

        Cursor imageCur = tilesDB.rawQuery(query, null);

        byte[] blob = null;
        if (imageCur.moveToFirst()) {
            blob = imageCur.getBlob(0);
        }
        imageCur.close();

        return blob;
    }
}
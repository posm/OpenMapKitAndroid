package com.mapbox.mapboxsdk.tileprovider;

import android.graphics.Rect;
import com.mapbox.mapboxsdk.constants.GeoConstants;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;

/**
 * A map tile is distributed using the observer pattern. The tile is delivered by a tile provider
 * (i.e. a descendant of {@link com.mapbox.mapboxsdk.tileprovider.modules.MapTileModuleLayerBase}
 * or
 * {@link MapTileLayerBase} to a consumer of tiles (e.g. descendant of
 * {@link com.mapbox.mapboxsdk.overlay.TilesOverlay}). Tiles are typically images (e.g. png or
 * jpeg).
 */
public class MapTile implements GeoConstants, MapboxConstants, TileLayerConstants {

    public static final int MAPTILE_SUCCESS_ID = 0;
    public static final int MAPTILE_FAIL_ID = MAPTILE_SUCCESS_ID + 1;

    // This class must be immutable because it's used as the key in the cache hash map
    // (ie all the fields are final).
    private final int x;
    private final int y;
    private final int z;
    private final String path;
    private final String cacheKey;
    private final int code;
    private Rect mTileRect;

    // For lat/lng bounds calculation
    private double tileSize = DEFAULT_TILE_SIZE;
    private double originShift = 2 * Math.PI * RADIUS_EARTH_METERS / 2.0;
    private double initialResolution = 2 * Math.PI * RADIUS_EARTH_METERS / tileSize;

    public MapTile(final int az, final int ax, final int ay) {
        this("", az, ax, ay);
    }

    public MapTile(final String aCacheKey, final int az, final int ax, final int ay) {
        this.z = az;
        this.x = ax;
        this.y = ay;
        this.path = (new StringBuilder()).append(z).append('/').append(x).append('/').append(y).toString();
        this.cacheKey = aCacheKey + "/" + path;
        this.code = ((17 * (37 + z)) * (37 * x)) * (37 + y);
    }

    public int getZ() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getPath() {
        return path;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MapTile)) {
            return false;
        }
        final MapTile rhs = (MapTile) obj;
        return z == rhs.z && x == rhs.x && y == rhs.y;
    }

    @Override
    public int hashCode() {
        return this.code;
    }

    public void setTileRect(final Rect rect) {
        mTileRect = rect;
    }

    public final Rect getTileRect() {
        return mTileRect;
    }

    public BoundingBox getTileLatLonBounds() {
        // Returns bounds of the given tile in EPSG:900913 coordinates
        double[] bounds = TileBounds(this.getX(), this.getY(), this.getZ());
        double[] minLatLon = MetersToLatLon(bounds[0], bounds[3]);
        double[] maxLatLon = MetersToLatLon(bounds[2], bounds[1]);

        return new BoundingBox(maxLatLon[0], maxLatLon[1], minLatLon[0], minLatLon[1]);
    }

    private double[] TileBounds(int tx, int ty, int zoom) {
        // Returns bounds of the given tile in EPSG:900913 coordinates
        double[] wn = PixelsToMeters(tx * tileSize, ty * tileSize, zoom);
        double[] es = PixelsToMeters((tx + 1) * tileSize, (ty + 1) * tileSize, zoom);
        return new double[]{wn[0], wn[1], es[0], es[1]};
    }

    private double[] PixelsToMeters(double px, double py, double zoom) {
        // Converts pixel coordinates in given zoom level of pyramid to EPSG:900913
        double res = Resolution(zoom);
        double mx = px * res - originShift;
        double my = py * res - originShift;

        return new double[]{mx, my};
    }

    private double[] MetersToLatLon(double mx, double my) {
        // Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum
        double lon = (mx / originShift) * 180.0;
        double lat = (my / originShift) * 180.0;
        lat = -180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);

        return new double[]{lat, lon};
    }

    private double Resolution(double zoom) {
        // Resolution (meters/pixel) for given zoom level (measured at Equator)
        return initialResolution / Math.pow(2, zoom);
    }
}

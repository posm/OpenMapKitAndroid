/**
 * A Projection serves to translate between the coordinate system of x/y on-screen pixel
 * coordinates and that of latitude/longitude points on the surface of the earth. You obtain a
 * Projection from MapView.getProjection(). You should not hold on to this object for more than
 * one draw, since the projection of the map could change. <br />
 * <br />
 * <I>Screen coordinates</I> are in the coordinate system of the screen's Canvas. The origin is
 * in the center of the plane. <I>Screen coordinates</I> are appropriate for using to draw to
 * the screen.<br />
 * <br />
 * <I>Map coordinates</I> are in the coordinate system of the standard Mercator projection. The
 * origin is in the upper-left corner of the plane. <I>Map coordinates</I> are appropriate for
 * use in the Projection class.<br />
 * <br />
 * <I>Intermediate coordinates</I> are used to cache the computationally heavy part of the
 * projection. They aren't suitable for use until translated into <I>screen coordinates</I> or
 * <I>map coordinates</I>.
 *
 * @author Nicolas Gramlich
 * @author Manuel Stahl
 */

package com.mapbox.mapboxsdk.views.util;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.constants.GeoConstants;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.views.MapView;

public class Projection implements GeoConstants {
    private MapView mapView = null;
    private int viewWidth2;
    private int viewHeight2;
    private int worldSize2;
    private final int offsetX;
    private final int offsetY;
    private final int centerX;
    private final int centerY;
    private BoundingBox mBoundingBoxProjection;
    private final float mZoomLevelProjection;
    private final Rect mScreenRectProjection;
    private final RectF mTransformedScreenRectProjection;
    private final Rect mIntrinsicScreenRectProjection;
    private final float mMapOrientation;
    private final Matrix mRotateMatrix = new Matrix();
    protected static int mTileSize = 256;

    public Projection(final MapView mv) {
        super();
        this.mapView = mv;

        viewWidth2 = mapView.getMeasuredWidth() >> 1;
        viewHeight2 = mapView.getMeasuredHeight() >> 1;
        mZoomLevelProjection = mapView.getZoomLevel(false);
        worldSize2 = mapSize(mZoomLevelProjection) >> 1;

        offsetX = -worldSize2;
        offsetY = -worldSize2;

        centerX = mv.getScrollX();
        centerY = mv.getScrollY();

        //TODO: optimize because right now each line re-compute the previous value
        mIntrinsicScreenRectProjection = mapView.getIntrinsicScreenRect(null);
        if (mapView.getMapOrientation() % 180 != 0) {
            // Since the canvas is shifted by getWidth/2, we can just return our
            // natural scrollX/Y
            // value since that is the same as the shifted center.
            PointF scrollPoint = mapView.getScrollPoint();
            mScreenRectProjection = GeometryMath.getBoundingBoxForRotatedRectangle(mIntrinsicScreenRectProjection,
                    scrollPoint.x, scrollPoint.y, this.getMapOrientation(), null);
        } else {
            mScreenRectProjection = mIntrinsicScreenRectProjection;
        }
        mTransformedScreenRectProjection = new RectF(mScreenRectProjection);
        mapView.getInversedTransformMatrix().mapRect(mTransformedScreenRectProjection);
        mMapOrientation = mapView.getMapOrientation();
        mRotateMatrix.setRotate(-mMapOrientation, viewWidth2, viewHeight2);
    }

    public float getZoomLevel() {
        return mZoomLevelProjection;
    }

    public int getHalfWorldSize() {
        return worldSize2;
    }

    public BoundingBox getBoundingBox() {
        if (mBoundingBoxProjection == null) {
            mBoundingBoxProjection = mapView.getBoundingBoxInternal();
        }
        return mBoundingBoxProjection;
    }

    public Rect getScreenRect() {
        return mScreenRectProjection;
    }

    public RectF getTransformScreenRect() {
        return mTransformedScreenRectProjection;
    }

    public Rect getIntrinsicScreenRect() {
        return mIntrinsicScreenRectProjection;
    }

    public float getMapOrientation() {
        return mMapOrientation;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    /**
     * Converts <I>screen coordinates</I> to the underlying LatLng.
     *
     * @return LatLng under x/y.
     */
    public ILatLng fromPixels(final float x, final float y) {
        final Rect screenRect = getIntrinsicScreenRect();
        return pixelXYToLatLong(screenRect.left + (int) x + worldSize2,
                screenRect.top + (int) y + worldSize2, mZoomLevelProjection);
    }

    /**
     * Converts <I>screen coordinates</I> to the underlying LatLng.
     *
     * @return LatLng under x/y.
     */
    public ILatLng fromPixels(final int x, final int y) {
        return fromPixels((float) x, (float) y);
    }

    /**
     * Converts from map pixels to a Point value. Optionally reuses an existing Point.
     */
    public Point fromMapPixels(final int x, final int y, final Point reuse) {
        final Point out = GeometryMath.reusable(reuse);
        out.set(x - viewWidth2, y - viewHeight2);
        out.offset(centerX, centerY);
        return out;
    }

    /**
     * Converts a LatLng to its <I>screen coordinates</I>.
     *
     * @param in the LatLng you want the <I>screen coordinates</I> of
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>screen coordinates</I> of the LatLng passed.
     */
    public PointF toPixels(final ILatLng in, final PointF reuse) {
        PointF result = toMapPixels(in, reuse);
        result.offset(-mIntrinsicScreenRectProjection.exactCenterX(),
                -mIntrinsicScreenRectProjection.exactCenterY());
        if (mMapOrientation % 360 != 0) {
            GeometryMath.rotatePoint(0, 0, result, mMapOrientation, result);
        }
        result.offset(viewWidth2, viewHeight2);
        return result;
    }

    /**
     * Converts a map position in pixel to its <I>screen coordinates</I>.
     *
     * @param mapPos the map point you want the <I>screen coordinates</I> of
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>screen coordinates</I> of the point passed.
     */
    public PointF toPixels(final PointF mapPos, final PointF reuse) {
        final PointF out = GeometryMath.reusable(reuse);
        out.set(mapPos);
        out.offset(viewWidth2 - mIntrinsicScreenRectProjection.exactCenterX(),
                viewHeight2 - mIntrinsicScreenRectProjection.exactCenterY());
        return out;
    }

    /**
     * Converts a LatLng to its <I>Map coordinates</I> in pixels for the current zoom.
     *
     * @param in the LatLng you want the <I>screen coordinates</I> of
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>Map coordinates</I> of the LatLng passed.
     */
    public PointF toMapPixels(final ILatLng in, final PointF reuse) {
        return toMapPixels(in.getLatitude(), in.getLongitude(), reuse);
    }

    public static PointF toMapPixels(final double latitude, final double longitude, final float zoom, final double centerX, final double centerY, final PointF reuse) {
        final PointF out = GeometryMath.reusable(reuse);
        final int mapSize = mapSize(zoom);
        latLongToPixelXY(latitude, longitude, zoom, out);
        final float worldSize2 = mapSize >> 1;
        out.offset(-worldSize2, -worldSize2);
//        if (Math.abs(out.x - centerX) > Math.abs(out.x - mapSize - centerX)) {
//            out.x -= mapSize;
//        }
//        if (Math.abs(out.x - centerX) > Math.abs(out.x + mapSize - centerX)) {
//            out.x += mapSize;
//        }
//        if (Math.abs(out.y - centerY) > Math.abs(out.y - mapSize - centerY)) {
//            out.y -= mapSize;
//        }
//        if (Math.abs(out.y - centerY) > Math.abs(out.y + mapSize - centerY)) {
//            out.y += mapSize;
//        }
        return out;
    }

    public PointF toMapPixels(final double latitude, final double longitude, final PointF reuse) {
        return toMapPixels(latitude, longitude, getZoomLevel(), centerX, centerY, reuse);
    }

    public static RectF toMapPixels(final BoundingBox box, final float zoom, final RectF reuse) {
        final RectF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new RectF();
        }
        final int mapSize_2 = mapSize(zoom) >> 1;
        PointF nw = latLongToPixelXY(box.getLatNorth(), box.getLonWest(), zoom, null);
        PointF se = latLongToPixelXY(box.getLatSouth(), box.getLonEast(), zoom, null);
        out.set(nw.x, nw.y, se.x, se.y);
        out.offset(-mapSize_2, -mapSize_2);
        return out;
    }

    /**
     * Performs only the first computationally heavy part of the projection. Call
     * toMapPixelsTranslated to get the final position.
     *
     * @param latitude the latitude of the point
     * @param longitude the longitude of the point
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return intermediate value to be stored and passed to toMapPixelsTranslated.
     */
    public static PointF toMapPixelsProjected(final double latitude, final double longitude,
            final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }
        latLongToPixelXY(latitude, longitude, TileLayerConstants.MAXIMUM_ZOOMLEVEL, out);
        return out;
    }

    /**
     * Performs the second computationally light part of the projection. Returns results in
     * <I>screen coordinates</I>.
     *
     * @param in the Point calculated by the toMapPixelsProjected
     * @param reuse just pass null if you do not have a Point to be 'recycled'.
     * @return the Point containing the <I>Screen coordinates</I> of the initial LatLng passed
     * to the toMapPixelsProjected.
     */
    public PointF toMapPixelsTranslated(final PointF in, final PointF reuse) {
        final PointF out;
        if (reuse != null) {
            out = reuse;
        } else {
            out = new PointF();
        }

        final float zoomDifference = TileLayerConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();
        out.set((int) (GeometryMath.rightShift(in.x, zoomDifference) + offsetX),
                (int) (GeometryMath.rightShift(in.y, zoomDifference) + offsetY));
        return out;
    }

    public double[] toMapPixelsTranslated(final double[] in, final double[] out) {
        final float zoomDifference = TileLayerConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();
        out[0] = GeometryMath.rightShift(in[0], zoomDifference) + offsetX;
        out[1] = GeometryMath.rightShift(in[1], zoomDifference) + offsetY;
        return out;
    }

    /**
     * Translates a rectangle from <I>screen coordinates</I> to <I>intermediate coordinates</I>.
     *
     * @param in the rectangle in <I>screen coordinates</I>
     * @return a rectangle in </I>intermediate coordindates</I>.
     */
    public Rect fromPixelsToProjected(final Rect in) {
        final Rect result = new Rect();

        final float zoomDifference = TileLayerConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();

        final int x0 = (int) GeometryMath.leftShift(in.left - offsetX, zoomDifference);
        final int x1 = (int) GeometryMath.leftShift(in.right - offsetX, zoomDifference);
        final int y0 = (int) GeometryMath.leftShift(in.bottom - offsetY, zoomDifference);
        final int y1 = (int) GeometryMath.leftShift(in.top - offsetY, zoomDifference);

        result.set(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1));
        return result;
    }

    public static void setTileSize(final int tileSize) {
        mTileSize = tileSize;
    }

    public static int getTileSize() {
        return mTileSize;
    }

    /**
     * Clips a number to the specified minimum and maximum values.
     *
     * @param n The number to clip
     * @param minValue Minimum allowable value
     * @param maxValue Maximum allowable value
     * @return The clipped value.
     */
    private static double clip(final double n, final double minValue, final double maxValue) {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    /**
     * Determines the map width and height (in pixels) at a specified level of detail.
     *
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The map width and height in pixels
     */
    public static int mapSize(final float levelOfDetail) {
        return (int) (GeometryMath.leftShift(mTileSize, levelOfDetail));
    }

    /**
     * Determines the ground resolution (in meters per pixel) at a specified latitude and level of
     * detail.
     *
     * @param latitude Latitude (in degrees) at which to measure the ground resolution
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return The ground resolution, in meters per pixel
     */
    public static double groundResolution(final double latitude, final float levelOfDetail) {
        double result = wrap(latitude, -90, 90, 180);
        result = clip(result, MIN_LATITUDE, MAX_LATITUDE);
        return Math.cos(result * Math.PI / 180) * 2 * Math.PI * RADIUS_EARTH_METERS / mapSize(
                levelOfDetail);
    }

    /**
     * Determines the ground resolution (in meters per pixel) at a specified latitude and level of
     * detail.
     *
     * @param latitude Latitude (in degrees) at which to measure the ground resolution
     * @return The ground resolution, in meters per pixel
     */
    public double groundResolution(final double latitude) {
        return groundResolution(latitude, mZoomLevelProjection);
    }

    /**
     * Determines the map scale at a specified latitude, level of detail, and screen resolution.
     *
     * @param latitude Latitude (in degrees) at which to measure the map scale
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @param screenDpi Resolution of the screen, in dots per inch
     * @return The map scale, expressed as the denominator N of the ratio 1 : N
     */
    public static double mapScale(final double latitude, final int levelOfDetail,
            final int screenDpi) {
        return groundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
    }

    /**
     * Converts a point from latitude/longitude WGS-84 coordinates (in degrees) into pixel XY
     * coordinates at a specified level of detail.
     *
     * @param latitude Latitude of the point, in degrees
     * @param longitude Longitude of the point, in degrees
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @param reuse An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the X and Y coordinates in pixels
     */
    public static PointF latLongToPixelXY(double latitude, double longitude,
            final float levelOfDetail, final PointF reuse) {
        latitude = wrap(latitude, -90, 90, 180);
        longitude = wrap(longitude, -180, 180, 360);
        final PointF out = (reuse == null ? new PointF() : reuse);

        latitude = clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        longitude = clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);

        final double x = (longitude + 180) / 360;
        final double sinLatitude = Math.sin(latitude * Math.PI / 180);
        final double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

        final float mapSize = mapSize(levelOfDetail);
        out.x = (float) clip(x * mapSize, 0, mapSize - 1);
        out.y = (float) clip(y * mapSize, 0, mapSize - 1);
        return out;
    }

    public static double[] latLongToPixelXY(double latitude, double longitude) {
        latitude = wrap(latitude, -90, 90, 180);
        longitude = wrap(longitude, -180, 180, 360);

        latitude = clip(latitude, MIN_LATITUDE, MAX_LATITUDE);
        longitude = clip(longitude, MIN_LONGITUDE, MAX_LONGITUDE);

        final double x = (longitude + 180) / 360;
        final double sinLatitude = Math.sin(latitude * Math.PI / 180);
        final double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

        final float mapSize = mapSize(TileLayerConstants.MAXIMUM_ZOOMLEVEL);
        double outX = clip(x * mapSize, 0, mapSize - 1);
        double outY = clip(y * mapSize, 0, mapSize - 1);
        double[] out = {outX, outY};
        return out;
    }

    /**
     * Converts a pixel from pixel XY coordinates at a specified level of detail into
     * latitude/longitude WGS-84 coordinates (in degrees).
     *
     * @param pixelX X coordinate of the point, in pixels
     * @param pixelY Y coordinate of the point, in pixels
     * @param levelOfDetail Level of detail, from 1 (lowest detail) to 23 (highest detail)
     * @return Output parameter receiving the latitude and longitude in degrees.
     */
    public static LatLng pixelXYToLatLong(double pixelX, double pixelY, final float levelOfDetail) {
        final double mapSize = mapSize(levelOfDetail);
        final double maxSize = mapSize - 1.0;
        double x = wrap(pixelX, 0, maxSize, mapSize);
        double y = wrap(pixelY, 0, maxSize, mapSize);

        x = (clip(x, 0, maxSize) / mapSize) - 0.5;
        y = 0.5 - (clip(y, 0, maxSize) / mapSize);

        final double latitude = 90.0 - 360.0 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
        final double longitude = 360.0 * x;

        return new LatLng(latitude, longitude);
    }

    /**
     * Converts a pixel from pixel XY coordinates at a specified level of detail into
     * latitude/longitude WGS-84 coordinates (in degrees).
     *
     * @param pixelX X coordinate of the point, in pixels
     * @param pixelY Y coordinate of the point, in pixels
     * @return Output parameter receiving the latitude and longitude in degrees.
     */
    public LatLng pixelXYToLatLong(double pixelX, double pixelY) {
        return pixelXYToLatLong(pixelX, pixelY, mZoomLevelProjection);
    }

    /**
     * Converts pixel XY coordinates into tile XY coordinates of the tile containing the specified
     * pixel.
     *
     * @param pixelX Pixel X coordinate
     * @param pixelY Pixel Y coordinate
     * @param reuse An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the tile X and Y coordinates
     */
    public static Point pixelXYToTileXY(final int pixelX, final int pixelY, final Point reuse) {
        final Point out = (reuse == null ? new Point() : reuse);

        out.x = pixelX / mTileSize;
        out.y = pixelY / mTileSize;
        return out;
    }

    /**
     * Converts tile XY coordinates into pixel XY coordinates of the upper-left pixel of the
     * specified tile.
     *
     * @param tileX Tile X coordinate
     * @param tileY Tile X coordinate
     * @param reuse An optional Point to be recycled, or null to create a new one automatically
     * @return Output parameter receiving the pixel X and Y coordinates
     */
    public static Point tileXYToPixelXY(final int tileX, final int tileY, final Point reuse) {
        final Point out = (reuse == null ? new Point() : reuse);

        out.x = tileX * mTileSize;
        out.y = tileY * mTileSize;
        return out;
    }

    /**
     * Returns a value that lies within <code>minValue</code> and <code>maxValue</code> by
     * subtracting/adding <code>interval</code>.
     *
     * @param n the input number
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param interval the interval length
     * @return a value that lies within <code>minValue</code> and <code>maxValue</code> by
     * subtracting/adding <code>interval</code>
     */
    private static double wrap(double n, final double minValue, final double maxValue,
            final double interval) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException(
                    "minValue must be smaller than maxValue: " + minValue + ">" + maxValue);
        }
        if (interval > maxValue - minValue + 1) {
            throw new IllegalArgumentException(
                    "interval must be equal or smaller than maxValue-minValue: "
                            + "min: "
                            + minValue
                            + " max:"
                            + maxValue
                            + " int:"
                            + interval
            );
        }
        while (n < minValue) {
            n += interval;
        }
        while (n > maxValue) {
            n -= interval;
        }
        return n;
    }

    public void rotatePoints(final float[] pRotatePoints) {
        mRotateMatrix.mapPoints(pRotatePoints);
    }

    public void rotateRect(final RectF rect) {
        mRotateMatrix.mapRect(rect);
    }

    public final Matrix getRotationMatrix() {
        return mRotateMatrix;
    }

    private static final String TAG = "Projection";
}

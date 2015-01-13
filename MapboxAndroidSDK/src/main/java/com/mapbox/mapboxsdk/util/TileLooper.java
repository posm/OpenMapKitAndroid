package com.mapbox.mapboxsdk.util;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.views.util.Projection;
import java.util.ArrayList;
import java.util.List;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * A class that will loop around all the map tiles in the given viewport.
 */
public abstract class TileLooper {

    protected final Point mUpperLeft = new Point();
    protected final Point mLowerRight = new Point();
    protected final Point center = new Point();
    protected List<CacheableBitmapDrawable> mBeingUsedDrawables =
            new ArrayList<CacheableBitmapDrawable>();

    public final int loop(final Canvas pCanvas, final String pCacheKey, final float pZoomLevel,
            final int pTileSizePx, final Rect pViewPort, final Rect pClipRect) {
        // Calculate the amount of tiles needed for each side around the center one.
        Projection.pixelXYToTileXY(pViewPort.left, pViewPort.top, mUpperLeft);
        mUpperLeft.offset(-1, -1);

        Projection.pixelXYToTileXY(pViewPort.right, pViewPort.bottom, mLowerRight);
        mLowerRight.offset(1, 1);

        center.set((mUpperLeft.x + mLowerRight.x) / 2, (mUpperLeft.y + mLowerRight.y) / 2);

        final int roundedZoom = (int) Math.floor(pZoomLevel);
        final int mapTileUpperBound = 1 << roundedZoom;
        initializeLoop(pZoomLevel, pTileSizePx);

        int tileX, tileY;

        for (int y = mUpperLeft.y; y <= mLowerRight.y; y++) {
            for (int x = mUpperLeft.x; x <= mLowerRight.x; x++) {
                tileY = GeometryMath.mod(y, mapTileUpperBound);
                tileX = GeometryMath.mod(x, mapTileUpperBound);
                final MapTile tile = new MapTile(pCacheKey, roundedZoom, tileX, tileY);
                handleTile(pCanvas, pCacheKey, pTileSizePx, tile, x, y, pClipRect);
            }
        }
        finalizeLoop();

        /* return number of tiles looped */
        return  (mLowerRight.y - mUpperLeft.y) * (mLowerRight.x - mUpperLeft.x);
    }

    public abstract void initializeLoop(float pZoomLevel, int pTileSizePx);

    public abstract void handleTile(Canvas pCanvas, final String pCacheKey, int pTileSizePx,
            MapTile pTile, int pX, int pY, final Rect pClipRect);

    public void finalizeLoop() {
        //we delay just to make sure drawable bitmaps are not reused while being drawn.
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                for (CacheableBitmapDrawable drawable : mBeingUsedDrawables) {
                    drawable.setBeingUsed(false);
                }
                mBeingUsedDrawables.clear();
            }
        }, 1);
    }
}

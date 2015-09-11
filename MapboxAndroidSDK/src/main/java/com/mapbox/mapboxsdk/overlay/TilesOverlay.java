package com.mapbox.mapboxsdk.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import com.mapbox.mapboxsdk.util.GeometryMath;
import com.mapbox.mapboxsdk.util.TileLooper;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;
import com.mapbox.mapboxsdk.views.util.Projection;
import java.util.HashMap;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * These objects are the principle consumer of map tiles.
 * <p/>
 * see {@link MapTile} for an overview of how tiles are acquired by this overlay.
 */

public class TilesOverlay extends SafeDrawOverlay {

    private static final String TAG = "TilesOverlay";

    public static final int MENU_OFFLINE = getSafeMenuId();
    private int mNuberOfTiles;

    /**
     * Current tile source
     */
    protected final MapTileLayerBase mTileProvider;

    /* to avoid allocations during draw */
    protected static SafePaint mDebugPaint = null;
    protected static SafePaint mLoadingTilePaint = null;
    protected static Bitmap mLoadingTileBitmap = null;
    protected Paint mLoadingPaint = null;
    private final Rect mTileRect = new Rect();
    private final Rect mViewPort = new Rect();
    private final Rect mClipRect = new Rect();
    float mCurrentZoomFactor = 1;
    private float mRescaleZoomDiffMax = 4;
    private boolean isAnimating = false;
    private boolean mOptionsMenuEnabled = true;

    private int mWorldSize_2;

    private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
    private int mLoadingLineColor = Color.rgb(200, 192, 192);
    private boolean mDrawLoadingTile = true;

    public TilesOverlay(final MapTileLayerBase aTileProvider) {
        super();
        if (aTileProvider == null) {
            throw new IllegalArgumentException("You must pass a valid tile provider to the tiles overlay.");
        }
        this.mTileProvider = aTileProvider;
        if (UtilConstants.DEBUGMODE) {
            getDebugPaint();
        }
        mLoadingPaint = new Paint();
        mLoadingPaint.setAntiAlias(true);
        mLoadingPaint.setFilterBitmap(true);
        mLoadingPaint.setColor(mLoadingLineColor);
        mLoadingPaint.setStrokeWidth(0);
        mNuberOfTiles = 0;
    }

    public static SafePaint getDebugPaint() {
        if (mDebugPaint == null) {
            mDebugPaint = new SafePaint();
            mDebugPaint.setAntiAlias(true);
            mDebugPaint.setFilterBitmap(true);
            mDebugPaint.setColor(Color.RED);
            mDebugPaint.setStyle(Paint.Style.STROKE);
        }
        return mDebugPaint;
    }

    @Override
    public void onDetach(final MapView pMapView) {
        this.mTileProvider.detach();
    }

    public float getMinimumZoomLevel() {
        return mTileProvider.getMinimumZoomLevel();
    }

    public float getMaximumZoomLevel() {
        return mTileProvider.getMaximumZoomLevel();
    }

    /**
     * Whether to use the network connection if it's available.
     *
     * @return true if this uses a data connection
     */
    public boolean useDataConnection() {
        return mTileProvider.useDataConnection();
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param aMode if true use the network connection if it's available. if false don't use the
     *              network connection even if it's available.
     */
    public void setUseDataConnection(final boolean aMode) {
        mTileProvider.setUseDataConnection(aMode);
    }

    @Override
    protected void drawSafe(final ISafeCanvas c, final MapView mapView, final boolean shadow) {

        if (shadow) {
            return;
        }
        //Commented for now. It needs heavy testing to see if we actually need it
        isAnimating = mapView.isAnimating();

        // Calculate the half-world size
        final Projection pj = mapView.getProjection();

        c.getClipBounds(mClipRect);
        float zoomDelta = (float) (Math.log(mapView.getScale()) / Math.log(2d));
        final float zoomLevel = pj.getZoomLevel();
        mWorldSize_2 = pj.getHalfWorldSize();
        GeometryMath.viewPortRectForTileDrawing(pj, mViewPort);

        int tileSize = Projection.getTileSize();
        // Draw the tiles!
        if (tileSize > 0) {
            if (mDrawLoadingTile) {
                drawLoadingTile(c.getSafeCanvas(), mapView, zoomLevel, mClipRect);
            }
            drawTiles(c.getSafeCanvas(), zoomLevel, tileSize, mViewPort, mClipRect);
        }

        if (UtilConstants.DEBUGMODE && mapView.getScrollableAreaLimit() != null) {
            SafePaint paint = new SafePaint();
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            Rect rect = new Rect();
            mapView.getScrollableAreaLimit().round(rect);
            if (mapView.getScrollableAreaLimit() != null) {
                c.drawRect(rect, paint);
            }
        }
    }

    /**
     * Draw a loading tile image to make in-progress tiles easier to deal with.
     *
     * @param c
     * @param mapView
     * @param zoomLevel
     * @param viewPort
     */
    public void drawLoadingTile(final Canvas c, final MapView mapView, final float zoomLevel, final Rect viewPort) {
        ISafeCanvas canvas = (ISafeCanvas) c;
        canvas.save();
        canvas.translate(-mapView.getScrollX(), -mapView.getScrollY());
        canvas.drawPaint(getLoadingTilePaint());
        canvas.restore();
    }

    /**
     * This is meant to be a "pure" tile drawing function that doesn't take into account
     * osmdroid-specific characteristics (like osmdroid's canvas's having 0,0 as the center rather
     * than the upper-left corner). Once the tile is ready to be drawn, it is passed to
     * onTileReadyToDraw where custom manipulations can be made before drawing the tile.
     */
    public void drawTiles(final Canvas c, final float zoomLevel, final int tileSizePx,
                          final Rect viewPort, final Rect pClipRect) {

        mNuberOfTiles = mTileLooper.loop(c, mTileProvider.getCacheKey(), zoomLevel, tileSizePx, viewPort, pClipRect);

        // draw a cross at center in debug mode
        if (UtilConstants.DEBUGMODE) {
            ISafeCanvas canvas = (ISafeCanvas) c;
            final Point centerPoint =
                    new Point(viewPort.centerX() - mWorldSize_2, viewPort.centerY() - mWorldSize_2);
            canvas.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9,
                    getDebugPaint());
            canvas.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y,
                    getDebugPaint());
        }
    }

    private final TileLooper mTileLooper = new TileLooper() {
        @Override
        public void initializeLoop(final float pZoomLevel, final int pTileSizePx) {

            final int roundedZoom = (int) Math.floor(pZoomLevel);
            if (roundedZoom != pZoomLevel) {
                final int mapTileUpperBound = 1 << roundedZoom;
                mCurrentZoomFactor =
                        (float) Projection.mapSize(pZoomLevel) / mapTileUpperBound / pTileSizePx;
            } else {
                mCurrentZoomFactor = 1.0f;
            }
        }

        @Override
        public void handleTile(final Canvas pCanvas, final String pCacheKey, final int pTileSizePx,
                               final MapTile pTile, final int pX, final int pY, final Rect pClipRect) {
            final double factor = pTileSizePx * mCurrentZoomFactor;
            double x = pX * factor - mWorldSize_2;
            double y = pY * factor - mWorldSize_2;
            mTileRect.set((int) x, (int) y, (int) (x + factor), (int) (y + factor));
            if (!Rect.intersects(mTileRect, pClipRect)) {
                return;
            }
            pTile.setTileRect(mTileRect);
            Drawable drawable = mTileProvider.getMapTile(pTile, !isAnimating);
            boolean isReusable = drawable instanceof CacheableBitmapDrawable;

            if (drawable != null) {
                if (isReusable) {
                    mBeingUsedDrawables.add((CacheableBitmapDrawable) drawable);
                }
                drawable.setBounds(mTileRect);
                drawable.draw(pCanvas);
            } else {
                mTileProvider.memoryCacheNeedsMoreMemory(mNuberOfTiles);
                //Log.w(TAG, "tile should have been drawn to canvas, but it was null.  tile = '" + pTile + "'");
            }

            if (UtilConstants.DEBUGMODE) {
                ISafeCanvas canvas = (ISafeCanvas) pCanvas;
                canvas.drawText(pTile.toString(), mTileRect.left + 1, mTileRect.top + getDebugPaint().getTextSize(), getDebugPaint());
                canvas.drawRect(mTileRect, getDebugPaint());
            }
        }
    };

    public int getLoadingBackgroundColor() {
        return mLoadingBackgroundColor;
    }

    /**
     * Set the color to use to draw the background while we're waiting for the tile to load.
     *
     * @param pLoadingBackgroundColor the color to use. If the value is {@link Color#TRANSPARENT}
     *                                then there will be no
     *                                loading tile.
     */
    public void setLoadingBackgroundColor(final int pLoadingBackgroundColor) {
        if (mLoadingBackgroundColor != pLoadingBackgroundColor) {
            mLoadingBackgroundColor = pLoadingBackgroundColor;
            clearLoadingTile();
        }
    }

    public int getLoadingLineColor() {
        return mLoadingLineColor;
    }

    public void setLoadingLineColor(final int pLoadingLineColor) {
        if (mLoadingLineColor != pLoadingLineColor) {
            mLoadingLineColor = pLoadingLineColor;
            mLoadingPaint.setColor(mLoadingLineColor);
            clearLoadingTile();
        }
    }

    /**
     * Set whether or not the default loading tile background should be drawn.
     * If it shouldn't be, then a transparent background will be displayed.
     * @param pDrawLoadingTile True if loading tiles should be displayed (default), False if not (aka: transparent background)
     */
    public void setDrawLoadingTile(final boolean pDrawLoadingTile) {
        this.mDrawLoadingTile = pDrawLoadingTile;
    }

    /**
     * Draw a 'loading' placeholder with a canvas.
     */
    private SafePaint getLoadingTilePaint() {
        if (mLoadingTilePaint == null && mLoadingBackgroundColor != Color.TRANSPARENT) {
            try {
                final int tileSize =
                        mTileProvider.getTileSource() != null ? mTileProvider.getTileSource()
                                .getTileSizePixels() : 256;
                mLoadingTileBitmap =
                        Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(mLoadingTileBitmap);
                canvas.drawColor(mLoadingBackgroundColor);
                final int lineSize = tileSize / 16;
                for (int a = 0; a < tileSize; a += lineSize) {
                    canvas.drawLine(0, a, tileSize, a, mLoadingPaint);
                    canvas.drawLine(a, 0, a, tileSize, mLoadingPaint);
                }
                mLoadingTilePaint = new SafePaint();
                mLoadingTilePaint.setShader(new BitmapShader(mLoadingTileBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
            } catch (final OutOfMemoryError e) {
                Log.e(TAG, "OutOfMemoryError getting loading tile: " + e.toString());
                System.gc();
            }
        }
        return mLoadingTilePaint;
    }

    private void clearLoadingTile() {
        mLoadingTilePaint = null;
        // Only recycle if we are running on a project less than 2.3.3 Gingerbread.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            if (mLoadingTileBitmap != null) {
                mLoadingTileBitmap.recycle();
                mLoadingTileBitmap = null;
            }
        }
    }

    /**
     * Recreate the cache using scaled versions of the tiles currently in it
     *
     * @param pNewZoomLevel the zoom level that we need now
     * @param pOldZoomLevel the previous zoom level that we should get the tiles to rescale
     * @param projection    the projection to compute view port
     */
    public void rescaleCache(final float pNewZoomLevel, final float pOldZoomLevel,
                             final Projection projection) {

        if (mTileProvider.hasNoSource() || Math.floor(pNewZoomLevel) == Math.floor(pOldZoomLevel) || projection == null || Math.abs(pOldZoomLevel - pNewZoomLevel) > mRescaleZoomDiffMax) {
            return;
        }

        final long startMs = System.currentTimeMillis();

        if (UtilConstants.DEBUGMODE) {
            Log.d(TAG, "rescale tile cache from " + pOldZoomLevel + " to " + pNewZoomLevel);
        }

        final int tileSize = Projection.getTileSize();
        final Rect viewPort =
                GeometryMath.viewPortRectForTileDrawing(pNewZoomLevel, projection, null);

        final ScaleTileLooper tileLooper =
                pNewZoomLevel > pOldZoomLevel ? new ZoomInTileLooper(pOldZoomLevel)
                        : new ZoomOutTileLooper(pOldZoomLevel);
        tileLooper.loop(null, mTileProvider.getCacheKey(), pNewZoomLevel, tileSize, viewPort, null);

        final long endMs = System.currentTimeMillis();
        if (UtilConstants.DEBUGMODE) {
            Log.d(TAG, "Finished rescale in " + (endMs - startMs) + "ms");
        }
    }

    private abstract class ScaleTileLooper extends TileLooper {

        /**
         * new (scaled) tiles to add to cache
         * NB first generate all and then put all in cache,
         * otherwise the ones we need will be pushed out
         */
        protected final HashMap<MapTile, Bitmap> mNewTiles;

        protected final float mOldZoomLevel;
        protected final int mOldZoomRound;
        protected final int mOldTileUpperBound;
        protected float mDiff;
        protected int mTileSize_2;
        protected Rect mSrcRect;
        protected Rect mDestRect;
        protected Paint mDebugPaint;

        public ScaleTileLooper(final float pOldZoomLevel) {
            mOldZoomLevel = pOldZoomLevel;
            mOldZoomRound = (int) Math.floor(mOldZoomLevel);
            mOldTileUpperBound = 1 << mOldZoomRound;
            mNewTiles = new HashMap<MapTile, Bitmap>();
            mSrcRect = new Rect();
            mDestRect = new Rect();
            mDebugPaint = new Paint();
        }

        @Override
        public void initializeLoop(final float pZoomLevel, final int pTileSizePx) {
            mDiff = (float) Math.abs(Math.floor(pZoomLevel) - Math.floor(mOldZoomLevel));
            mTileSize_2 = (int) GeometryMath.rightShift(pTileSizePx, mDiff);
        }

        @Override
        public void handleTile(final Canvas pCanvas, final String pCacheKey, final int pTileSizePx,
                               final MapTile pTile, final int pX, final int pY, final Rect pClipRect) {

            // Get tile from cache.
            // If it's found then no need to created scaled version.
            // If not found (null) them we've initiated a new request for it,
            // and now we'll create a scaled version until the request completes.
            final Drawable requestedTile = mTileProvider.getMapTile(pTile, !isAnimating);
            if (requestedTile == null) {
                try {
                    handleScaleTile(pCacheKey, pTileSizePx, pTile, pX, pY);
                } catch (final OutOfMemoryError e) {
                    Log.e(TAG, "OutOfMemoryError rescaling cache");
                }
            }
        }

        @Override
        public void finalizeLoop() {
            super.finalizeLoop();
            // now add the new ones, pushing out the old ones
            while (!mNewTiles.isEmpty()) {

                final MapTile tile = mNewTiles.keySet().iterator().next();
                final Bitmap bitmap = mNewTiles.remove(tile);
                mTileProvider.putExpiredTileIntoCache(tile, bitmap);
            }
        }

        protected abstract void handleScaleTile(final String pCacheKey, final int pTileSizePx,
                                                final MapTile pTile, final int pX, final int pY);
    }

    private class ZoomInTileLooper extends ScaleTileLooper {
        public ZoomInTileLooper(final float pOldZoomLevel) {
            super(pOldZoomLevel);
        }

        @Override
        public void handleScaleTile(final String pCacheKey, final int pTileSizePx,
                                    final MapTile pTile, final int pX, final int pY) {
            int oldTileX = GeometryMath.mod((int) GeometryMath.rightShift(pX, mDiff), mOldTileUpperBound);
            int oldTileY = GeometryMath.mod((int) GeometryMath.rightShift(pY, mDiff), mOldTileUpperBound);

            // get the correct fraction of the tile from cache and scale up
            final MapTile oldTile = new MapTile(pCacheKey,
                    mOldZoomRound, oldTileX, oldTileY);
            final Drawable oldDrawable = mTileProvider.getMapTileFromMemory(oldTile);

            if (oldDrawable instanceof BitmapDrawable) {
                final boolean isReusable = oldDrawable instanceof CacheableBitmapDrawable;
                if (isReusable) {
                    ((CacheableBitmapDrawable) oldDrawable).setBeingUsed(true);
                    mBeingUsedDrawables.add((CacheableBitmapDrawable) oldDrawable);
                }

                final Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
                if (oldBitmap != null) {
                    final int xx = (pX % (int) GeometryMath.leftShift(1, mDiff)) * mTileSize_2;
                    final int yy = (pY % (int) GeometryMath.leftShift(1, mDiff)) * mTileSize_2;
                    mSrcRect.set(xx, yy, xx + mTileSize_2, yy + mTileSize_2);
                    mDestRect.set(0, 0, pTileSizePx, pTileSizePx);

                    // Try to get a bitmap from the pool, otherwise allocate a new one
                    Bitmap bitmap = mTileProvider.getBitmapFromRemoved(pTileSizePx, pTileSizePx);

                    if (bitmap == null) {
                        bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx, Bitmap.Config.ARGB_8888);
                    }
                    final Canvas canvas = new Canvas(bitmap);
                    canvas.drawBitmap(oldBitmap, mSrcRect, mDestRect, null);
                    mNewTiles.put(pTile, bitmap);
                }
            }
        }
    }

    private class ZoomOutTileLooper extends ScaleTileLooper {
        private static final int MAX_ZOOM_OUT_DIFF = 8;

        public ZoomOutTileLooper(final float pOldZoomLevel) {
            super(pOldZoomLevel);
        }

        @Override
        protected void handleScaleTile(final String pCacheKey, final int pTileSizePx,
                                       final MapTile pTile, final int pX, final int pY) {

            if (mDiff >= MAX_ZOOM_OUT_DIFF) {
                return;
            }

            // get many tiles from cache and make one tile from them
            final int xx = (int) GeometryMath.leftShift(pX, mDiff);
            final int yy = (int) GeometryMath.leftShift(pY, mDiff);
            final int numTiles = (int) GeometryMath.leftShift(1, mDiff);

            int oldTileX, oldTileY;
            Bitmap bitmap = null;
            Canvas canvas = null;
            for (int x = 0; x < numTiles; x++) {
                for (int y = 0; y < numTiles; y++) {
                    oldTileY = GeometryMath.mod(yy + y, mOldTileUpperBound);
                    oldTileX = GeometryMath.mod(xx + x, mOldTileUpperBound);
                    final MapTile oldTile = new MapTile(pCacheKey,
                            mOldZoomRound, oldTileX, oldTileY);
                    Drawable oldDrawable = mTileProvider.getMapTileFromMemory(oldTile);

                    if (oldDrawable instanceof BitmapDrawable) {
                        final boolean isReusable = oldDrawable instanceof CacheableBitmapDrawable;
                        if (isReusable) {
                            ((CacheableBitmapDrawable) oldDrawable).setBeingUsed(true);
                            mBeingUsedDrawables.add((CacheableBitmapDrawable) oldDrawable);
                        }
                        final Bitmap oldBitmap = ((BitmapDrawable) oldDrawable).getBitmap();
                        if (oldBitmap != null) {
                            if (bitmap == null) {
                                // Try to get a bitmap from the pool, otherwise allocate a new one
                                bitmap = mTileProvider.getBitmapFromRemoved(pTileSizePx,
                                        pTileSizePx);
                                if (bitmap == null) {
                                    bitmap = Bitmap.createBitmap(pTileSizePx, pTileSizePx,
                                            Bitmap.Config.ARGB_8888);
                                }
                                canvas = new Canvas(bitmap);
                            }
                            mDestRect.set(x * mTileSize_2, y * mTileSize_2, (x + 1) * mTileSize_2,
                                    (y + 1) * mTileSize_2);
                            canvas.drawBitmap(oldBitmap, null, mDestRect, null);
                        }
                    }
                }
            }

            if (bitmap != null) {
                mNewTiles.put(pTile, bitmap);
            }
        }
    }
}

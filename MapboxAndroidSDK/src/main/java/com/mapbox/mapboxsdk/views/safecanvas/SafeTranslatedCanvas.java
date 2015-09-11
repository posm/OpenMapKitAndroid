package com.mapbox.mapboxsdk.views.safecanvas;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;

/**
 * An implementation of {@link ISafeCanvas} that wraps a {@link Canvas} and adjusts drawing calls
 * to
 * the wrapped Canvas so that they are relative to an origin that is always at the center of the
 * screen.<br />
 * <br />
 * See {@link ISafeCanvas} for details<br />
 *
 * @author Marc Kurtz
 */
public class SafeTranslatedCanvas extends Canvas implements ISafeCanvas {
    private static final Matrix sMatrix = new Matrix();
    private static final RectF sRectF = new RectF();
    private static float[] sFloatAry = new float[0];
    private Canvas mCanvas;
    private final Matrix mMatrix = new Matrix();
    public int xOffset;
    public int yOffset;

    public SafeTranslatedCanvas() {
        //
    }

    public SafeTranslatedCanvas(Canvas canvas) {
        this.setCanvas(canvas);
    }

    @Override
    public Canvas getSafeCanvas() {
        return this;
    }

    @Override
    public int getXOffset() {
        return xOffset;
    }

    @Override
    public int getYOffset() {
        return yOffset;
    }

    public void setCanvas(Canvas canvas) {
        mCanvas = canvas;
        canvas.getMatrix(mMatrix);
    }

    public void getUnsafeCanvas(UnsafeCanvasHandler handler) {
        this.save();
        this.setMatrix(this.getOriginalMatrix());
        handler.onUnsafeCanvas(mCanvas);
        this.restore();
    }

    public Canvas getWrappedCanvas() {
        return mCanvas;
    }

    public Matrix getOriginalMatrix() {
        return mMatrix;
    }

    @Override
    public boolean clipPath(SafeTranslatedPath path, Op op) {
        return getWrappedCanvas().clipPath(path, op);
    }

    @Override
    public boolean clipPath(SafeTranslatedPath path) {
        return getWrappedCanvas().clipPath(path);
    }

    @Override
    public boolean clipRect(final double left, final double top, final double right,
                            final double bottom, final Op op) {
        return getWrappedCanvas().clipRect((float) (left + xOffset), (float) (top + yOffset),
                (float) (right + xOffset), (float) (bottom + yOffset), op);
    }

    @Override
    public boolean clipRect(final double left, final double top, final double right,
                            final double bottom) {
        return getWrappedCanvas().clipRect((float) (left + xOffset), (float) (top + yOffset),
                (float) (right + xOffset), (float) (bottom + yOffset));
    }

    @Override
    public boolean clipRect(final int left, final int top, final int right, final int bottom) {
        return getWrappedCanvas().clipRect(left + xOffset, top + yOffset, right + xOffset,
                bottom + yOffset);
    }

    @Override
    public boolean clipRect(final Rect rect, final Op op) {
        rect.offset(xOffset, yOffset);
        return getWrappedCanvas().clipRect(rect, op);
    }

    @Override
    public boolean clipRect(final Rect rect) {
        rect.offset(xOffset, yOffset);
        return getWrappedCanvas().clipRect(rect);
    }

    @Override
    public boolean clipRegion(final Region region, final Op op) {
        region.translate(xOffset, yOffset);
        return getWrappedCanvas().clipRegion(region, op);
    }

    @Override
    public boolean clipRegion(final Region region) {
        region.translate(xOffset, yOffset);
        return getWrappedCanvas().clipRegion(region);
    }

    @Override
    public void concat(final Matrix matrix) {
        getWrappedCanvas().concat(matrix);
    }

    @Override
    public void drawARGB(final int a, final int r, final int g, final int b) {
        getWrappedCanvas().drawARGB(a, r, g, b);
    }

    @Override
    public void drawArc(final Rect oval, final float startAngle, final float sweepAngle,
                        final boolean useCenter, final SafePaint paint) {
        getWrappedCanvas().drawArc(this.toOffsetRectF(oval, sRectF), startAngle, sweepAngle,
                useCenter, paint);
    }

    @Override
    public void drawBitmap(final Bitmap bitmap, final double left, final double top,
                           final SafePaint paint) {
        getWrappedCanvas().drawBitmap(bitmap, (float) (left + xOffset), (float) (top + yOffset),
                paint);
    }

    @Override
    public void drawBitmap(final Bitmap bitmap, final Matrix matrix, final SafePaint paint) {
        sMatrix.set(matrix);
        sMatrix.postTranslate(xOffset, yOffset);
        getWrappedCanvas().drawBitmap(bitmap, sMatrix, paint);
    }

    @Override
    public void drawBitmap(final Bitmap bitmap, final Rect src, final Rect dst,
                           final SafePaint paint) {
        dst.offset(xOffset, yOffset);
        getWrappedCanvas().drawBitmap(bitmap, src, dst, paint);
        dst.offset(-xOffset, -yOffset);
    }

    /* This is used by Drawable.draw(Canvas), so also we adjust here */
    @Override
    public void drawBitmap(final Bitmap bitmap, final Rect src, final Rect dst, final Paint paint) {
        dst.offset(xOffset, yOffset);
        getWrappedCanvas().drawBitmap(bitmap, src, dst, paint);
        dst.offset(-xOffset, -yOffset);
    }

    @Override
    public void drawBitmap(final int[] colors, final int offset, final int stride, final double x,
                           final double y, final int width, final int height, final boolean hasAlpha,
                           final SafePaint paint) {
        getWrappedCanvas().drawBitmap(colors, offset, stride, (float) (x + xOffset),
                (float) (y + yOffset), width, height, hasAlpha, paint);
    }

    @Override
    public void drawBitmap(final int[] colors, final int offset, final int stride, final int x,
                           final int y, final int width, final int height, final boolean hasAlpha,
                           final SafePaint paint) {
        getWrappedCanvas().drawBitmap(colors, offset, stride, x + offset, y + offset, width, height,
                hasAlpha, paint);
    }

    @Override
    public void drawBitmapMesh(final Bitmap bitmap, final int meshWidth, final int meshHeight,
                               final double[] verts, final int vertOffset, final int[] colors, final int colorOffset,
                               final SafePaint paint) {
        getWrappedCanvas().drawBitmapMesh(bitmap, meshWidth, meshHeight,
                this.toOffsetFloatAry(verts, sFloatAry), vertOffset, colors, colorOffset, paint);
    }

    @Override
    public void drawCircle(final double cx, final double cy, final float radius,
                           final SafePaint paint) {
        getWrappedCanvas().drawCircle((float) (cx + xOffset), (float) (cy + yOffset), radius,
                paint);
    }

    @Override
    public void drawColor(final int color, final Mode mode) {

        getWrappedCanvas().drawColor(color, mode);
    }

    @Override
    public void drawColor(final int color) {

        getWrappedCanvas().drawColor(color);
    }

    @Override
    public void drawLine(double startX, double startY, double stopX, double stopY,
                         final SafePaint paint) {
        startX += xOffset;
        startY += yOffset;
        stopX += xOffset;
        stopY += yOffset;
        getWrappedCanvas().drawLine((float) startX, (float) startY, (float) stopX, (float) stopY,
                paint);
    }

    @Override
    public void drawLines(double[] pts, int offset, int count, final SafePaint paint) {
        getWrappedCanvas().drawLines(this.toOffsetFloatAry(pts, sFloatAry), offset, count, paint);
    }

    @Override
    public void drawLines(double[] pts, final SafePaint paint) {
        getWrappedCanvas().drawLines(this.toOffsetFloatAry(pts, sFloatAry), paint);
    }

    @Override
    public void drawOval(Rect oval, final SafePaint paint) {
        getWrappedCanvas().drawOval(this.toOffsetRectF(oval, sRectF), paint);
    }

    @Override
    public void drawPaint(SafePaint paint) {
        getWrappedCanvas().drawPaint(paint);
    }

    @Override
    public void drawPath(SafeTranslatedPath path, SafePaint paint) {
        getWrappedCanvas().drawPath(path, paint);
    }

    @Override
    public void drawPicture(final Picture picture, final Rect dst) {
        dst.offset(xOffset, yOffset);
        getWrappedCanvas().drawPicture(picture, dst);
        dst.offset(-xOffset, -yOffset);
    }

    @Override
    public void drawPicture(final Picture picture) {
        getWrappedCanvas().drawPicture(picture);
    }

    @Override
    public void drawPoint(double x, double y, final SafePaint paint) {
        x += xOffset;
        y += yOffset;
        getWrappedCanvas().drawPoint((float) x, (float) y, paint);
    }

    @Override
    public void drawPoints(double[] pts, int offset, int count, final SafePaint paint) {
        getWrappedCanvas().drawPoints(this.toOffsetFloatAry(pts, sFloatAry), offset, count, paint);
    }

    @Override
    public void drawPoints(double[] pts, final SafePaint paint) {
        getWrappedCanvas().drawPoints(this.toOffsetFloatAry(pts, sFloatAry), paint);
    }

    @Override
    public void drawPosText(char[] text, int index, int count, double[] pos,
                            final SafePaint paint) {
        getWrappedCanvas().drawPosText(text, index, count, this.toOffsetFloatAry(pos, sFloatAry),
                paint);
    }

    @Override
    public void drawPosText(String text, double[] pos, final SafePaint paint) {
        getWrappedCanvas().drawPosText(text, this.toOffsetFloatAry(pos, sFloatAry), paint);
    }

    @Override
    public void drawRGB(int r, int g, int b) {
        getWrappedCanvas().drawRGB(r, g, b);
    }

    @Override
    public void drawRect(double left, double top, double right, double bottom,
                         final SafePaint paint) {
        left += xOffset;
        right += xOffset;
        top += yOffset;
        bottom += yOffset;
        getWrappedCanvas().drawRect((float) left, (float) top, (float) right, (float) bottom,
                paint);
    }

    @Override
    public void drawRect(Rect r, SafePaint paint) {
        r.offset(xOffset, yOffset);
        getWrappedCanvas().drawRect(r, paint);
        r.offset(-xOffset, -yOffset);
    }

    @Override
    public void drawRoundRect(Rect rect, float rx, float ry, final SafePaint paint) {
        getWrappedCanvas().drawRoundRect(this.toOffsetRectF(rect, sRectF), rx, ry, paint);
    }

    @Override
    public void drawText(String text, double x, double y, final SafePaint paint) {
        getWrappedCanvas().drawText(text, (float) (x + xOffset), (float) (y + yOffset), paint);
    }

    @Override
    public void drawText(final String text, final float x, final float y, final Paint paint) {
        getWrappedCanvas().drawText(text, (x + xOffset), (y + yOffset), paint);
    }

    @Override
    public void drawText(char[] text, int index, int count, double x, double y,
                         final SafePaint paint) {
        getWrappedCanvas().drawText(text, index, count, (float) (x + xOffset),
                (float) (y + yOffset), paint);
    }

    @Override
    public void drawText(CharSequence text, int start, int end, double x, double y,
                         final SafePaint paint) {
        getWrappedCanvas().drawText(text, start, end, (float) (x + xOffset), (float) (y + yOffset),
                paint);
    }

    @Override
    public void drawText(String text, int start, int end, double x, double y,
                         final SafePaint paint) {
        getWrappedCanvas().drawText(text, start, end, (float) (x + xOffset), (float) (y + yOffset),
                paint);
    }

    @Override
    public void drawTextOnPath(char[] text, int index, int count, final SafeTranslatedPath path,
                               float hOffset, float vOffset, final SafePaint paint) {
        getWrappedCanvas().drawTextOnPath(text, index, count, path, hOffset, vOffset, paint);
    }

    @Override
    public void drawTextOnPath(String text, SafeTranslatedPath path, float hOffset, float vOffset,
                               final SafePaint paint) {
        getWrappedCanvas().drawTextOnPath(text, path, hOffset, vOffset, paint);
    }

    @Override
    public void drawVertices(VertexMode mode, int vertexCount, double[] verts, int vertOffset,
                             float[] texs, int texOffset, int[] colors, int colorOffset, short[] indices,
                             int indexOffset, int indexCount, final SafePaint paint) {
        getWrappedCanvas().drawVertices(mode, vertexCount, this.toOffsetFloatAry(verts, sFloatAry),
                vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount,
                paint);
    }

    @Override
    public boolean getClipBounds(final Rect bounds) {
        boolean success = getWrappedCanvas().getClipBounds(bounds);
        if (bounds != null) {
            bounds.offset(-xOffset, -yOffset);
        }
        return success;
    }

    @Override
    public int getDensity() {
        return getWrappedCanvas().getDensity();
    }

    @Override
    public DrawFilter getDrawFilter() {
        return getWrappedCanvas().getDrawFilter();
    }

    @Override
    public int getHeight() {

        return getWrappedCanvas().getHeight();
    }

    @Override
    public void getMatrix(Matrix ctm) {

        getWrappedCanvas().getMatrix(ctm);
    }

    @Override
    public int getSaveCount() {

        return getWrappedCanvas().getSaveCount();
    }

    @Override
    public int getWidth() {

        return getWrappedCanvas().getWidth();
    }

    @Override
    public boolean isOpaque() {

        return getWrappedCanvas().isOpaque();
    }

    @Override
    public boolean quickReject(double left, double top, double right, double bottom,
                               final EdgeType type) {
        left += xOffset;
        right += xOffset;
        top += yOffset;
        bottom += yOffset;
        return getWrappedCanvas().quickReject((float) left, (float) top, (float) right,
                (float) bottom, type);
    }

    @Override
    public boolean quickReject(final SafeTranslatedPath path, final EdgeType type) {
        return getWrappedCanvas().quickReject(path, type);
    }

    @Override
    public boolean quickReject(final Rect rect, final EdgeType type) {

        return getWrappedCanvas().quickReject(this.toOffsetRectF(rect, sRectF), type);
    }

    @Override
    public void restore() {

        getWrappedCanvas().restore();
    }

    @Override
    public void restoreToCount(final int saveCount) {
        getWrappedCanvas().restoreToCount(saveCount);
    }

    @Override
    public void rotate(float degrees) {
        getWrappedCanvas().translate(this.xOffset, this.yOffset);
        getWrappedCanvas().rotate(degrees);
        getWrappedCanvas().translate(-this.xOffset, -this.yOffset);
    }

    @Override
    public void rotate(float degrees, double px, double py) {
        getWrappedCanvas().rotate(degrees, (float) (px + xOffset), (float) (py + yOffset));
    }

    @Override
    public int save() {

        return getWrappedCanvas().save();
    }

    @Override
    public int save(int saveFlags) {

        return getWrappedCanvas().save(saveFlags);
    }

    @Override
    public int saveLayer(double left, double top, double right, double bottom, SafePaint paint,
                         int saveFlags) {
        return getWrappedCanvas().saveLayer((float) (left + xOffset), (float) (top + yOffset),
                (float) (right + xOffset), (float) (bottom + yOffset), paint, saveFlags);
    }

    @Override
    public int saveLayer(Rect bounds, SafePaint paint, int saveFlags) {
        int result =
                getWrappedCanvas().saveLayer(this.toOffsetRectF(bounds, sRectF), paint, saveFlags);
        return result;
    }

    @Override
    public int saveLayerAlpha(double left, double top, double right, double bottom, int alpha,
                              int saveFlags) {
        return getWrappedCanvas().saveLayerAlpha((float) (left + xOffset), (float) (top + yOffset),
                (float) (right + xOffset), (float) (bottom + yOffset), alpha, saveFlags);
    }

    @Override
    public int saveLayerAlpha(Rect bounds, int alpha, int saveFlags) {
        return getWrappedCanvas().saveLayerAlpha(this.toOffsetRectF(bounds, sRectF), alpha,
                saveFlags);
    }

    @Override
    public void scale(float sx, float sy) {
        getWrappedCanvas().scale(sx, sy);
    }

    @Override
    public void scale(float sx, float sy, double px, double py) {
        getWrappedCanvas().scale(sx, sy, (float) (px + xOffset), (float) (py + yOffset));
    }

    @Override
    public void setBitmap(Bitmap bitmap) {
        getWrappedCanvas().setBitmap(bitmap);
    }

    @Override
    public void setDensity(int density) {
        getWrappedCanvas().setDensity(density);
    }

    @Override
    public void setDrawFilter(DrawFilter filter) {
        getWrappedCanvas().setDrawFilter(filter);
    }

    @Override
    public void setMatrix(Matrix matrix) {
        getWrappedCanvas().setMatrix(matrix);
    }

    @Override
    public void skew(float sx, float sy) {
        getWrappedCanvas().skew(sx, sy);
    }

    @Override
    public void translate(float dx, float dy) {
        getWrappedCanvas().translate(dx, dy);
    }

    @Override
    public boolean equals(Object o) {
        return getWrappedCanvas().equals(o);
    }

    @Override
    public int hashCode() {
        return getWrappedCanvas().hashCode();
    }

    @Override
    public String toString() {
        return getWrappedCanvas().toString();
    }

    /**
     * Helper function to convert a Rect to RectF and adjust the values of the Rect by the offsets.
     */
    protected final RectF toOffsetRectF(Rect rect, RectF reuse) {
        if (reuse == null) {
            reuse = new RectF();
        }

        reuse.set(rect.left + xOffset, rect.top + yOffset, rect.right + xOffset,
                rect.bottom + yOffset);
        return reuse;
    }

    /**
     * Helper function to convert a Rect to RectF and adjust the values of the Rect by the offsets.
     */
    protected final float[] toOffsetFloatAry(double[] rect, float[] reuse) {
        if (reuse == null || reuse.length < rect.length) {
            reuse = new float[rect.length];
        }

        for (int a = 0; a < rect.length; a++) {
            reuse[a] = (float) (rect[a] + (a % 2 == 0 ? xOffset : yOffset));
        }
        return reuse;
    }
}

package com.mapbox.mapboxsdk.views;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;

/**
 * Created by nitrog42 on 09/04/15.
 */
public class NumberBitmapDrawable extends BitmapDrawable {
    protected int mCount;
    protected Paint mPaint;
    private static final int TEXT_SIZE = 16;

    public NumberBitmapDrawable(final Resources res, final Bitmap bitmap) {
        this(res, bitmap, 0);
    }

    public NumberBitmapDrawable(final Resources res, final Bitmap bitmap, final int count) {
        super(res, bitmap);
        mCount = count;
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE, res.getDisplayMetrics()));
        paint.setTextAlign(Paint.Align.CENTER);
        setTextPaint(paint);
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        drawText(canvas);
    }

    protected void drawText(Canvas canvas) {
        canvas.drawText(String.valueOf(mCount), getBounds().centerX(), getBounds().centerY() + mPaint.descent(), mPaint);
    }

    public void setCount(final int count) {
        mCount = count;
    }

    public int getCount() {
        return mCount;
    }

    public void setTextPaint(Paint paint) {
        mPaint = paint;
    }


}

package com.mapbox.mapboxsdk.views.safecanvas;

import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;

/**
 * The SafePaint class is designed to work in conjunction with {@link SafeTranslatedCanvas} to work
 * around various Android issues with large canvases.
 *
 * @author Marc Kurtz
 * @see {@link ISafeCanvas}
 */
public class SafePaint extends Paint {

    @Override
    public PathEffect setPathEffect(PathEffect effect) {
        if (effect instanceof DashPathEffect) {
            throw new RuntimeException(
                    "Do not use DashPathEffect. Use SafeDashPathEffect instead.");
        }
        return super.setPathEffect(effect);
    }
}

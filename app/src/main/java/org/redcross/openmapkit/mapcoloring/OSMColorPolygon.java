package org.redcross.openmapkit.mapcoloring;

import android.graphics.Paint;
import android.graphics.Path;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.OSMWay;

/**
 * Created by coder on 8/11/15.
 */
public class OSMColorPolygon extends OSMColorPath {
    private int a;
    private int r;
    private int g;
    private int b;
    /**
     * This should only be constructed by
     * OSMPath.createOSMPath
     * * * *
     * @param w
     */
    protected OSMColorPolygon(OSMWay w, MapView mv) {
        super(w, mv);

        // color polygon according to if it has been edited before

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(a, r, g, b);
    }

    /**
     * For now, we are drawing all of the polygons, even those outside of the canvas.
     *
     * This isn't too much of a problem for now, because the Spatial Index will give us
     * only polygons that intersect.
     *
     * This can be problematic for very large polygons.
     * * * * * * * *
     * @param path
     * @param projectedPoint
     * @param nextProjectedPoint
     * @param screenPoint
     */
    @Override
    void clipOrDrawPath(Path path, double[] projectedPoint, double[] nextProjectedPoint, double[] screenPoint) {
        if (pathLineToReady) {
            path.lineTo( (float) screenPoint[0], (float) screenPoint[1] );
        } else {
            path.moveTo( (float) screenPoint[0], (float) screenPoint[1] );
            pathLineToReady = true;
        }
    }
}

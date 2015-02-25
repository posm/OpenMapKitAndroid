package com.spatialdev.osm.renderer;

import android.graphics.Paint;
import android.graphics.Path;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.OSMWay;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class OSMPolygon extends OSMPath {

    // OSM LAVENDER
    private static final int DEFAULT_A = 50;
    private static final int DEFAULT_R = 62;
    private static final int DEFAULT_G = 107;
    private static final int DEFAULT_B = 255;
    
    // GOLD
    private static final int DEFAULT_SELECTED_A = 180;
    private static final int DEFAULT_SELECTED_R = 255;
    private static final int DEFAULT_SELECTED_G = 140;
    private static final int DEFAULT_SELECTED_B = 0;

    // MAROON
    private static final int DEFAULT_EDITED_A = 50;
    private static final int DEFAULT_EDITED_R = 245;
    private static final int DEFAULT_EDITED_G = 17;
    private static final int DEFAULT_EDITED_B = 135;

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
    protected OSMPolygon(OSMWay w, MapView mv) {
        super(w, mv);

        // color polygon according to if it has been edited before
        if (w.isModified()) {
            this.a = DEFAULT_EDITED_A;
            this.r = DEFAULT_EDITED_R;
            this.g = DEFAULT_EDITED_G;
            this.b = DEFAULT_EDITED_B;
        } else {
            this.a = DEFAULT_A;
            this.r = DEFAULT_R;
            this.g = DEFAULT_G;
            this.b = DEFAULT_B;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(a, r, g, b);
    }

    @Override
    public void select() {
        paint.setARGB(DEFAULT_SELECTED_A, DEFAULT_SELECTED_R, DEFAULT_SELECTED_G, DEFAULT_SELECTED_B);
    }

    @Override
    public void deselect() {
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

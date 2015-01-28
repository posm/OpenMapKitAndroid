package com.spatialdev.osm.renderer;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.Way;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class OSMPolygon extends OSMPath {

    /**
     * This should only be constructed by
     * OSMPath.createOSMPath
     * * * *
     * @param w
     */
    protected OSMPolygon(Way w, MapView mv) {
        this(w, mv, Color.GREEN);
    }
    
    // no border stroke
    protected OSMPolygon(Way w, MapView mv, int color) {
        super(w, mv);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }
    
    // border stroke
//    protected OSMPolygon(Way w, MapView mv, int color, float width) {
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(color);
//    }

    /**
     * For now, we are drawing all of the polygons, even those outside of the canvas.
     * 
     * This isn't too much of a problem for now, because the RTree will give us
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

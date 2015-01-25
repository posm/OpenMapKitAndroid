package com.spatialdev.osm.renderer;

import android.graphics.Color;
import android.graphics.Paint;

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
}

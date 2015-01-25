package com.spatialdev.osm.renderer;

import android.graphics.Color;
import android.graphics.Paint;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.Node;
import com.spatialdev.osm.model.Way;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class OSMLine extends OSMPath {

    /**
     * This should only be constructed by
     * OSMPath.createOSMPath
     * * * *
     * @param w
     */
    protected OSMLine(Way w, MapView mv) {
        this(w, mv, Color.BLUE, 10.0f);
    }

    protected OSMLine(Way w, MapView mv, int color, float width) {
        super(w, mv);
        paint.setColor(color);
        paint.setAntiAlias(true);
        setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);

    }

}

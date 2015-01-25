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
     * OSMPath.createOSMPathFromOSMElement
     * * * *
     * @param w
     */
    protected OSMLine(Way w) {
        this(w, Color.BLUE, 10.0f);
    }

    public OSMLine(Way w, int color, float width) {
        super(w);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);

    }

}

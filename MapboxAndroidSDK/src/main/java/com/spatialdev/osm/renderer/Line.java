package com.spatialdev.osm.renderer;

import android.graphics.Color;
import android.graphics.Paint;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.Node;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class Line extends OSMPath {

    public Line(List<Node> nodes, MapView mapView) {
        this(nodes, mapView, Color.BLUE, 10.0f);
    }

    public Line(List<Node> nodes, MapView mapView, int color, float width) {
        super(nodes, mapView);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);

    }

}

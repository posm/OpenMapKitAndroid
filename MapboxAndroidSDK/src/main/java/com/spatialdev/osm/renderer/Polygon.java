package com.spatialdev.osm.renderer;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.Node;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class Polygon extends OSMPath {

    public Polygon(List<Node> nodes, MapView mapView) {
        super(nodes, mapView);
    }
}

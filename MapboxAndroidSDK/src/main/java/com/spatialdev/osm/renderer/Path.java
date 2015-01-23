package com.spatialdev.osm.renderer;

import android.graphics.PointF;

import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.spatialdev.osm.model.Node;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public abstract class Path {
    
    // These are the points for a path converted to an "intermediate"
    // pixel space of the entire earth.
    protected PointF[] projectedPoints;
    
    public Path(List<Node> nodes, MapView mapView) {
        projectNodes(nodes, mapView);
        
    }
    
//    public Path(List<Node> nodes, MapView mapView, )

    /**
     * Do the expensive projection straight up upon construction rather than draw.
     * 
     * @param nodes
     * @param mapView
     */
    private void projectNodes(List<Node> nodes, MapView mapView) {
        projectedPoints = new PointF[nodes.size()];
        final Projection pj = mapView.getProjection();
        int i = 0;
        for (Node n : nodes) {
            projectedPoints[i++] = pj.toMapPixelsProjected(n.getLng(), n.getLat(), null);
        }
    }
}

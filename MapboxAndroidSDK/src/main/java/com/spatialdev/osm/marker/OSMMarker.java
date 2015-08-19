package com.spatialdev.osm.marker;

import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.OSMNode;

/**
 * OSMMarker is a subclass of Marker that has a reference
 * to the OSMNode so that we can get the applicable OSM object
 * when the user selects the given marker.
 */
public class OSMMarker extends Marker {

    private OSMNode node;

    public OSMMarker(MapView mapView, OSMNode node) {
        // We don't care about title and description in
        // markers, because we have more detailed tags
        // in the OSMNode itself.
        super(mapView, "", "", node.getLatLng());
        this.node = node;
        node.setMarker(this);
    }

    public OSMNode getNode() {
        return node;
    }

    public MapView getMapView() {
        return mapView;
    }
}

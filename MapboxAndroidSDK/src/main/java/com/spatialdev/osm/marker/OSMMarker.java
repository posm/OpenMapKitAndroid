package com.spatialdev.osm.marker;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.spatialdev.osm.model.OSMNode;

/**
 * OSMMarker is a subclass of Marker that has a reference
 * to the OSMNode so that we can get the applicable OSM object
 * when the user selects the given marker.
 */
public class OSMMarker extends Marker {

    private OSMNode node;

    public OSMMarker(OSMNode node) {
        // We don't care about title and description in
        // markers, because we have more detailed tags
        // in the OSMNode itself.
        super("", "", node.getLatLng());
        this.node = node;
    }

    public OSMNode getNode() {
        return node;
    }

}

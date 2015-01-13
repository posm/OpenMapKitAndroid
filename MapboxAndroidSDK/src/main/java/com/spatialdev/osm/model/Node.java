/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.LinkedList;
import java.util.List;

public class Node extends OSMElement {

    private LatLng latLng;

    private LinkedList<Relation> linkedRelations = new LinkedList<>();

    public Node( String idStr,
                 String latStr,
                 String lonStr,
                 String versionStr,
                 String timestampStr,
                 String changesetStr,
                 String uidStr,
                 String userStr ) {

        super(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);

        double lat = Double.valueOf(latStr);
        double lon = Double.valueOf(lonStr);
        latLng = new LatLng(lat, lon);
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public double getLat() {
        return latLng.getLatitude();
    }

    public double getLng() {
        return latLng.getLongitude();
    }

    public void addRelation(Relation relation) {
        linkedRelations.push(relation);
    }

    public List<Relation> getRelations() {
        return linkedRelations;
    }

    @Override
    public Object getOverlay() {
        return null;
    }

}

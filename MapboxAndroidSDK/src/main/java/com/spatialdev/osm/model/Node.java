/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.xmlpull.v1.XmlSerializer;

import java.util.LinkedList;
import java.util.List;

public class Node extends OSMElement {

    private double lat;
    private double lng;

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

        lat = Double.valueOf(latStr);
        lng = Double.valueOf(lonStr);
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void addRelation(Relation relation) {
        linkedRelations.push(relation);
    }

    public List<Relation> getRelations() {
        return linkedRelations;
    }

    @Override
    public void xml(XmlSerializer xmlSerializer) {

    }

    @Override
    public Object getOverlay() {
        return null;
    }

}

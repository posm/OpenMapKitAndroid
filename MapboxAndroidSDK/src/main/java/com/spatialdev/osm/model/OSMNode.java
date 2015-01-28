/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class OSMNode extends OSMElement {

    private double lat;
    private double lng;

    private LinkedList<OSMRelation> linkedRelations = new LinkedList<>();

    public OSMNode(String idStr,
                   String latStr,
                   String lonStr,
                   String versionStr,
                   String timestampStr,
                   String changesetStr,
                   String uidStr,
                   String userStr) {

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

    public void addRelation(OSMRelation relation) {
        linkedRelations.push(relation);
    }

    public List<OSMRelation> getRelations() {
        return linkedRelations;
    }

    @Override
    void xml(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag(null, "node");
        if (isModified()) {
            xmlSerializer.attribute(null, "action", "modify");
        }
        setOsmElementXmlAttributes(xmlSerializer);
        xmlSerializer.attribute(null, "lat", String.valueOf(lat));
        xmlSerializer.attribute(null, "lon", String.valueOf(lng));
        super.xml(xmlSerializer); // generates tags
        xmlSerializer.endTag(null, "node");
    }

}

/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import com.mapbox.mapboxsdk.R;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.spatialdev.osm.marker.OSMMarker;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OSMNode extends OSMElement {

    private double lat;
    private double lng;

    private LinkedList<OSMRelation> linkedRelations = new LinkedList<>();

    // This is only for standalone nodes.
    private OSMMarker marker;

    /**
     * This constructor is used by OSMDataSet in the XML parsing process.
     */
    public OSMNode(String idStr,
                   String latStr,
                   String lonStr,
                   String versionStr,
                   String timestampStr,
                   String changesetStr,
                   String uidStr,
                   String userStr,
                   String action) {

        super(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr, action);

        lat = Double.valueOf(latStr);
        lng = Double.valueOf(lonStr);
    }

    /**
     * This constructor is used when we are creating an new OSMElement,
     * such as when a new Node is created. This constructor assumes
     * that we are creating a NEW element in the current survey.
     */
    public OSMNode(LatLng latLng) {
        super(); // super sets the element to be modified
        lat = latLng.getLatitude();
        lng = latLng.getLongitude();
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

    /**
     * This moves an OSMNode to a new location on the map.
     *
     * The LatLng in the node is reset, the LatLng in the
     * marker is reset, and the node is removed and replaced
     * in the JTSModel's spatial index.
     *
     * @param jtsModel - the model in which the node is spatially indexed
     * @param latLng - the new location to move the node to
     */
    public void move(JTSModel jtsModel, LatLng latLng) {
        lat = latLng.getLatitude();
        lng = latLng.getLongitude();
        jtsModel.removeOSMElement(this);
        jtsModel.addOSMStandaloneNode(this);
        if (marker != null) {
            marker.setPoint(latLng);
        }
    }

    /**
     * Deletes this OSMNode from the JTSModel.
     *
     * @param jtsModel - the model in which the node is spatially indexed
     */
    public void delete(JTSModel jtsModel) {
        jtsModel.removeOSMElement(this);
        if (marker != null) {
            // Because that marker is selected, it's also rendered as the focused item in addition
            // to being on the display list. The marker will still appear on the map if we
            // don't do this...
            marker.setVisibility(false);
            ItemizedOverlay overlay = marker.getParentHolder();
            if (overlay != null) {
                overlay.setFocus(null);
            }
        }
    }

    public void addRelation(OSMRelation relation) {
        linkedRelations.push(relation);
    }

    public List<OSMRelation> getRelations() {
        return linkedRelations;
    }

    /**
     * We want a reference to a marker when the node is a standalone node.
     * This is a reference held when the marker is created
     * in OSMOverlay#renderMarker
     *
     * @param marker
     */
    public void setMarker(OSMMarker marker) {
        this.marker = marker;
    }

    /**
     * We will get a marker only if the node is standalone.
     * This is a reference held when the marker is created
     * in OSMOverlay#renderMarker
     *
     * @return
     */
    public Marker getMarker() {
        return marker;
    }

    /**
     * The checksum of an OSMNode is the sorted k,v of the tags
     * with the lat and long following.
     *
     * @return
     */
    @Override
    public String checksum() {
        StringBuilder str = tagsAsSortedKVString();
        str.append(lat);
        str.append(lng);
        return new String(Hex.encodeHex(DigestUtils.sha1(str.toString())));
    }

    @Override
    void xml(XmlSerializer xmlSerializer, String omkOsmUser) throws IOException {
        xmlSerializer.startTag(null, "node");
        setOsmElementXmlAttributes(xmlSerializer, omkOsmUser);
        xmlSerializer.attribute(null, "lat", String.valueOf(lat));
        xmlSerializer.attribute(null, "lon", String.valueOf(lng));
        super.xml(xmlSerializer, omkOsmUser); // generates tags
        xmlSerializer.endTag(null, "node");
    }

    @Override
    public void select() {
        super.select();
        if (marker != null) {
            marker.setMarker(marker.getMapView().getContext().getResources().getDrawable(R.mipmap.maki_star_orange));
        } else {
            // Very wretched hack. Something is wrong with Mapbox Android SDK (Deprecated). Satisfies #98
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (marker != null) {
                        marker.setMarker(marker.getMapView().getContext().getResources().getDrawable(R.mipmap.maki_star_orange));
                    }
                }
            }, 100);
        }
    }

    @Override
    public void deselect() {
        super.deselect();
        if (marker != null) {
            marker.setMarker(marker.getMapView().getContext().getResources().getDrawable(R.mipmap.maki_star_blue));
        }
    }
}

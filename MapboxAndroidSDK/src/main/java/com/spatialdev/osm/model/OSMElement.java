package com.spatialdev.osm.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Created by Nicholas Hallahan on 1/2/15.
 * nhallahan@spatialdev.com
 */
public abstract class OSMElement {
    protected long id;
    protected long version;
    protected String timestamp;
    protected long changeset;
    protected long uid;
    protected String user;
    protected boolean selected = false;

    protected Geometry jtsGeom;

    protected Object overlay;

    protected Map<String, String> tags = new LinkedHashMap<>();

    public OSMElement(String idStr,
                      String versionStr,
                      String timestampStr,
                      String changesetStr,
                      String uidStr,
                      String userStr) {

        id = Long.valueOf(idStr);
        version = Long.valueOf(versionStr);
        timestamp = timestampStr;
        changeset = Long.valueOf(changesetStr);
        uid = Long.valueOf(uidStr);
        user = userStr;
    }

    public void addTag(String k, String v) {
        tags.put(k, v);
    }

    public long getId() {
        return id;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public int getTagCount() {
        return tags.size();
    }

    public void setJTSGeom(Geometry geom) {
        jtsGeom = geom;
    }

    public Geometry getJTSGeom() {
        return jtsGeom;
    }

    public void select() {
        selected = true;
    }

    public void deselect() {
        selected = false;
    }

    /**
     * If you set an overlay object for this element, it will
     * not try to create it's own overlay for itself. It will
     * return the overlay that you gave it.
     *
     * You are responsible for adding this object's geometry
     * to the overlay layer.
     *
     * @param overlay
     */
    public Object setOverlay(Object overlay) {
        this.overlay = overlay;
        return overlay;
    }

    /**
     * Returns a object that can draw as
     * an overlay or marker in the Mapbox Android SDK.
     *
     * If an overlay has not been set, this element should
     * create an overlay that represents itself.
     *
     * @return a Marker or PathOverlay object
     */
    public abstract Object getOverlay();

    public void toggle() {
        if (selected) {
            deselect();
        } else {
            select();
        }
    }

    public boolean isSelected() {
        return selected;
    }
}

/**
 * Created by Nicholas Hallahan on 1/2/15.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.spatialdev.osm.renderer.OSMPath;
import com.vividsolutions.jts.geom.Geometry;

import org.xmlpull.v1.XmlSerializer;


public abstract class OSMElement {
    
    private static LinkedList<OSMElement> selectedElements = new LinkedList<>();
    private static boolean selectedElementsChanged = false;
    
    private static LinkedList<OSMElement> modifiedElements = new LinkedList<>();
    
    protected long id;
    protected long version;
    protected String timestamp;
    protected long changeset;
    protected long uid;
    protected String user;
    protected boolean selected = false;
    
    // set to true if the application modifies tags for this element
    protected boolean modified = false;

    protected Geometry jtsGeom;

    /**
     * These tags get modified by the application
     */
    protected Map<String, String> tags = new LinkedHashMap<>();

    /**
     * These tags are the original tags in the data set. This SHOULD NOT BE MODIFIED. 
     */
    protected Map<String, String> originalTags = new LinkedHashMap<>();

    /**
     * This is the object that actually gets drawn by OSMOverlay. 
     */
    protected OSMPath osmPath;

    /**
     * Elements that have been put in a select state* 
     * @return
     */
    public static LinkedList<OSMElement> getSelectedElements() {
        return selectedElements;
    }
    
    public static LinkedList<OSMElement> getModifiedElements() {
        return modifiedElements;        
    }
    
    public static boolean hasSelectedElementsChanged() {
        if (selectedElementsChanged) {
            selectedElementsChanged = false;
            return true;
        }
        return false;
    }
    
    public static void deselectAll() {
        for (OSMElement el : selectedElements) {
            selectedElementsChanged = true;
            el.deselect();
        }
    }
    
    public OSMElement(String idStr,
                      String versionStr,
                      String timestampStr,
                      String changesetStr,
                      String uidStr,
                      String userStr,
                      String action) {
        try {
            id = Long.valueOf(idStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            version = Long.valueOf(versionStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            timestamp = timestampStr;
        } catch (Exception e) {
            // dont assign
        }
        try {
            changeset = Long.valueOf(changesetStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            uid = Long.valueOf(uidStr);
        } catch (Exception e) {
            // dont assign
        }
        try {
            user = userStr;
        } catch (Exception e) {
            // dont assign
        }
        if (action != null && action.equals("modify")) {
            modified = true;
        }
    }

    void xml(XmlSerializer xmlSerializer) throws IOException {
        // set the tags for the element (all element types can have tags)
        Set<String> tagKeys = tags.keySet();
        for (String tagKey : tagKeys) {
            xmlSerializer.startTag(null, "tag");
            xmlSerializer.attribute(null, "k", tagKey);
            xmlSerializer.attribute(null, "v", tags.get(tagKey));
            xmlSerializer.endTag(null, "tag");
        }
    }
    
    protected void setOsmElementXmlAttributes(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.attribute(null, "id", String.valueOf(id));
        if (isModified()) {
            xmlSerializer.attribute(null, "action", "modify");
        }
        xmlSerializer.attribute(null, "version", String.valueOf(version));
        xmlSerializer.attribute(null, "changeset", String.valueOf(changeset));
        xmlSerializer.attribute(null, "timestamp", timestamp);
    }
    
    /**
     * If a tag is edited or added, this should be called by the application.* 
     * @param k
     * @param v
     */
    public void addOrEditTag(String k, String v) {
        String origVal = tags.get(k);
        // if the original tag is the same as this, we're not really editing anything.
        if (v.equals(origVal)) {
            return;
        }
        modified = true;
        tags.put(k, v);
        modifiedElements.add(this);
    }

    /**
     * If the user removes a tag, call this method with the key of the tag.* 
     * @param k
     */
    public void deleteTag(String k) {
        String origVal = tags.get(k);
        // Don't do anything if we are not deleting anything.
        if (origVal == null) {
            return;
        }
        modified = true;
        tags.remove(k);
        modifiedElements.add(this);
    }
    
    public boolean isModified() {
        return modified;
    }
    
    /**
     * This should only be used by the parser. 
     * @param k
     * @param v
     */
    public void addParsedTag(String k, String v) {
        originalTags.put(k, v);
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
        selectedElementsChanged = true;
        selected = true;
        selectedElements.push(this);
        if (osmPath != null) {
            osmPath.select();
        }
    }

    public void deselect() {
        selectedElementsChanged = true;
        selected = false;
        selectedElements.remove(this);
        if (osmPath != null) {
            osmPath.deselect();
        }
    }

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

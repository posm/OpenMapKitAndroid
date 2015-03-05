package org.redcross.openmapkit.tagswipe;

import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.odkcollect.ODKCollectData;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nicholas Hallahan on 3/4/15.
 * nhallahan@spatialdev.com
 * * *
 */
public class TagEdit {

    private static LinkedHashMap<String, TagEdit> tagEdits;
    
    private String tagKey;
    private String tagVal;
    private ODKTag odkTag;
    private boolean readOnly;
    private int idx = -1;
    
    public static LinkedHashMap<String, TagEdit> buildTagEditHash() {
        int idx = 0;
        tagEdits = new LinkedHashMap<>();
        OSMElement osmElement = OSMElement.getSelectedElements().getFirst();
        Map<String, String> tags = osmElement.getTags();
        
        // Tag Edits for ODK Collect Mode
        if (ODKCollectHandler.isODKCollectMode()) {
            Map<String, String> readOnlyTags = new HashMap<>(tags);
            ODKCollectData odkCollectData = ODKCollectHandler.getODKCollectData();
            Collection<ODKTag> requiredTags = odkCollectData.getRequiredTags();
            for (ODKTag odkTag : requiredTags) {
                String tagKey = odkTag.getKey();
                TagEdit tagEdit = new TagEdit(tagKey, tags.get(tagKey), odkTag, false, idx++);
                tagEdits.put(tagKey, tagEdit);
                readOnlyTags.remove(tagKey);
            }
            Set<String> readOnlyKeys = readOnlyTags.keySet();
            for (String readOnlyKey : readOnlyKeys) {
                TagEdit tagEdit = new TagEdit(readOnlyKey, readOnlyTags.get(readOnlyKey), true, idx++);
                tagEdits.put(readOnlyKey, tagEdit);
            }
        }
        
        // Tag Edits for Standalone Mode
        else {
            Set<String> keys = tags.keySet();
            for (String key : keys) {
                TagEdit tagEdit = new TagEdit(key, tags.get(key), false, idx++);
                tagEdits.put(key, tagEdit);
            }
        }
        
        return tagEdits;
    }
    
    public static LinkedHashMap<String, TagEdit> getTagEdits() {
        return tagEdits;
    }
    
    private TagEdit(String tagKey, String tagVal, ODKTag odkTag, boolean readOnly, int idx) {
        this.tagKey = tagKey;
        this.tagVal = tagVal;
        this.odkTag = odkTag;
        this.readOnly = readOnly;
        this.idx = idx;
    }
    
    private TagEdit(String tagKey, String tagVal, boolean readOnly, int idx) {
        this.tagKey = tagKey;
        this.tagVal = tagVal;
        this.readOnly = readOnly;
        this.idx = idx;
    }
    
    public String getTitle() {
        return tagKey;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public boolean isSelectOne() {
        if ( !readOnly &&
                odkTag != null &&
                odkTag.getItems().size() > 0 ) {
            return true;
        }
        return false;
    }
    
    public int getIndex() {
        return idx;
    }
    
}

package org.redcross.openmapkit.tagswipe;

import android.widget.EditText;

import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.odkcollect.ODKCollectData;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;
import org.redcross.openmapkit.odkcollect.tag.ODKTagItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nicholas Hallahan on 3/4/15.
 * nhallahan@spatialdev.com
 * * *
 */
public class TagEdit {

    private static LinkedHashMap<String, TagEdit> tagEditHash;
    private static List<TagEdit> tagEdits;
    private static OSMElement osmElement;
    
    private String tagKey;
    private String tagVal;
    private ODKTag odkTag;
    private boolean readOnly;
    private int idx = -1;
    private EditText editText;
    
    public static List<TagEdit> buildTagEdits() {
        int idx = 0;
        tagEditHash = new LinkedHashMap<>();
        tagEdits = new ArrayList<>();
        osmElement = OSMElement.getSelectedElements().getFirst();
        Map<String, String> tags = osmElement.getTags();
        
        // Tag Edits for ODK Collect Mode
        if (ODKCollectHandler.isODKCollectMode()) {
            Map<String, String> readOnlyTags = new HashMap<>(tags);
            ODKCollectData odkCollectData = ODKCollectHandler.getODKCollectData();
            Collection<ODKTag> requiredTags = odkCollectData.getRequiredTags();
            for (ODKTag odkTag : requiredTags) {
                String tagKey = odkTag.getKey();
                TagEdit tagEdit = new TagEdit(tagKey, tags.get(tagKey), odkTag, false, idx++);
                tagEditHash.put(tagKey, tagEdit);
                tagEdits.add(tagEdit);
                readOnlyTags.remove(tagKey);
            }
            Set<String> readOnlyKeys = readOnlyTags.keySet();
            for (String readOnlyKey : readOnlyKeys) {
                TagEdit tagEdit = new TagEdit(readOnlyKey, readOnlyTags.get(readOnlyKey), true, idx++);
                tagEditHash.put(readOnlyKey, tagEdit);
                tagEdits.add(tagEdit);
            }
        }
        
        // Tag Edits for Standalone Mode
        else {
            Set<String> keys = tags.keySet();
            for (String key : keys) {
                TagEdit tagEdit = new TagEdit(key, tags.get(key), false, idx++);
                tagEditHash.put(key, tagEdit);
                tagEdits.add(tagEdit);
            }
        }
        
        return tagEdits;
    }
    
    public static TagEdit getTag(int idx) {
        return tagEdits.get(idx);
    }
    
    public static TagEdit getTag(String key) {
        return tagEditHash.get(key);        
    }

    public static int getIndexForTagKey(String key) {
        TagEdit tagEdit = tagEditHash.get(key);
        if (tagEdit != null) {
            return tagEdit.getIndex();
        }
        // If its not there, just go to the first TagEdit
        return 0;
    }
    
    public static void saveToODKCollect() {
        updateTagsInOSMElement();
        ODKCollectHandler.saveXmlInODKCollect(osmElement);
    }
    
    private static void updateTagsInOSMElement() {
        for (TagEdit tagEdit : tagEdits) {
            tagEdit.updateTagInOSMElement();
        }
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

    /**
     * The EditText widget from StringTagValueFragment gets passed into here
     * so that the value can be retrieved on save.
     * * * *
     * @param editText
     */
    public void setEditText(EditText editText) {
        this.editText = editText;
    }
    
    // TODO: Clint, this is where we get the actual value
    // from the editText, and we need to do something
    // similar to get it from the set of radio buttons.
    private void updateTagInOSMElement() {
        if (editText == null) return; // need to account for when its a select one
        String val = editText.getText().toString();
        tagVal = val;
        osmElement.addOrEditTag(tagKey, tagVal);
    }
    
    public String getTitle() {
        return tagKey;
    }
    
    public String getTagKeyLabel() {
        if (odkTag != null) {
            return odkTag.getLabel();
        }
        return null;
    }

    public String getTagKey() {
        return tagKey;
    }
    
    public String getTagValLabel() {
        if (odkTag == null) return null;
        ODKTagItem item = odkTag.getItem(tagVal);
        if (item != null) {
            return item.getLabel();
        }
        return null;
    }
    
    public String getTagVal() {
        return tagVal;
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

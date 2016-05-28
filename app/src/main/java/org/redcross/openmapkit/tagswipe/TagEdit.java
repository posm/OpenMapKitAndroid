package org.redcross.openmapkit.tagswipe;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.Constraints;
import org.redcross.openmapkit.odkcollect.ODKCollectData;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;
import org.redcross.openmapkit.odkcollect.tag.ODKTagItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
    private static LinkedHashMap<String, TagEdit> tagEditHiddenHash;
    private static List<TagEdit> tagEdits;
    private static OSMElement osmElement;
    private static TagSwipeActivity tagSwipeActivity;
    
    private final String tagKey; // a given TagEdit always associates to an immutable key
    private String tagVal;
    private ODKTag odkTag;
    private boolean readOnly;
    private boolean checkBoxMode = false;
    private EditText editText;
    private RadioGroup radioGroup;

    /**
     * For CheckBox mode.
     */
    private CheckBox editTextCheckBox;
    private EditText checkBoxEditText;

    /**
     * Factory Method that gives collection of instances.
     *
     * @return
     */
    public static List<TagEdit> buildTagEdits() {
        tagEditHash = new LinkedHashMap<>();
        tagEditHiddenHash = new LinkedHashMap<>();
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
                TagEdit tagEdit = new TagEdit(tagKey, tagValueOrDefaultValue(tags, tagKey), odkTag, false);
                String implicitVal = Constraints.singleton().implicitVal(tagKey);
                if (implicitVal != null) {
                    tagEditHiddenHash.put(tagKey, tagEdit);
                    osmElement.addOrEditTag(tagKey, implicitVal);
                } else if (Constraints.singleton().tagShouldBeShown(tagKey, osmElement)) {
                    tagEditHash.put(tagKey, tagEdit);
                    tagEdits.add(tagEdit);
                } else {
                    tagEditHiddenHash.put(tagKey, tagEdit);
                }
                readOnlyTags.remove(tagKey);
            }
            Set<String> readOnlyKeys = readOnlyTags.keySet();
            for (String readOnlyKey : readOnlyKeys) {
                TagEdit tagEdit = new TagEdit(readOnlyKey, readOnlyTags.get(readOnlyKey), true);
                tagEditHash.put(readOnlyKey, tagEdit);
                tagEdits.add(tagEdit);
            }
        }
        
        // Tag Edits for Standalone Mode
        else {
            Set<String> keys = tags.keySet();
            for (String key : keys) {
                TagEdit tagEdit = new TagEdit(key, tags.get(key), false);
                tagEditHash.put(key, tagEdit);
                tagEdits.add(tagEdit);
            }
        }
        
        return tagEdits;
    }

    private static String tagValueOrDefaultValue(Map<String,String> tags, String tagKey) {
        String tagVal = tags.get(tagKey);
        if (tagVal == null) {
            // null if there is no default
            tagVal = Constraints.singleton().tagDefaultValue(tagKey);
            if (tagVal != null) {
                osmElement.addOrEditTag(tagKey, tagVal);
            }
        }
        return tagVal;
    }

    public static void setTagSwipeActivity(TagSwipeActivity tagSwipeActivity) {
        TagEdit.tagSwipeActivity = tagSwipeActivity;
    }
    
    public static TagEdit getTag(int idx) {
        return tagEdits.get(idx);
    }
    
    public static TagEdit getTag(String key) {
        return tagEditHash.get(key);        
    }

    public static Set<String> hiddenTagKeys() {
        return tagEditHash.keySet();
    }

    public static Set<String> shownTagKeys() {
        return tagEditHiddenHash.keySet();
    }

    public static int getIndexForTagKey(String key) {
        TagEdit tagEdit = tagEditHash.get(key);
        if (tagEdit != null) {
            return tagEdits.indexOf(tagEdit);
        }
        // If its not there, just go to the first TagEdit
        return 0;
    }
    
    public static boolean saveToODKCollect(String osmUserName) {
        updateTagsInOSMElement();

        Set<String> missingTags = Constraints.singleton().requiredTagsNotMet(osmElement);
        if (missingTags.size() > 0) {
            tagSwipeActivity.notifyMissingTags(missingTags);
            return false;
        } else {
            ODKCollectHandler.saveXmlInODKCollect(osmElement, osmUserName);
            return true;
        }
    }

    private static void removeTag(String key, String activeTagKey) {
        if (tagEditHash.get(key) == null) return;
        int idx = getIndexForTagKey(key);
        TagEdit tagEdit = tagEditHash.remove(key);
        tagEditHiddenHash.put(key, tagEdit);
        tagEdits.remove(idx);
        if (tagSwipeActivity != null) {
            tagSwipeActivity.updateUI(activeTagKey);
        }
    }

    private static void addTag(String key, String activeTagKey) {
        if (tagEditHiddenHash.get(key) == null) return;
        int idx = getIndexForTagKey(activeTagKey) + 1;
        TagEdit tagEdit = tagEditHiddenHash.remove(key);
        tagEditHash.put(key, tagEdit);
        tagEdits.add(idx, tagEdit);
        if (tagSwipeActivity != null) {
            tagSwipeActivity.updateUI(activeTagKey);
        }
    }
    
    private static void updateTagsInOSMElement() {
        for (TagEdit tagEdit : tagEdits) {
            tagEdit.updateTagInOSMElement();
        }
    }
    
    private TagEdit(String tagKey, String tagVal, ODKTag odkTag, boolean readOnly) {
        this.tagKey = tagKey;
        this.tagVal = tagVal;
        this.odkTag = odkTag;
        this.readOnly = readOnly;
    }
    
    private TagEdit(String tagKey, String tagVal, boolean readOnly) {
        this.tagKey = tagKey;
        this.tagVal = tagVal;
        this.readOnly = readOnly;
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
    
    public void setRadioGroup(RadioGroup radioGroup) {
        this.radioGroup = radioGroup;
    }

    public void setupEditCheckbox(CheckBox cb, EditText et) {
        checkBoxMode = true;
        editTextCheckBox = cb;
        checkBoxEditText = et;
    }
    
    public ODKTag getODKTag() {
        return odkTag;
    }
    
    public void updateTagInOSMElement() {
        // check boxes
        if (odkTag != null && checkBoxMode) {
            boolean editTextCheckBoxChecked = editTextCheckBox.isChecked();
            if (odkTag.hasCheckedTagValues() || editTextCheckBoxChecked) {
                if (editTextCheckBoxChecked) {
                    tagVal = odkTag.getSemiColonDelimitedTagValues(checkBoxEditText.getText().toString());
                } else {
                    tagVal = odkTag.getSemiColonDelimitedTagValues(null);
                }
                addOrEditTag(tagKey, tagVal);
            } else {
                deleteTag(tagKey);
            }
            return;
        }
        // radio buttons
        if (radioGroup != null && odkTag != null) {
            View v = radioGroup.getChildAt(radioGroup.getChildCount() - 1);
            int checkedId = radioGroup.getCheckedRadioButtonId();
            // has custom value input
            if (v instanceof LinearLayout) {
                LinearLayout customLL = (LinearLayout)v;
                RadioButton customRadio = (RadioButton)customLL.getChildAt(0);
                if (customRadio.isChecked()) {
                    EditText et = (EditText)customLL.getChildAt(1);
                    tagVal = et.getText().toString();
                    addOrEditTag(tagKey, tagVal);
                } else if (checkedId != -1) {
                    tagVal = odkTag.getTagItemValueFromButtonId(checkedId);
                    addOrEditTag(tagKey, tagVal);
                } else {
                    deleteTag(tagKey);
                }
            }
            // no custom value input
            else {
                if (checkedId != -1) {
                    tagVal = odkTag.getTagItemValueFromButtonId(checkedId);
                    addOrEditTag(tagKey, tagVal);
                } else {
                    deleteTag(tagKey);
                }
            }
        }
        // edit text
        else if (editText != null) {
            tagVal = editText.getText().toString();
            addOrEditTag(tagKey, tagVal);
        }
    }

    private void addOrEditTag(String tagKey, String tagVal) {
        osmElement.addOrEditTag(tagKey, tagVal);
        Constraints.TagAction tagAction = Constraints.singleton().tagAddedOrEdited(tagKey, tagVal);
        executeTagAction(tagAction);
    }

    private void deleteTag(String tagKey) {
        osmElement.deleteTag(tagKey);
        tagVal = null;
        Constraints.TagAction tagAction = Constraints.singleton().tagDeleted(tagKey);
        executeTagAction(tagAction);
    }

    private void executeTagAction(Constraints.TagAction tagAction) {
        for (String tag : tagAction.hide) {
            removeTag(tag, tagKey);
        }
        for (String tag : tagAction.show) {
            addTag(tag, tagKey);
        }
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

    public Set<String> getTagVals() {
        Set<String> tagVals = new HashSet<>();
        if (tagVal == null || tagVal.length() < 1) {
            return tagVals;
        }
        String[] vals = tagVal.trim().split(";");
        for (int i = 0; i < vals.length; i++) {
            String val = vals[i];
            tagVals.add(val);
        }
        return tagVals;
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
    
}

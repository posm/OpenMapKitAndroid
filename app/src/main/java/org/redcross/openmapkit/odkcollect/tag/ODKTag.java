package org.redcross.openmapkit.odkcollect.tag;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *	Structure for ODK OSM Tag elements in XForm.
 *  Created by Nicholas Hallahan nhallahan@spatialdev.com
 */
public class ODKTag {
    private String key;
    private String label;
    private LinkedHashMap<String, ODKTagItem> items = new LinkedHashMap<>();
    private Map<Integer, ODKTagItem> buttonIdToODKTagItemHash = new HashMap<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }
    
    public ODKTagItem getItem(String value) {
        return items.get(value);        
    }
    
    public Collection<ODKTagItem> getItems() {
        return items.values();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void addItem(ODKTagItem item) {
        items.put(item.getValue(), item);
    }
    
    public void putButtonIdToTagItemHash(Integer id, ODKTagItem tagItem) {
        buttonIdToODKTagItemHash.put(id, tagItem);
    }
    
    public String getTagItemValueFromButtonId(Integer id) {
        ODKTagItem item = buttonIdToODKTagItemHash.get(id);
        if (item != null) {
            return item.getValue();
        }
        return null;
    }

    public TextView createTagKeyTextView(Activity activity) {
        TextView tv = new TextView(activity);
        if (label != null) {
            tv.setText(label);
        } else {
            tv.setText(key);
        }
        return tv;
    }

    public TextView createTagValueTextView(Activity activity, String initialTagVal) {
        EditText et = new EditText(activity);
        if (initialTagVal != null) {
            et.setText(initialTagVal);
        }
        return et;
    }

    public void addCheckbox(CheckBox cb) {
        checkBoxes.add(cb);
    }

    public boolean hasCheckedTagValues() {
        for (CheckBox cb : checkBoxes) {
            if (cb.isChecked()) {
                return true;
            }
        }
        return false;
    }

    public String getSemiColonDelimitedTagValues(String customValues) {
        String values = null;
        boolean firstVal = true;
        for (CheckBox cb : checkBoxes) {
            if (cb.isChecked()) {
                int id = cb.getId();
                ODKTagItem item = buttonIdToODKTagItemHash.get(id);
                if (item != null) {
                    if (firstVal) {
                        firstVal = false;
                        values = item.getValue();
                    } else {
                        values += ';' + item.getValue();
                    }
                }
            }
        }
        if (customValues != null) {
            customValues = customValues.trim();
            if (customValues.length() > 0) {
                if (firstVal) {
                    values = customValues;
                } else {
                    values += ';' + customValues;
                }
            }
        }
        return values;
    }
}

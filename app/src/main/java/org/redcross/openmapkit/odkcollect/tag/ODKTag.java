package org.redcross.openmapkit.odkcollect.tag;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *	Structure for ODK OSM Tag elements in XForm.
 *  Created by Nicholas Hallahan nhallahan@spatialdev.com
 */
public class ODKTag {
    private String key;
    private String label;
    private LinkedHashMap<String, ODKTagItem> items = new LinkedHashMap<>();
    private Map<Integer, ODKTagItem> radioButtonIdToODKTagItemHash = new HashMap<>();

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
    
    public void putRadioButtonIdToTagItemHash(Integer id, ODKTagItem tagItem) {
        radioButtonIdToODKTagItemHash.put(id, tagItem);        
    }
    
    public String getTagItemValueFromRadioButtonId(Integer id) {
        ODKTagItem item = radioButtonIdToODKTagItemHash.get(id);
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
}

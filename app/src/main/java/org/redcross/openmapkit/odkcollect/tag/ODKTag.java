package org.redcross.openmapkit.odkcollect.tag;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 *	Structure for ODK OSM Tag elements in XForm.
 *  Created by Nicholas Hallahan nhallahan@spatialdev.com
 */
public class ODKTag {
    private String key;
    private String label;
    private List<ODKTagItem> items = new ArrayList<>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<ODKTagItem> getItems() {
        return items;
    }

    public void addItem(ODKTagItem item) {
        items.add(item);
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

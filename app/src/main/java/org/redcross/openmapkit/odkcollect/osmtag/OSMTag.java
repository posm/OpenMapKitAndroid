package org.redcross.openmapkit.odkcollect.osmtag;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 *	Structure for OSM Tag elements in XForm.
 *  Created by Nicholas Hallahan nhallahan@spatialdev.com
 */
public class OSMTag {
    public String key;
    public String label;
    public List<OSMTagItem> items = new ArrayList<>();
    
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

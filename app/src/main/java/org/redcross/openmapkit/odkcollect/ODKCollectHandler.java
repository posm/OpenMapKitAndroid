package org.redcross.openmapkit.odkcollect;

import android.content.Intent;
import android.os.Bundle;

import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.odkcollect.tag.ODKTag;
import org.redcross.openmapkit.odkcollect.tag.ODKTagItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Nicholas Hallahan on 2/9/15.
 * nhallahan@spatialdev.com
 * * * 
 */
public class ODKCollectHandler {

    private static ODKCollectData odkCollectData;
    
    public static void registerIntent(Intent intent) {
        if(intent.getAction().equals("android.intent.action.SEND")) {
            if (intent.getType().equals("text/plain")) {
                Bundle extras = intent.getExtras();
                if(extras != null) {
                    // extract data from intent extras
                    String formId = extras.getString("FORM_ID");
                    String formFileName = extras.getString("FORM_FILE_NAME");
                    String instanceId = extras.getString("INSTANCE_ID");
                    String instanceDir = extras.getString("INSTANCE_DIR");
                    LinkedHashMap<String, ODKTag> requiredTags = generateRequiredOSMTagsFromBundle(extras);
                    odkCollectData = new ODKCollectData(formId, formFileName, instanceId, instanceDir, requiredTags);
                }
            }
        }
    }
    
    public static boolean isODKCollectMode() {
        if (odkCollectData != null) {
            return true;
        }
        return false;
    }
    
    public static boolean isStandaloneMode() {
        if (odkCollectData == null) {
            return true;
        }
        return false;
    }
    
    public static ODKCollectData getODKCollectData() {
        return odkCollectData;
    }

    /**
     * Saves an OSM Element as XML in ODK Collect.
     * * * 
     * @param el
     * @return The full path of the saved OSM XML File
     */
    public static String saveXmlInODKCollect(OSMElement el) {
        try {
            odkCollectData.consumeOSMElement(el);
            odkCollectData.writeXmlToOdkCollectInstanceDir();
            return odkCollectData.getOSMFileFullPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static LinkedHashMap<String, ODKTag> generateRequiredOSMTagsFromBundle(Bundle extras) {
        List<String> tagKeys = extras.getStringArrayList("TAG_KEYS");
        if (tagKeys == null || tagKeys.size() == 0) {
            return null;
        }
        LinkedHashMap<String, ODKTag> tags = new LinkedHashMap<>();
        for (String key : tagKeys) {
            ODKTag tag = new ODKTag();
            tags.put(key, tag);
            tag.setKey(key);
            String label = extras.getString("TAG_LABEL." + key);
            if (label != null) {
                tag.setLabel(label);
            }
            List<String> values = extras.getStringArrayList("TAG_VALUES." + key);
            if (values != null && values.size() > 0) {
                for (String value : values) {
                    ODKTagItem tagItem = new ODKTagItem();
                    tagItem.setValue(value);
                    String valueLabel = extras.getString("TAG_VALUE_LABEL." + key + "." + value);
                    if (valueLabel != null) {
                        tagItem.setLabel(valueLabel);
                    }
                    tag.addItem(tagItem);
                }
            }
        }
        return tags;
    }
}

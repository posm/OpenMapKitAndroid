package org.redcross.openmapkit;

import com.spatialdev.osm.model.OSMElement;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.tagswipe.TagEdit;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Constraints {

    private static Constraints instance;

    private JSONObject defaultConstraintsJson;
    private JSONObject formConstraintsJson;

    /**
     * We feed tag key, values through this map of maps to find the set
     * of tags with keys that should be hidden. The inner map may have
     * a "" key that is the wildcard of tags to hide.
     */
    private Map<String, Map<String, Set<String>>> hideMap = new HashMap<>();

    private boolean active = true;

    public class TagAction {
        public Set<String> hide = new HashSet<>();
        public Set<String> show = new HashSet<>();
    }

    public static Constraints initialize() {
        instance = new Constraints();
        return instance;
    }

    public static Constraints singleton() {
        return instance;
    }

    /**
     * If there is no default.json constraints file, we have nothing to work with,
     * so the constraint functionality is disabled.
     *
     * @return boolean if the constraint is active
     */
    public boolean isActive() {
        return active;
    }

    public boolean tagIsNumeric(String tagKey) {
        return cascadeBooleanTagConstraint(tagKey, "numeric", false);
    }

    public boolean tagAllowsCustomValue(String tagKey) {
        return cascadeBooleanTagConstraint(tagKey, "custom_value", false);
    }

    public boolean tagShouldBeShown(String tagKey) {

        return true;
    }

    public TagAction tagAddedOrEdited(String key, String val) {
        TagAction tagAction = new TagAction();

        // THIS IS JUST TO THINK THINGS THROUGH.
        // Example of hiding shop if amenity is selected...
        if (key.equals("amenity")) {
            TagEdit.removeTag("shop");
//            TagEdit.removeTag("name");
        }

        return tagAction;
    }

    public TagAction tagDeleted(String key) {
        TagAction tagAction = new TagAction();

        return tagAction;
    }

    private Constraints() {
        loadConstraintsJson();
        buildSkipLogic();
    }

    private void loadConstraintsJson() {
        try {
            File defaultConstraintsFile = ExternalStorage.fetchConstraintsFile("default");
            String defaultConstraintsStr = FileUtils.readFileToString(defaultConstraintsFile);
            defaultConstraintsJson = new JSONObject(defaultConstraintsStr);
        }
        // If there is no default.json constraints file,
        // we just turn off Constraints functionality.
        catch (Exception e) {
            active = false;
        }

        if (!ODKCollectHandler.isODKCollectMode()) return;

        String formFileName = ODKCollectHandler.getODKCollectData().getFormFileName();
        try {
            File formConstraintsFile = ExternalStorage.fetchConstraintsFile(formFileName);
            String formConstraintsStr = FileUtils.readFileToString(formConstraintsFile);
            formConstraintsJson = new JSONObject(formConstraintsStr);
        } catch (Exception e) {
            // do nothing
            // We typically do not need a constraints specific to a given form,
            // so this is normal.
        }
    }

    private boolean cascadeBooleanTagConstraint(String tagKey, String tagConstraint, boolean defaultVal) {
        boolean val = defaultVal;

        try {
            JSONObject tagConstraints = defaultConstraintsJson.getJSONObject(tagKey);
            val = tagConstraints.getBoolean(tagConstraint);
        } catch (JSONException e) {
            // do nothing
        }

        if (formConstraintsJson != null) {
            try {
                JSONObject tagConstraints = formConstraintsJson.getJSONObject(tagKey);
                val = tagConstraints.getBoolean(tagConstraint);
            } catch (JSONException e) {
                // do nothing
            }
        }

        return val;
    }

    private String cascadeStringTagConstraint(String tagKey, String tagConstraint, String defaultVal) {
        String val = defaultVal;

        try {
            JSONObject tagConstraints = defaultConstraintsJson.getJSONObject(tagKey);
            val = tagConstraints.getString(tagConstraint);
        } catch (JSONException e) {
            // do nothing
        }

        if (formConstraintsJson != null) {
            try {
                JSONObject tagConstraints = formConstraintsJson.getJSONObject(tagKey);
                val = tagConstraints.getString(tagConstraint);
            } catch (JSONException e) {
                // do nothing
            }
        }

        return val;
    }

    private void buildSkipLogic() {
        Iterator<String> tagKeys = defaultConstraintsJson.keys();
        // iterate through main tag keys
        while (tagKeys.hasNext()) {
            String tag = tagKeys.next();
            // look for hide_if and show_if constraints
            try {
                JSONObject constraints = defaultConstraintsJson.getJSONObject(tag);
                try {
                    JSONObject hideIf = constraints.getJSONObject("hide_if");
                    Iterator<String> hideIfKeys = hideIf.keys();
                    while (hideIfKeys.hasNext()) {
                        String hideIfKey = hideIfKeys.next();
                        String hideIfVal = hideIf.optString(hideIfKey);
                        Map<String, Set<String>> hideMapVal = hideMap.get(hideIfKey);
                        // check to make sure inner objects are created
                        if (hideMapVal == null) {
                            hideMapVal = new HashMap<>();
                            hideMapVal.put(hideIfVal, new HashSet<String>());
                            hideMap.put(hideIfKey, hideMapVal);
                        } else if (hideMapVal.get(hideIfVal) == null) {
                            hideMapVal.put(hideIfVal, new HashSet<String>());
                        }
                        // Under the right conditions,
                        // this tag in this set should be hidden.
                        hideMapVal.get(hideIfVal).add(tag);
                    }
                } catch (JSONException e) {
                    // do nothing
                }

            } catch (JSONException e) {
                // do nothing
            }

        }
    }

}

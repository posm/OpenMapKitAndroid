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
     * Contains a map of constraint keys with the key value pairs of the
     * OSM Element conditions in which a tag view should be hidden.
     *
     * The key of the outer map is to the tag in question
     */
    private Map<String, Map<String, String>> hideMap = new HashMap<>();

    /**
     * This map is similar, but the key to the outer map is of tag keys
     * that will cause the inner Set of tag keys to be hidden.
     */
    private Map<String, Map<String, Set<String>>> causeHideMap = new HashMap<>();

    /**
     * Contains a map of constraint keys with the key value pairs of the
     * OSM Element conditions in which a tag view should be shown.
     *
     * The key of the outer map is to the tag in question.
     */
    private Map<String, Map<String, String>> showMap = new HashMap<>();

    /**
     * The key of the outer map is to the tag key that causes the inner
     * set of tag keys to be shown.
     */
    private Map<String, Map<String, Set<String>>> causeShowMap = new HashMap<>();

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

    public boolean tagShouldBeShown(String tagKey, OSMElement osmElement) {
        if (!isActive()) return true;

        // Check showMap
        Map<String, String> showMapMap = showMap.get(tagKey);
        if (showMapMap != null) {
            Set<String> showMapMapKeys = showMapMap.keySet();
            for (String key : showMapMapKeys) {
                String val = showMapMap.get(key);
                // wildcard
                if (val.equals("")) {
                    if (osmElement.getTags().keySet().contains(key)) {
                        // OSM Element has a tag key that is required (declared with true)
                        // by the show_if condition for the given tag_key.
                        return true;
                    }
                } else {
                    String osmElementTagVal = osmElement.getTags().get(key);
                    if (osmElementTagVal != null && osmElementTagVal.equals(val)) {
                        // OSM Element has a tag key and value that match the
                        // show_if condition for the given tagKey.
                        return true;
                    }
                }
            }
            // OSM Element has no tags that match the show_if condition for the given tagKey.
            return false;
        }

        // Check hideMap
        Map<String, String> hideMapMap = hideMap.get(tagKey);
        if (hideMapMap != null) {
            Set<String> hideMapMapKeys = hideMapMap.keySet();
            for (String key : hideMapMapKeys) {
                String val = hideMapMap.get(key);
                // wildcard
                if (val.equals("")) {
                    if (osmElement.getTags().keySet().contains(key)) {
                        return false;
                    }
                } else {
                    String osmElementTagVal = osmElement.getTags().get(key);
                    if (osmElementTagVal != null && osmElementTagVal.equals(val)) {
                        return false;
                    }
                }
            }
        }

        // If the tag isn't mentioned in the showMap or the hideMap, then we should show it!
        return true;
    }

    public TagAction tagAddedOrEdited(String key, String val, OSMElement osmElement) {
        TagAction tagAction = new TagAction();
        if (!isActive()) return tagAction;

        Set<String> shownTagKeys = TagEdit.shownTagKeys();
        Set<String> hiddenTagKeys = TagEdit.hiddenTagKeys();

        for (String k : shownTagKeys) {
//            tagShouldBeShown(, osmElement);
        }

        for (String k : hiddenTagKeys) {

        }

        // THIS IS JUST TO THINK THINGS THROUGH.
        // Example of hiding shop if amenity is selected...
        if (key.equals("amenity")) {
            TagEdit.removeTag("shop");
//            TagEdit.removeTag("name");
        }

        return tagAction;
    }

    public TagAction tagDeleted(String key, OSMElement osmElement) {
        TagAction tagAction = new TagAction();
        if (!isActive()) return tagAction;

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
        if (!isActive()) return val;

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
        if (!isActive()) return val;

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
        buildHideMaps(defaultConstraintsJson);
        buildHideMaps(formConstraintsJson);
        buildShowMaps(defaultConstraintsJson);
        buildHideMaps(formConstraintsJson);
    }

    private void buildHideMaps(JSONObject constraintsJson) {
        Iterator<String> tagKeys = constraintsJson.keys();
        // iterate through main tag keys
        while (tagKeys.hasNext()) {
            String tag = tagKeys.next();
            try {
                JSONObject constraints = constraintsJson.getJSONObject(tag);
                // look for hide_if constraints
                JSONObject hideIf = constraints.getJSONObject("hide_if");
                Iterator<String> hideIfKeys = hideIf.keys();
                while (hideIfKeys.hasNext()) {
                    String hideIfKey = hideIfKeys.next();
                    String hideIfVal = hideIf.optString(hideIfKey);
                    Map<String, String> hideMapMap = hideMap.get(hideIfKey);
                    // check to make sure inner objects are created
                    if (hideMapMap == null) {
                        hideMapMap = new HashMap<>();
                        hideMap.put(hideIfKey, hideMapMap);
                    }
                    hideMapMap.put(hideIfKey, hideIfVal);
                }
            } catch (JSONException e) {
                // do nothing
            }
        }
    }

    private void buildShowMaps(JSONObject constraintsJson) {
        Iterator<String> tagKeys = constraintsJson.keys();
        // iterate through main tag keys
        while (tagKeys.hasNext()) {
            String tag = tagKeys.next();
            try {
                JSONObject constraints = constraintsJson.getJSONObject(tag);
                JSONObject showIf = constraints.getJSONObject("show_if");
                Iterator<String> showIfKeys = showIf.keys();
                while (showIfKeys.hasNext()) {
                    String showIfKey = showIfKeys.next();
                    String showIfVal = showIf.optString(showIfKey);
                    Map<String, String> showMapMap = showMap.get(showIfKey);
                    if (showMapMap == null) {
                        showMapMap = new HashMap<>();
                        showMap.put(showIfKey, showMapMap);
                    }
                    showMapMap.put(showIfKey, showIfVal);
                }
            } catch (JSONException e) {
                // do nothing
            }
        }
    }
}

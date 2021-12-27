package org.redcross.openmapkit;

import com.spatialdev.osm.model.OSMElement;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;

import java.io.File;
import java.util.Collection;
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
     * <p>
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
     * <p>
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

    public boolean tagIsRequired(String tagKey) {
        return cascadeBooleanTagConstraint(tagKey, "required", false);
    }

    public boolean tagIsSelectMultiple(String tagKey) {
        return cascadeBooleanTagConstraint(tagKey, "select_multiple", false);
    }

    public Set<String> requiredTagsNotMet(OSMElement osmElement) {
        Set<String> missingTags = new HashSet<>();

        if (ODKCollectHandler.isODKCollectMode()) {
            Map<String, String> tags = osmElement.getTags();
            Collection<ODKTag> odkTags = ODKCollectHandler.getODKCollectData().getRequiredTags();
            for (ODKTag odkTag : odkTags) {
                String odkTagKey = odkTag.getKey();
                if (cascadeBooleanTagConstraint(odkTagKey, "required", false)) {
                    String osmTagVal = tags.get(odkTagKey);
                    if (osmTagVal == null || osmTagVal.length() < 1) {
                        missingTags.add(odkTagKey);
                    }
                }
            }
        }

        return missingTags;
    }

    public String tagDefaultValue(String tagKey) {
        return cascadeStringTagConstraint(tagKey, "default", null);
    }

    public boolean tagShouldBeShown(String tagKey, OSMElement osmElement) {
        if (!isActive()) return true;

        // Check showMap
        Map<String, String> showMapMap = showMap.get(tagKey);
        if (showMapMap != null && showMapMap.size() > 0) {
            Set<String> showMapMapKeys = showMapMap.keySet();
            for (String key : showMapMapKeys) {
                String val = showMapMap.get(key);
                // wildcard
                if (val.equals("true")) {
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
        if (hideMapMap != null && hideMapMap.size() > 0) {
            Set<String> hideMapMapKeys = hideMapMap.keySet();
            for (String key : hideMapMapKeys) {
                String val = hideMapMap.get(key);
                // wildcard
                if (val.equals("true")) {
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

    /**
     * If the tag has an implicit value, it returns it.
     * Otherwise, it returns null.
     * <p>
     * This is how to test if the tag should be considered implicit.
     *
     * @param tagKey
     * @return implicit value or null
     */
    public String implicitVal(String tagKey) {
        return cascadeStringTagConstraint(tagKey, "implicit", null);
    }

    public TagAction tagAddedOrEdited(String key, String val) {
        TagAction tagAction = new TagAction();
        if (!isActive()) return tagAction;

        tagAction.hide = findTagsToBeHiddenFromUpdate(key, val);
        tagAction.show = findTagsToBeShownFromUpdate(key, val);

        return tagAction;
    }

    public TagAction tagDeleted(String key) {
        TagAction tagAction = new TagAction();
        if (!isActive()) return tagAction;

        tagAction.hide = findTagsToBeHiddenFromDelete(key);
        tagAction.show = findTagsToBeShownFromDelete(key);

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
        ExternalStorage.copyFormConstraintsFromOdk(formFileName);
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
        if (defaultConstraintsJson != null) {
            buildHideMaps(defaultConstraintsJson);
            buildShowMaps(defaultConstraintsJson);
        }
        if (formConstraintsJson != null) {
            buildHideMaps(formConstraintsJson);
            buildShowMaps(formConstraintsJson);
        }
    }

    private void buildHideMaps(JSONObject constraintsJson) {
        Iterator<String> tagKeys = constraintsJson.keys();
        // iterate through main tag keys
        while (tagKeys.hasNext()) {
            String tag = tagKeys.next();
            try {
                JSONObject constraints = constraintsJson.getJSONObject(tag);
                Map<String, String> hideMapMap = hideMap.get(tag);
                if (hideMapMap == null) {
                    hideMapMap = new HashMap<>();
                    hideMap.put(tag, hideMapMap);
                }
                JSONObject hideIf = constraints.getJSONObject("hide_if");
                Iterator<String> hideIfKeys = hideIf.keys();
                while (hideIfKeys.hasNext()) {
                    String hideIfKey = hideIfKeys.next();
                    String hideIfVal = hideIf.optString(hideIfKey);
                    Map<String, Set<String>> causeHideMapMap = causeHideMap.get(hideIfKey);
                    if (causeHideMapMap == null) {
                        causeHideMapMap = new HashMap<>();
                        causeHideMapMap.put(hideIfVal, new HashSet<String>());
                        causeHideMap.put(hideIfKey, causeHideMapMap);
                    } else if (causeHideMapMap.get(hideIfVal) == null) {
                        causeHideMapMap.put(hideIfVal, new HashSet<String>());
                    }
                    hideMapMap.put(hideIfKey, hideIfVal);
                    causeHideMapMap.get(hideIfVal).add(tag);
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
                Map<String, String> showMapMap = showMap.get(tag);
                if (showMapMap == null) {
                    showMapMap = new HashMap<>();
                    showMap.put(tag, showMapMap);
                }
                JSONObject showIf = constraints.getJSONObject("show_if");
                Iterator<String> showIfKeys = showIf.keys();
                while (showIfKeys.hasNext()) {
                    String showIfKey = showIfKeys.next();
                    String showIfVal = showIf.optString(showIfKey);
                    Map<String, Set<String>> causeShowMapMap = causeShowMap.get(showIfKey);
                    if (causeShowMapMap == null) {
                        causeShowMapMap = new HashMap<>();
                        causeShowMapMap.put(showIfVal, new HashSet<String>());
                        causeShowMap.put(showIfKey, causeShowMapMap);
                    } else if (causeShowMapMap.get(showIfVal) == null) {
                        causeShowMapMap.put(showIfVal, new HashSet<String>());
                    }
                    showMapMap.put(showIfKey, showIfVal);
                    causeShowMapMap.get(showIfVal).add(tag);
                }
            } catch (JSONException e) {
                // do nothing
            }
        }
    }

    public Set<String> findTagsToBeHiddenFromUpdate(String key, String val) {
        Set<String> tags = new HashSet<>();

        Map<String, Set<String>> causeHideMapMap = causeHideMap.get(key);
        if (causeHideMapMap != null) {
            Set<String> set = causeHideMapMap.get("true");
            if (set != null) {
                tags.addAll(set);
            }
            set = causeHideMap.get(key).get(val);
            if (set != null) {
                tags.addAll(set);
            }
        }

        Map<String, Set<String>> causeShowMapMap = causeShowMap.get(key);
        if (causeShowMapMap != null) {
            Set<String> keys = causeShowMapMap.keySet();
            for (String k : keys) {
                // We should still show the keys that should be shown by
                // the key wildcard and the specific new value selected.
                if (k.equals("true") || k.equals(val)) continue;
                Set<String> tagsToHide = causeShowMapMap.get(k);
                tags.addAll(tagsToHide);
            }
        }
        return tags;
    }

    public Set<String> findTagsToBeShownFromUpdate(String key, String val) {
        Set<String> tags = new HashSet<>();

        Map<String, Set<String>> causeShowMapMap = causeShowMap.get(key);
        if (causeShowMapMap != null) {
            Set<String> set = causeShowMapMap.get("true");
            if (set != null) {
                tags.addAll(set);
            }
            set = causeShowMap.get(key).get(val);
            if (set != null) {
                tags.addAll(set);
            }
        }

        return tags;
    }

    private Set<String> findTagsToBeHiddenFromDelete(String key) {
        Set<String> tags = new HashSet<>();

        Map<String, Set<String>> allTags = causeShowMap.get(key);
        if (allTags != null && allTags.size() > 0) {
            Set<String> vals = allTags.keySet();
            for (String v : vals) {
                tags.addAll(allTags.get(v));
            }
        }

        return tags;
    }

    private Set<String> findTagsToBeShownFromDelete(String key) {
        Set<String> tags = new HashSet<>();

        Map<String, Set<String>> allTags = causeHideMap.get(key);
        if (allTags != null && allTags.size() > 0) {
            Set<String> vals = allTags.keySet();
            for (String v : vals) {
                tags.addAll(allTags.get(v));
            }
        }

        return tags;
    }

}

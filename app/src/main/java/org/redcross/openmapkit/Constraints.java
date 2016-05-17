package org.redcross.openmapkit;

import com.spatialdev.osm.model.OSMElement;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;

import java.io.File;

public class Constraints {

    private static Constraints instance;

    private OSMElement osmElement;
    private JSONObject defaultConstraintsJson;
    private JSONObject formConstraintsJson;

    boolean active = true;

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

    public void setOSMElement(OSMElement osmElement) {
        this.osmElement = osmElement;
    }

    public boolean tagIsNumeric(String tagKey) {
        return cascadeBooleanTagConstraint(tagKey, "numeric", false);
    }

    public boolean tagAllowsCustomValue(String tagKey) {
        return cascadeBooleanTagConstraint(tagKey, "custom_value", false);
    }

    public void tagAddedOrEdited(String key, String val) {

        // THIS IS JUST TO THINK THINGS THROUGH.
        // Example of hiding shop if amenity is selected...
        if (key.equals("amenity")) {

        }

    }

    public void tagDeleted(String key) {

    }

    private Constraints() {
        loadConstraintsJson();
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
}

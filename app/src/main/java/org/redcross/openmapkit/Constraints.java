package org.redcross.openmapkit;

import com.spatialdev.osm.model.OSMElement;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Constraints {

    // We're just keeping these guys static so they only get loaded up once.
    private static JSONObject defaultConstraints;
    private static Map<String, JSONObject> formConstraints = new HashMap<>();

    private OSMElement osmElement;
    private JSONObject activeFormConstraints;

    boolean active = true;

    public Constraints(OSMElement osmElement) {
        this.osmElement = osmElement;

        loadConstraintsJson();
    }

    private void loadConstraintsJson() {
        if (defaultConstraints == null) {
            try {
                File defaultConstraintsFile = ExternalStorage.fetchConstraintsFile("default");
                String defaultConstraintsStr = FileUtils.readFileToString(defaultConstraintsFile);
                defaultConstraints = new JSONObject(defaultConstraintsStr);
            } catch (Exception e) {
                active = false;
            }
        }

        if (!ODKCollectHandler.isODKCollectMode()) return;

        String formId = ODKCollectHandler.getODKCollectData().getFormId();
        activeFormConstraints = formConstraints.get(formId);
        if (activeFormConstraints == null) {
            try {
                File formConstraintsFile = ExternalStorage.fetchConstraintsFile(formId);
                String formConstraintsStr = FileUtils.readFileToString(formConstraintsFile);
                JSONObject json = new JSONObject(formConstraintsStr);
                formConstraints.put(formId, json);
            } catch (Exception e) {
                // do nothing
                // We typically do not need a constraints specific to a given form,
                // so this is normal.
            }
        }
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


}

package org.redcross.openmapkit.odkcollect;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicholas Hallahan on 2/9/15.
 * nhallahan@spatialdev.com
 * * * 
 */
public class ODKCollectHandler {

    // Set to true if we are working with ODK Collect and interacting with intentions.
    private static boolean odkCollectMode = false;
    
    private static Intent intent;
    private static ODKCollectData odkCollectData;
    
    public static void registerIntent(Intent i) {
        intent = i;
        if(intent.getAction().equals("android.intent.action.SEND")) {
            if (intent.getType().equals("text/plain")) {
                Bundle extras = intent.getExtras();
                if(extras != null) {
                    // extract data from intent extras
                    String formId = extras.getString("FORM_ID");
                    String instanceId = extras.getString("INSTANCE_ID");
                    String instanceDir = extras.getString("INSTANCE_DIR");
                    ArrayList<String> requiredTags = extras.getStringArrayList("REQUIRED_TAGS");
                    odkCollectData = new ODKCollectData(formId, instanceId, instanceDir, requiredTags);
                    odkCollectMode = true; // things are good, be in ODK Collect mode
                    return;
                } 
            }
        }
        /**
         * If all of these things are not true, we don't whave what we need from ODK Collect
         * so we should be in the standard, standalone mode for the app.
         * * * 
         */
        odkCollectMode = false; 
    }
    
    public static boolean isOdkCollectMode() {
        return odkCollectMode;
    }
    
    public static boolean isStandaloneMode() {
        return ! odkCollectMode;
    }
    
    public static ODKCollectData getOdkCollectData() throws NoSuchFieldError {
        if (odkCollectData == null) {
            throw new NoSuchFieldError("We have no data from ODK Collect!");
        }
        return odkCollectData;
    }
    
    public static List<String> getRequiredTags() throws NoSuchFieldError {
        if (odkCollectData == null) {
            throw new NoSuchFieldError("We have no data from ODK Collect!");
        }
        return odkCollectData.getRequiredTags();
    }
}

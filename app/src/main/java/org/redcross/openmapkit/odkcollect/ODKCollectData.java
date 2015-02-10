package org.redcross.openmapkit.odkcollect;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 2/9/15.
 * nhallahan@spatialdev.com
 * * * 
 */
public class ODKCollectData {
    
    private String formId;
    private String instanceId;
    private String instanceDir;
    private List<String> requiredTags;
    
    public ODKCollectData ( String formId, 
                            String instanceId, 
                            String instanceDir, 
                            List<String> requiredTags ) {
        this.formId = formId;
        this.instanceId = instanceId;
        this.instanceDir = instanceDir;
        this.requiredTags = requiredTags;        
    }


    public String getFormId() {
        return formId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceDir() {
        return instanceDir;
    }

    public List<String> getRequiredTags() {
        return requiredTags;
    }
}

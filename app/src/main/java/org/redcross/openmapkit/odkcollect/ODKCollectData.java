package org.redcross.openmapkit.odkcollect;

import android.os.Environment;

import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMXmlWriter;

import org.redcross.openmapkit.odkcollect.osmtag.OSMTag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
    private List<OSMTag> requiredTags;
    
    private String editedXml;
    private String osmClassName;
    private long osmId;
    
    public ODKCollectData ( String formId, 
                            String instanceId, 
                            String instanceDir, 
                            List<OSMTag> requiredTags ) {
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

    public List<OSMTag> getRequiredTags() {
        return requiredTags;
    }
    
    public void consumeOSMElement(OSMElement el) throws IOException {
        osmClassName = el.getClass().getSimpleName();
        osmId = el.getId();
        editedXml = OSMXmlWriter.elementToString(el, "theoutpost");
    }
    
    public void writeXmlToOdkCollectInstanceDir() throws IOException {
        if ( ! isODKCollectInstanceDirectoryAvailable() ) {
            throw new IOException("The ODK Collect Instance Directory cannot be accessed!");
        }
        File f = new File( getOSMFileFullPath() );
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        OutputStreamWriter writer = new OutputStreamWriter(fos);
        writer.append(editedXml);
        writer.close();
        fos.close();
    }

    public String getOSMFileName() {
        return osmClassName + osmId + ".osm";
    }
    
    public String getOSMFileFullPath() {
        return instanceDir + "/" + getOSMFileName();
    }
    
    
    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    
    private boolean isODKCollectInstanceDirectoryAvailable() {
        if ( ! isExternalStorageWritable() ) {
            return false;
        }
        File dir = new File(instanceDir);
        if (dir.exists()) {
            return true;
        }
        return false;
    }
    
}

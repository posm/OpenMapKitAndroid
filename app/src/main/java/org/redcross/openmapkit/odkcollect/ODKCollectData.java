package org.redcross.openmapkit.odkcollect;

import android.os.Environment;
import android.util.Log;

import com.spatialdev.osm.OSMUtil;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMXmlWriter;

import org.redcross.openmapkit.ExternalStorage;
import org.redcross.openmapkit.MapActivity;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;
import org.redcross.openmapkit.odkcollect.tag.ODKTagItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Nicholas Hallahan on 2/9/15.
 * nhallahan@spatialdev.com
 * * *
 */
public class ODKCollectData {

    public static final String APP_NAME = "OpenMapKit Android";

    private String formId;
    private String formFileName;
    private String instanceId;
    private String instanceDir;
    private String previousOSMEditFileName;
    private LinkedHashMap<String, ODKTag> requiredTags;
    private List<File> editedOSM = new ArrayList<>();

    private String editedXml;
    private String checksum;
    private String appVersion;

    public ODKCollectData(String formId,
                          String formFileName,
                          String instanceId,
                          String instanceDir,
                          String previousOSMEditFileName,
                          LinkedHashMap<String, ODKTag> requiredTags) {
        this.formId = formId;
        this.formFileName = formFileName;
        this.instanceId = instanceId;
        this.instanceDir = instanceDir;
        this.previousOSMEditFileName = previousOSMEditFileName;
        this.requiredTags = requiredTags;
        this.appVersion = MapActivity.getVersion();
        findEditedOSMForForm(formFileName);
    }

    private void findEditedOSMForForm(String formFileName) {
        if (formFileName == null) {
            return;
        }
        String instances = new File(instanceDir).getParent();
        File[] instancesDirs = new File(instances).listFiles();
        if (instancesDirs != null)
            for (int i = 0; i < instancesDirs.length; ++i) {
                File dir = instancesDirs[i];
                if (!dir.isDirectory()) {
                    continue;
                }
                // check if the instance dir is for the form we are dealing with
                // it is 0 if the form file name is the first substring of the dirname
                if (dir.getName().indexOf(formFileName) != 0) {
                    continue;
                }

                String[] files = dir.list();
                for (int j = 0; j < files.length; ++j) {
                    String fname = files[j];
                    if (fname.lastIndexOf(".osm") > -1) {
                        File osmFile = new File(dir, fname);
                        editedOSM.add(osmFile);
                    }
                }
            }
    }

    public List<File> getEditedOSM() {
        return editedOSM;
    }

    public String getFormId() {
        return formId;
    }

    public String getFormFileName() {
        return formFileName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceDir() {
        return instanceDir;
    }

    public Collection<ODKTag> getRequiredTags() {
        return requiredTags.values();
    }

    /**
     * Returns the ODK defined label for a OSM tag key if exists
     * * *
     *
     * @param key
     * @return
     */
    public String getTagKeyLabel(String key) {
        ODKTag tag = requiredTags.get(key);
        if (tag != null) {
            return tag.getLabel();
        }
        return null;
    }

    /**
     * Returns the ODK defined label for an OSM tag value if exists
     * * *
     *
     * @param key
     * @param value
     * @return
     */
    public String getTagValueLabel(String key, String value) {
        ODKTag tag = requiredTags.get(key);
        if (tag != null) {
            ODKTagItem item = tag.getItem(value);
            return item.getLabel();
        }
        return null;
    }

    public void consumeOSMElement(OSMElement el, String osmUserName) throws IOException {
        checksum = el.checksum();
        editedXml = OSMXmlWriter.elementToString(el, osmUserName, APP_NAME + " " + appVersion);
    }

    public void deleteOldOSMEdit() {
        if (previousOSMEditFileName == null) {
            return;
        }
        String path = instanceDir + '/' + previousOSMEditFileName;
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
    }

    public void writeXmlToOdkCollectInstanceDir() throws IOException {
        if (!isODKCollectInstanceDirectoryAvailable()) {
            throw new IOException("The ODK Collect Instance Directory cannot be accessed!");
        }
        File f = new File(getOSMFileFullPath());
        f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        OutputStreamWriter writer = new OutputStreamWriter(fos);
        writer.append(editedXml);
        writer.close();
        fos.close();
    }

    public String getOSMFileName() {
        return checksum + ".osm";
    }

    public String getOSMFileFullPath() {
        return instanceDir + "/" + getOSMFileName();
    }


    private boolean isODKCollectInstanceDirectoryAvailable() {
        Log.d("apple", "isODKCollectInstanceDirectoryAvailable: "+ExternalStorage.isWritable());
        if (!ExternalStorage.isWritable()) {
            return false;
        }
        File dir = new File(instanceDir);
        if (dir.exists()) {
            return true;
        }
        return false;
    }

}

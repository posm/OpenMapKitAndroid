package org.redcross.openmapkit;

import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.test.ApplicationTestCase;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public static final String TEST_FORM_NAME = "omk_functional_test";
    public static final String TEST_FORM_INSTANCE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/odk/instances/omk_functional_test";
    public ApplicationTest() {
        super(Application.class);
    }

    /**
     * This method creates an intent similar to the one used to launch OpenMapKit from OpenDataKit
     *
     * @return  Intent similar to the one used to launch OpenMapKit from OpenDataKit
     */
    public static Intent getLaunchOMKIntent() {
        File odkInstanceDir = new File(TEST_FORM_INSTANCE_DIR);
        odkInstanceDir.mkdirs();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra("FORM_FILE_NAME", TEST_FORM_NAME);
        intent.putExtra("FORM_ID", "-1");
        intent.putExtra("INSTANCE_ID", "uuid:6004201f-9942-429d-bfa4-e65b683da37b");
        intent.putExtra("INSTANCE_DIR", TEST_FORM_INSTANCE_DIR);
        intent.putExtra("OSM_EDIT_FILE_NAME", TEST_FORM_NAME + ".osm");
        ODKCollectHandler.registerIntent(intent);

        addTagsToIntent(intent);

        return intent;
    }

    private static void addTagsToIntent(Intent intent) {
        String formFileName = ODKCollectHandler.getODKCollectData().getFormFileName();
        try {
            File formConstraintsFile = ExternalStorage.fetchConstraintsFile(formFileName);
            String formConstraintsStr = FileUtils.readFileToString(formConstraintsFile);
            JSONObject formConstraintsJson = new JSONObject(formConstraintsStr);
            ArrayList<String> tagKeys = new ArrayList<>();

            Iterator<String> keyIterator = formConstraintsJson.keys();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                tagKeys.add(key);
                intent.putExtra("TAG_LABEL." + key, key);
                intent.putExtra("TAG_VALUE_LABEL."+key+".yes", "Yes");
                intent.putExtra("TAG_VALUE_LABEL." + key + ".no", "No");
            }
            intent.putExtra("TAG_KEYS", tagKeys);
        } catch (Exception e) {

        }
    }
}
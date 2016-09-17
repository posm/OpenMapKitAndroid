package org.redcross.openmapkit;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.text.InputType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * Created by Jason Rogena - jrogena@ona.io on 8/10/16.
 */
public class ExternalStorageTest {
    Intent launchIntent;
    Context context;
    @Before
    public void init() {
        context = InstrumentationRegistry.getContext();
        launchIntent = ApplicationTest.getLaunchOMKIntent();
        createOdkMediaDirectory();
    }

    /**
     * Creates the odk media directory on the sdcard and adds any file that will be needed in the
     * tests
     */
    private void createOdkMediaDirectory() {
        String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String odkMediaPath = sdCardPath + "/odk/forms/" + ApplicationTest.TEST_FORM_NAME + "-media";
        File mediaDir = new File(odkMediaPath);
        mediaDir.mkdirs();
        AssetManager assetManager = context.getAssets();
        InputStream is = null;
        OutputStream os = null;
        try {
            String odkConstraintsPath = odkMediaPath + "/" + ExternalStorage.CONSTRAINTS_FILE_NAME_ON_ODK;
            is = assetManager.open("constraints/" + ApplicationTest.TEST_FORM_NAME + ".json");
            File odkConstraintsFile = new File(odkConstraintsPath);
            os = new FileOutputStream(odkConstraintsFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1){
                os.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null) is.close();
                if(os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tests whether constraints files are copied over from ODK's media directory for the form to
     * OMK's constraints directory
     */
    @Test
    public void testCopyFormConstraintsFromOdk() {
        String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        //make sure the constraints directory exists
        String omkConstraintsDirPath = sdCardPath + "/" + ExternalStorage.APP_DIR
                + "/" + ExternalStorage.CONSTRAINTS_DIR;
        File omkConstraintsDir = new File(omkConstraintsDirPath);
        omkConstraintsDir.mkdirs();

        //make sure the test form's constraint file is not in the constraints directory
        String omkConstraintsFilePath = omkConstraintsDirPath
                + "/" + ApplicationTest.TEST_FORM_NAME + ".json";
        File omkConstraintsFile = new File(omkConstraintsFilePath);
        if(omkConstraintsFile.exists()) {
            omkConstraintsFile.delete();
        }

        //try copying over the file from ODK
        ExternalStorage.copyFormConstraintsFromOdk(ApplicationTest.TEST_FORM_NAME);

        //check if file was copied
        File file = new File(omkConstraintsFilePath);
        assertTrue(file.exists());

        //check if contents of file are what is expected
        AssetManager assetManager = context.getAssets();
        InputStream is = null;
        try {
            is = assetManager.open("constraints/" + ApplicationTest.TEST_FORM_NAME + ".json");
            String assetString = IOUtils.toString(is);
            String formFileName = ODKCollectHandler.getODKCollectData().getFormFileName();
            File formConstraintsFile = ExternalStorage.fetchConstraintsFile(formFileName);
            String formConstraintsStr = FileUtils.readFileToString(formConstraintsFile);

            assertEquals(assetString, formConstraintsStr);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(e.getMessage(), false);
        } finally {
            try {
                if(is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
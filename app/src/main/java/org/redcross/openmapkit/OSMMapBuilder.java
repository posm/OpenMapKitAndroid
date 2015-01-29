package org.redcross.openmapkit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.spatialdev.osm.model.JTSModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Nicholas Hallahan on 1/28/15.
 * nhallahan@spatialdev.com* 
 */
public class OSMMapBuilder extends AsyncTask<File, Integer, JTSModel> {
    
    private static int remainingFiles = -1;
    public static boolean running = false;
    
    public static void buildMapFromExternalStorage(MapActivity mapActivity) throws IOException {
        if (running) {
            throw new IOException("MAP BUILDER CURRENTLY LOADING!");
        }
        running = true;
        File[] xmlFiles = fetchOsmXmlFiles(mapActivity);
        remainingFiles = xmlFiles.length;
        for (int i = 0; i < xmlFiles.length; i++) {
            File xmlFile = xmlFiles[i];
            OSMMapBuilder builder = new OSMMapBuilder();
            // Reading all of the files in parallel.
            builder.execute(xmlFile);
        }
        
        
    }
    
    private static File[] fetchOsmXmlFiles(Context ctx) {
        String dirPath = Environment.getExternalStorageDirectory() + "/" 
                + ctx.getString(R.string.appFolderName) 
                + "/" + ctx.getString(R.string.osmFolderName) + "/";
        File dir = new File(dirPath);
        return dir.listFiles();
    }

    
    
    
    @Override
    protected JTSModel doInBackground(File... params) {
        File f = params[0];
        try {
            FileInputStream fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        
    }

    @Override
    protected void onPostExecute(JTSModel model) {
        
    }
    
}

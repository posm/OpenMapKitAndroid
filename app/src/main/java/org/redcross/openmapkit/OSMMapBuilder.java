package org.redcross.openmapkit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.spatialdev.osm.model.JTSModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.CountingInputStream;
import com.spatialdev.osm.model.OSMDataSet;
import com.spatialdev.osm.model.OSMXmlParser;

/**
 * Created by Nicholas Hallahan on 1/28/15.
 * nhallahan@spatialdev.com* 
 */
public class OSMMapBuilder extends AsyncTask<File, Long, JTSModel> {
    
    private static int remainingFiles = -1;
    public static boolean running = false;
    
    private CountingInputStream countingInputStream;
    private long fileSize = -1;
    
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
            Log.i("PARSING", "PARSING: " + xmlFile.getName());
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
        fileSize = f.length();
        try {
            InputStream is = new FileInputStream(f);
            countingInputStream = new CountingInputStream(is);
            OSMDataSet ds = OSMXmlParserInOSMMapBuilder.parseFromInputStream(countingInputStream, this);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        long percent = progress[0];
        long elementsRead = progress[1];
        long nodesRead = progress[2];
        long waysRead = progress[3];
        long relationsRead = progress[4];
        Log.i("PARSER_PROGRESS", 
                "percent=" + percent + ", " +
                "elementsRead=" + elementsRead + ", " +
                "nodesRead=" + nodesRead + ", " +
                "waysRead=" + waysRead + ", " +
                "relationsRead=" + relationsRead);
    }

    @Override
    protected void onPostExecute(JTSModel model) {
        
    }
    
    public void updateFromParser(long elementsRead, 
                                 long nodesRead, 
                                 long waysRead, 
                                 long relationsRead) {
        long percent = (long)(((float)countingInputStream.getCount() / (float)fileSize) * 100);
        publishProgress(percent, elementsRead, nodesRead, waysRead, relationsRead);
    }
    
}

package org.redcross.openmapkit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.spatialdev.osm.OSMMap;
import com.spatialdev.osm.model.JTSModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.io.CountingInputStream;
import com.spatialdev.osm.model.OSMDataSet;

/**
 * Created by Nicholas Hallahan on 1/28/15.
 * nhallahan@spatialdev.com* 
 */
public class OSMMapBuilder extends AsyncTask<File, Long, JTSModel> {
    
    private static final float MIN_VECTOR_RENDER_ZOOM = 19;
    
    private static int remainingFiles = -1;
    public static boolean running = false;
    private static MapActivity staticMapActivity; // only supporting one per app for now
    
    private String fileName;
    private CountingInputStream countingInputStream;
    private long fileSize = -1;

    private JTSModel jtsModel = new JTSModel();
    
    public static void buildMapFromExternalStorage(MapActivity mapActivity) throws IOException {
        if (running) {
            throw new IOException("MAP BUILDER CURRENTLY LOADING!");
        }
        running = true;
        staticMapActivity = mapActivity;
        File[] xmlFiles = ExternalStorage.fetchOSMXmlFiles();
        remainingFiles = xmlFiles.length;
        for (int i = 0; i < xmlFiles.length; i++) {
            File xmlFile = xmlFiles[i];
            String fileName = xmlFile.getName();
            OSMMapBuilder builder = new OSMMapBuilder();
            Log.i("BEGIN_PARSING", "PARSING: " + fileName);
//            builder.execute(xmlFile);
            builder.executeOnExecutor(LARGE_STACK_THREAD_POOL_EXECUTOR, xmlFile);
        }
        
        
    }

    @Override
    protected JTSModel doInBackground(File... params) {
        File f = params[0];
        fileName = f.getName();
        fileSize = f.length();
        try {
            InputStream is = new FileInputStream(f);
            countingInputStream = new CountingInputStream(is);
            OSMDataSet ds = OSMXmlParserInOSMMapBuilder.parseFromInputStream(countingInputStream, this);
            jtsModel.addOSMDataSet(ds);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jtsModel;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        long percent = progress[0];
        long elementsRead = progress[1];
        long nodesRead = progress[2];
        long waysRead = progress[3];
        long relationsRead = progress[4];
        Log.i("PARSER_PROGRESS", 
                "fileName=" + fileName + ", " +
                "percent=" + percent + ", " +
                "elementsRead=" + elementsRead + ", " +
                "nodesRead=" + nodesRead + ", " +
                "waysRead=" + waysRead + ", " +
                "relationsRead=" + relationsRead);
    }

    @Override
    protected void onPostExecute(JTSModel model) {
        --remainingFiles;
        // do this when everything is done loading
        if (remainingFiles == 0) {
            new OSMMap(staticMapActivity.getMapView(), jtsModel, staticMapActivity, MIN_VECTOR_RENDER_ZOOM);
            running = false;
        }
    }
    
    public void updateFromParser(long elementReadCount, 
                                 long nodeReadCount, 
                                 long wayReadCount, 
                                 long relationReadCount, 
                                 long tagReadCount) {
        
        long percent = (long)(((float)countingInputStream.getCount() / (float)fileSize) * 100);
        publishProgress(percent, 
                        elementReadCount, 
                        nodeReadCount, 
                        wayReadCount, 
                        relationReadCount, 
                        tagReadCount);
    }

    
    
    
    

    /**
     *  CUSTOM THREAD POOL THAT HAS A LARGER STACK SIZE TO HANDLE LARGER OSM XML FILES
     *  Sometimes the tags parsing recurses deeply... 
     *  http://stackoverflow.com/questions/27277861/increase-asynctask-stack-size
     */

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory yourFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            ThreadGroup group = new ThreadGroup("threadGroup");
            return new Thread(group, r, "YourThreadName", 50000);
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);

    public static final Executor LARGE_STACK_THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, yourFactory);
    
}

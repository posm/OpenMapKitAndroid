package org.redcross.openmapkit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.spatialdev.osm.OSMMap;
import com.spatialdev.osm.model.JTSModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.io.CountingInputStream;
import com.spatialdev.osm.model.OSMDataSet;

import org.redcross.openmapkit.odkcollect.ODKCollectHandler;

/**
 * Created by Nicholas Hallahan on 1/28/15.
 * nhallahan@spatialdev.com* 
 */
public class OSMMapBuilder extends AsyncTask<File, Long, JTSModel> {
    
    private static final float MIN_VECTOR_RENDER_ZOOM = 18;
    private static final String PERSISTED_OSM_FILES = "org.redcross.openmapkit.PERSISTED_OSM_FILES";

    private static MapActivity mapActivity;
    private static SharedPreferences sharedPreferences;
    private static Set<String> persistedOSMFiles = new HashSet<>();
    private static Set<String> loadedOSMFiles = new HashSet<>();
    private static JTSModel jtsModel = new JTSModel();
    private static ProgressDialog progressDialog;

    private static int totalFiles = 0;
    private static int completedFiles = 0;
    private static Set<OSMMapBuilder> activeBuilders = new HashSet<>();
    private static long totalBytesLoaded = 0;
    private static long totalFileSizes = 0;
 
    private String fileName;
    private CountingInputStream countingInputStream;
    private long fileSize = 0;
    private long fileBytesLoaded = 0;
    
    // Should be set to true if we are loading edited OSM XML
    private boolean isOSMEdit = false;

    
    public static void buildMapFromExternalStorage(MapActivity ma) {
        mapActivity = ma;
        sharedPreferences = mapActivity.getPreferences(Context.MODE_PRIVATE);
        persistedOSMFiles = sharedPreferences.getStringSet(PERSISTED_OSM_FILES, loadedOSMFiles);

        // load the previously selected OSM files in OpenMapKit
        for (String absPath : persistedOSMFiles) {
            if (!loadedOSMFiles.contains(absPath)) {
                ++totalFiles;
                File xmlFile = new File(absPath);
                OSMMapBuilder builder = new OSMMapBuilder(false);
                builder.executeOnExecutor(LARGE_STACK_THREAD_POOL_EXECUTOR, xmlFile);
            }
        }

        // load the edited OSM files in ODK Collect
        if (ODKCollectHandler.isODKCollectMode()) {
            List<File> editedOsmFiles = ODKCollectHandler.getODKCollectData().getEditedOSM();
            for (File f : editedOsmFiles) {
                if (!loadedOSMFiles.contains(f.getAbsolutePath())) {
                    ++totalFiles;
                    OSMMapBuilder builder = new OSMMapBuilder(true);
                    builder.executeOnExecutor(LARGE_STACK_THREAD_POOL_EXECUTOR, f);
                }
            }
        }

        if (totalFiles > 0) {
            setupProgressDialog(mapActivity);
        } else {
            OSMMap osmMap = new OSMMap(mapActivity.getMapView(), jtsModel, mapActivity, MIN_VECTOR_RENDER_ZOOM);
            mapActivity.setOSMMap(osmMap);
        }
    }

    /**
     * Returns a boolean array of what files have been loaded
     * * 
     * @param files
     * @return
     */
    public static boolean[] isFileArrayLoaded(File[] files) {
        int len = files.length;
        boolean[] isLoaded = new boolean[len];
        for (int i=0; i < len; ++i) {
            String absPath = files[i].getAbsolutePath();
            isLoaded[i] = loadedOSMFiles.contains(absPath);
        }
        return isLoaded;
    }

    /**
     * Returns a boolean array of what files have been previously
     * selected and persisted to be on the map.
     * * * *
     * @param files
     * @return
     */
    public static boolean[]  isFileArraySelected(File[] files) {
        int len = files.length;
        boolean[] isLoaded = new boolean[len];
        for (int i=0; i < len; ++i) {
            String absPath = files[i].getAbsolutePath();
            isLoaded[i] = persistedOSMFiles.contains(absPath);
        }
        return isLoaded;
    }

    public static void removeOSMFilesFromModel(Set<File> files) {
        if (files.size() < 1) {
            return;
        }
        for (File f : files) {
            String absPath = f.getAbsolutePath();
            if (loadedOSMFiles.contains(absPath)) {
                jtsModel.removeDataSet(absPath);
                loadedOSMFiles.remove(absPath);
                persistedOSMFiles.remove(absPath);
            }
        }
        mapActivity.getMapView().invalidate();
        updateSharedPreferences();
    }
    
    public static void addOSMFilesToModel(Set<File> files) {
        if (files.size() < 1) {
            return;
        }
        for (File f : files) {
            String absPath = f.getAbsolutePath();
            // Don't add something that is either in progress
            // or already on the map.
            if (persistedOSMFiles.contains(absPath)) {
                continue;
            }
            ++totalFiles;
            persistedOSMFiles.add(absPath);
            File xmlFile = new File(absPath);
            OSMMapBuilder builder = new OSMMapBuilder(false);
            builder.executeOnExecutor(LARGE_STACK_THREAD_POOL_EXECUTOR, xmlFile);
        }
        setupProgressDialog(mapActivity);
        mapActivity.getMapView().invalidate();
        updateSharedPreferences();
    }

    /**
     * The provided set of files gets added to the model, and all files
     * currently in the model that are not in the set are removed.
     *
     * @param files - the only files we want on the map
     */
    public static void addOSMFilesToModelExclusively(Set<File> files) {
        Set<String> filePaths = new HashSet<>();
        for (File f : files) {
            filePaths.add(f.getAbsolutePath());
        }
        Set<File> filesToRemove = new HashSet<>();
        for (String lf : loadedOSMFiles) {
            if (!filePaths.contains(lf)) {
                filesToRemove.add(new File(lf));
            }
        }
        removeOSMFilesFromModel(filesToRemove);
        addOSMFilesToModel(files);
    }

    private static void updateSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(PERSISTED_OSM_FILES, persistedOSMFiles);
        editor.apply();
    }
    
    private OSMMapBuilder(boolean isOSMEdit) {
        super();
        this.isOSMEdit = isOSMEdit;
        activeBuilders.add(this);
    }

    protected static void setupProgressDialog(MapActivity mapActivity) {
        progressDialog = new ProgressDialog(mapActivity);
        progressDialog.setTitle("Loading OSM Data");
        progressDialog.setMessage("");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
    }
    
    @Override
    protected JTSModel doInBackground(File... params) {
        File f = params[0];
        fileName = f.getName();
        String absPath = f.getAbsolutePath();
        
        Log.i("BEGIN_PARSING", fileName);
        setFileSize(f.length());
        try {
            InputStream is = new FileInputStream(f);
            countingInputStream = new CountingInputStream(is);
            OSMDataSet ds = OSMXmlParserInOSMMapBuilder.parseFromInputStream(countingInputStream, this);
            if (isOSMEdit) {
                jtsModel.mergeEditedOSMDataSet(absPath, ds);
            } else {
                jtsModel.addOSMDataSet(absPath, ds);
            }
            loadedOSMFiles.add(absPath);
        } catch (Exception e) {
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
        progressDialog.setMessage("Parsing " + (completedFiles + 1) + " of " + totalFiles + " OSM XML Files.");
        progressDialog.setProgress((int)percent);
    }

    @Override
    protected void onPostExecute(JTSModel model) {
        ++completedFiles;
        // do this when everything is done loading
        if (completedFiles == totalFiles) {
            finishAndResetStaticState();
            OSMMap osmMap = new OSMMap(mapActivity.getMapView(), model, mapActivity, MIN_VECTOR_RENDER_ZOOM);
            mapActivity.setOSMMap(osmMap);
        }
    }
    
    private void finishAndResetStaticState() {
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        totalFiles = 0;
        completedFiles = 0;
        activeBuilders = new HashSet<>();
    }
    
    public void updateFromParser(long elementReadCount, 
                                 long nodeReadCount, 
                                 long wayReadCount, 
                                 long relationReadCount, 
                                 long tagReadCount) {
        
        fileBytesLoaded = countingInputStream.getCount();
        computeTotalProgress();
        long percent = (long)(((float)totalBytesLoaded / (float)totalFileSizes) * 100);
        publishProgress(percent,
                        elementReadCount, 
                        nodeReadCount, 
                        wayReadCount, 
                        relationReadCount, 
                        tagReadCount);
    }

    private void setFileSize(long size) {
        fileSize = size;
    }
    
    private long getFileSize() {
        return fileSize;
    }
    
    private long getFileBytesLoaded() {
        return fileBytesLoaded;
    }
    
    
    private static void computeTotalProgress() {
        totalBytesLoaded = 0;
        totalFileSizes = 0;
        for (OSMMapBuilder builder : activeBuilders) {
            long bytesLoaded = builder.getFileBytesLoaded();
            long fileSize = builder.getFileSize();
            totalBytesLoaded += bytesLoaded;
            totalFileSizes += fileSize;
        }
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

    private static final ThreadFactory factory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            ThreadGroup group = new ThreadGroup("OSMMapBuilder_group");
            return new Thread(group, r, "OSMMapBuilder_thread", 50000);
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>();

    public static final Executor LARGE_STACK_THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, factory);
    
}

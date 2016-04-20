package org.redcross.openmapkit.deployments;


import android.app.Activity;
import android.os.AsyncTask;

import com.google.common.io.Files;

import org.json.JSONArray;
import org.json.JSONObject;
import org.redcross.openmapkit.Basemap;
import org.redcross.openmapkit.ExternalStorage;
import org.redcross.openmapkit.OSMMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Deployment {
    private JSONObject json;
    private DeploymentDownloader downloader;

    public static String fileNameFromUrl(String url) {
        int slashIdx = url.lastIndexOf("/");
        return url.substring(slashIdx+1);
    }

    public Deployment(JSONObject deploymentJson) {
        if (deploymentJson != null) {
            json = deploymentJson;
        } else {
            json = new JSONObject();
        }
    }

    public JSONObject json() {
        return json;
    }

    /**
     * Returns the name of the deployment.
     *
     * @return - name
     */
    public String name() {
        return json.optString("name");
    }

    /**
     * Returns the title of the deployment. If the title
     * is not available, return the name instead.
     *
     * @return - title or name
     */
    public String title() {
        String title = null;
        JSONObject manifest = json.optJSONObject("manifest");
        if (manifest != null) {
            title = manifest.optString("title");
        }
        if (title != null && title.length() > 0) {
            return title;
        } else {
            return json.optString("name");
        }
    }

    public int osmCount() {
        JSONObject files = json.optJSONObject("files");
        if (files == null) return 0;
        JSONArray osmFiles = files.optJSONArray("osm");
        if (osmFiles == null) return 0;
        return osmFiles.length();
    }

    public int mbtilesCount() {
        JSONObject files = json.optJSONObject("files");
        if (files == null) return 0;
        JSONArray mbtilesFiles = files.optJSONArray("mbtiles");
        if (mbtilesFiles == null) return 0;
        return mbtilesFiles.length();
    }

    public int geojsonCount() {
        JSONObject files = json.optJSONObject("files");
        if (files == null) return 0;
        JSONArray geojsonFiles = files.optJSONArray("geojson");
        if (geojsonFiles == null) return 0;
        return geojsonFiles.length();
    }

    public int fileCount() {
        return osmCount() + mbtilesCount() + geojsonCount();
    }

    public long totalSize() {
        return json.optLong("totalSize", 0);
    }

    public String totalSizeMB() {
        double totalSize = ((double)totalSize()) / 1000000.0;
        return totalSize + " MB.";
    }

    public JSONArray osm() {
        JSONObject files = json.optJSONObject("files");
        if (files == null) return new JSONArray();
        JSONArray osmFiles = files.optJSONArray("osm");
        if (osmFiles == null) return new JSONArray();
        return osmFiles;
    }

    public JSONArray mbtiles() {
        JSONObject files = json.optJSONObject("files");
        if (files == null) return new JSONArray();
        JSONArray mbtilesFiles = files.optJSONArray("mbtiles");
        if (mbtilesFiles == null) return new JSONArray();
        return mbtilesFiles;
    }

    public JSONArray geojson() {
        JSONObject files = json.optJSONObject("files");
        if (files == null) return new JSONArray();
        JSONArray geojsonFiles = files.optJSONArray("geojson");
        if (geojsonFiles == null) return new JSONArray();
        return geojsonFiles;
    }

    public List<JSONObject> filesToDownload() {
        List<JSONObject> files = new ArrayList<>();
        addJSONArrayToCollection(files, osm());
        addJSONArrayToCollection(files, mbtiles());
        addJSONArrayToCollection(files, geojson());
        return files;
    }

    private void addJSONArrayToCollection(Collection<JSONObject> list, JSONArray arr) {
        int len = arr.length();
        for (int i=0; i < len; ++i) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj != null) {
                list.add(obj);
            }
        }
    }

    public void addToMap() {
        addMBTilesToMap();
        addOSMToMap();
    }

    public void addOSMToMap() {
        Set<File> files = ExternalStorage.deploymentOSMXmlFiles(name());
        OSMMapBuilder.prepareMapToShowOnlyTheseOSM(files);
    }

    public void addMBTilesToMap() {
        List<String> paths = ExternalStorage.deploymentMBTilesFilePaths(name());
        if (paths.size() > 0) {
            String path = paths.get(0);
            Basemap.select(path);
        }
    }

    /**
     * Saves the JSON object to disk.
     */
    public void writeJSONToDisk() {
        String jsonStr = json.toString();
        File deploymentDir = ExternalStorage.deploymentDir(name());
        File f = new File(deploymentDir, "deployment.json");
        try {
            Files.write(jsonStr.getBytes(), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDownloaderListener(DeploymentDownloaderListener listener) {
        if (downloader != null) {
            downloader.addListener(listener);
        }
    }

    public void startDownload(Activity activity) {
        downloader = new DeploymentDownloader(this, activity);
        downloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void cancelDownload() {
        if (downloader != null) {
            downloader.cancel();
        }
    }

    public boolean downloadComplete() {
        List<JSONObject> filesToDownload = filesToDownload();
        Map<String, File> filesDownloaded = ExternalStorage.deploymentDownloadedFiles(name());
        if (filesToDownload.size() != filesDownloaded.size()) {
            return false;
        }
        for (JSONObject o : filesToDownload) {
            String name = o.optString("name");
            if (name == null) return false;
            File f = filesDownloaded.get(name);
            if (f == null) return false;
            long fileSize = f.length();
            long sz = o.optLong("size");
            if (sz != fileSize) return false;
        }
        return true;
    }

}

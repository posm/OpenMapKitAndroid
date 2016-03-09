package org.redcross.openmapkit.deployments;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class Deployment {
    private JSONObject json;

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

    public int fileCount() {
        return osmCount() + mbtilesCount();
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
}

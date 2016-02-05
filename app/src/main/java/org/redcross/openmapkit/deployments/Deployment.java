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

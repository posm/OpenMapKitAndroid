package org.redcross.openmapkit.deployments;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Deployment {
    private JSONObject json;

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
        int osmCount = 0;
        JSONArray files = json.optJSONArray("files");
        for (int i = 0; i < files.length(); i++) {
            String f = files.optString(i);
            if (f.contains(".osm")) {
                ++osmCount;
            }
        }
        return osmCount;
    }

    public int mbtilesCount() {
        int mbtilesCount = 0;
        JSONArray files = json.optJSONArray("files");
        for (int i = 0; i < files.length(); i++) {
            String f = files.optString(i);
            if (f.contains(".mbtiles")) {
                ++mbtilesCount;
            }
        }
        return mbtilesCount;
    }

    public int fileCount() {
        return osmCount() + mbtilesCount();
    }

    public List<String> osmUrls() {
        List<String> urls = new ArrayList<>();
        JSONArray files = json.optJSONArray("files");
        for (int i = 0; i < files.length(); i++) {
            String f = files.optString(i);
            if (f.contains(".osm")) {
                urls.add(f);
            }
        }
        return urls;
    }

    public List<String> mbtilesUrls() {
        List<String> urls = new ArrayList<>();
        JSONArray files = json.optJSONArray("files");
        for (int i = 0; i < files.length(); i++) {
            String f = files.optString(i);
            if (f.contains(".mbtiles")) {
                urls.add(f);
            }
        }
        return urls;
    }
}

package org.fieldpapers.model;

import com.vividsolutions.jts.index.quadtree.Quadtree;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FPAtlas {

    private JSONObject geoJson;
    private String title;

    private Quadtree spatialIndex = new Quadtree();
    private Map<String, FPPage> pages = new HashMap<>();

    public FPAtlas(File fpGeoJSON) throws IOException, JSONException {
        String geoJsonStr = FileUtils.readFileToString(fpGeoJSON, "UTF-8");
        geoJson = new JSONObject(geoJsonStr);
        parsePages();
    }

    private void parsePages() {
        JSONArray features = geoJson.optJSONArray("features");
        if (features == null) return;
        int len = features.length();
        // the 3rd feature is the first page
        for (int i = 2; i < len; ++i) {
            JSONObject o = features.optJSONObject(i);
            if (o != null) {
                FPPage p = new FPPage(o);
                pages.put(p.pageNumber(), p);
                spatialIndex.insert(p.envelope(), p);
            }
        }
    }

    public String title() {
        if (title != null) return title;
        JSONArray features = geoJson.optJSONArray("features");
        if (features == null) return null;
        JSONObject properties = features.optJSONObject(0);
        title = properties.optString("title");
        return title;
    }


}
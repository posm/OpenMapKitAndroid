package org.fieldpapers.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.mapbox.mapboxsdk.views.MapView;
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

    private static final String PREVIOUS_FP_FILE_PATH = "org.redcross.openmapkit.PREVIOUS_FP_FILE_PATH";
    private static File fpGeoJson;
    private static FPAtlas atlas;

    private JSONObject geoJson;
    private String title;

    private Quadtree spatialIndex = new Quadtree();
    private Map<String, FPPage> pages = new HashMap<>();

    public static void load(File fpGeoJSON) throws IOException, JSONException {
        /**
         * Only load if the file specified is a file not currently loaded.
         */
        if (fpGeoJSON.equals(FPAtlas.fpGeoJson)) return;
        FPAtlas.fpGeoJson = fpGeoJSON;
        atlas = new FPAtlas(fpGeoJSON);
    }

    public static void addToMap(Activity activity, MapView mapView) throws IOException, JSONException {
        /**
         * Deal with SharedPreferences. Use it if we haven't explicitly loaded. Set it if we have.
         */
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        if (fpGeoJson == null) {
            String previousFpGeoJsonPath = preferences.getString(PREVIOUS_FP_FILE_PATH, null);
            if (previousFpGeoJsonPath == null) return;
            load(new File(previousFpGeoJsonPath));
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREVIOUS_FP_FILE_PATH, fpGeoJson.getAbsolutePath());
        }


    }

    /**
     * Singleton Constructor
     *
     * @param fpGeoJSON
     * @throws IOException
     * @throws JSONException
     */
    private FPAtlas(File fpGeoJSON) throws IOException, JSONException {
        String geoJsonStr = FileUtils.readFileToString(fpGeoJSON, "UTF-8");
        geoJson = new JSONObject(geoJsonStr);
        parseTitle();
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

    private void parseTitle() {
        try {
            title = geoJson.getJSONArray("features")
                        .getJSONObject(0)
                        .getJSONObject("properties")
                        .getString("title");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String title() {
        return title;
    }

}
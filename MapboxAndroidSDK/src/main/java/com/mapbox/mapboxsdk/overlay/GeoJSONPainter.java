package com.mapbox.mapboxsdk.overlay;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.cocoahero.android.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.mapbox.mapboxsdk.views.MapView;
import java.io.InputStream;
import java.util.ArrayList;

public class GeoJSONPainter {

    private final MapView mapView;
    private final Icon markerIcon;

    public GeoJSONPainter(final MapView mapView, final Icon markerIcon) {
        super();
        this.mapView = mapView;
        this.markerIcon = markerIcon;
    }

    public void loadFromURL(final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        new LoadAndDisplay().execute(url);
    }

    /**
     * Class that generates markers from formats such as GeoJSON
     */
    private class LoadAndDisplay extends AsyncTask<String, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(String... params) {
            InputStream is;
            String jsonText;
            ArrayList<Object> uiObjects = new ArrayList<Object>();

            try {
                FeatureCollection parsed = DataLoadingUtils.loadGeoJSONFromUrl(params[0]);
                uiObjects = DataLoadingUtils.createUIObjectsFromGeoJSONObjects(parsed, markerIcon);
            } catch (Exception e) {
                Log.e(TAG, "Error loading / parsing GeoJSON: " + e.toString());
                e.printStackTrace();
            }
            return uiObjects;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> objects) {
            // Back on the Main Thread so add new UI Objects and refresh map
            for (Object obj : objects) {
                if (obj instanceof Marker) {
                    mapView.addMarker((Marker) obj);
                } else if (obj instanceof PathOverlay) {
                    mapView.getOverlays().add((PathOverlay) obj);
                }
            }
            if (objects.size() > 0) {
                mapView.invalidate();
            }
        }
    }

    static final String TAG = "GeoJSONLayer";
}

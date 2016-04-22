package org.fieldpapers.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.spatialdev.osm.renderer.OSMLine;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.quadtree.Quadtree;

import org.apache.commons.io.FileUtils;
import org.fieldpapers.listeners.FPListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FPAtlas implements MapViewListener, MapListener {

    private static final String PREVIOUS_FP_FILE_PATH = "org.redcross.openmapkit.PREVIOUS_FP_FILE_PATH";
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private static File fpGeoJson;
    private static FPAtlas atlas;

    private JSONObject geoJson;
    private String title;

    private Activity activity;
    private MapView mapView;

    private Quadtree spatialIndex = new Quadtree();
    private Map<String, FPPage> pages = new HashMap<>();

    private PathOverlay selectedPathOverlay;


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
            editor.apply();
        }

        if (atlas == null) return;

        atlas.setActivity(activity);
        atlas.setupMapView(mapView);
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

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setupMapView(MapView mapView) {
        this.mapView = mapView;
        mapView.setMapViewListener(this);
        mapView.addListener(this);
        addPathOverlaysToMapView();
    }

    private void findMapCenterPage() {
        LatLng center = mapView.getCenter();
        Coordinate centerCoord = new Coordinate(center.getLongitude(), center.getLatitude());
        Envelope env = new Envelope(centerCoord);
        List fpPages = spatialIndex.query(env);
        for (Object p : fpPages) {
            FPPage page = (FPPage)p;
            Geometry pageGeom = page.geometry();
            if (pageGeom.contains(GEOMETRY_FACTORY.createPoint(centerCoord))) {
                foundMapCenterPage(page);
            }
        }
    }

    private void foundMapCenterPage(FPPage page) {
        if (activity != null && activity instanceof FPListener) {
            String msg = pageMessage(page);
            ((FPListener)activity).onMapCenterPageChangeMessage(msg);
            setSelectedPathOverlay(page);
        }
    }

    private String pageMessage(FPPage page) {
        return title() + " " + page.pageNumber();
    }

    private void setSelectedPathOverlay(FPPage page) {
        if (selectedPathOverlay != null) {
            selectedPathOverlay.getPaint().setColor(Color.BLACK);
        }
        PathOverlay pathOverlay = page.pathOverlay();
        pathOverlay.getPaint().setARGB(255, OSMLine.DEFAULT_R, OSMLine.DEFAULT_G, OSMLine.DEFAULT_B);
        selectedPathOverlay = pathOverlay;
    }

    private void addPathOverlaysToMapView() {
        List<Overlay> overlays = mapView.getOverlays();
        for (Overlay o : overlays) {
            if (o instanceof PathOverlay) {
                overlays.remove(o);
            }
        }
        Collection<FPPage> pagesCollection = pages.values();
        for (FPPage p : pagesCollection) {
            overlays.add(p.pathOverlay());
        }
        mapView.invalidate();
    }



    /**
     * LISTENERS
     */

    @Override
    public void onScroll(ScrollEvent event) {
        findMapCenterPage();
    }

    @Override
    public void onZoom(ZoomEvent event) {
        findMapCenterPage();
    }

    @Override
    public void onRotate(RotateEvent event) {
        findMapCenterPage();
    }

    @Override
    public void onShowMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onHideMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onTapMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onLongPressMarker(MapView pMapView, Marker pMarker) {

    }

    @Override
    public void onTapMap(MapView pMapView, ILatLng pPosition) {

    }

    @Override
    public void onLongPressMap(MapView pMapView, ILatLng pPosition) {

    }

}
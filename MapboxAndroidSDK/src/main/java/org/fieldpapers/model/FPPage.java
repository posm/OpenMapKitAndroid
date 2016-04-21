package org.fieldpapers.model;

import android.graphics.Path;

import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FPPage {

    private JSONObject feature;
    private Geometry geom;
    private Envelope env;
    private PathOverlay pathOverlay;

    private String pageNumber;
    private String url;

    public FPPage(JSONObject feature) {
        this.feature = feature;
        parsePageNumber();
        buildGeometryAndPathOverlay();
        buildEnvelope();
    }

    private void parsePageNumber() {
        try {
            pageNumber = feature.getJSONObject("properties").getString("page_number");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildGeometryAndPathOverlay() {
        try {
            pathOverlay = new PathOverlay();
            JSONArray coords = feature.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0);
            int len = coords.length();
            Coordinate[] coordinates = new Coordinate[len];
            for (int i = 0; i < len; ++i) {
                JSONArray coord = coords.getJSONArray(i);
                double lng = coord.getDouble(0);
                double lat = coord.getDouble(1);
                coordinates[i] = new Coordinate(lng, lat);
                pathOverlay.addPoint(lat, lng);
            }
            geom = FPAtlas.GEOMETRY_FACTORY.createPolygon(coordinates);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildEnvelope() {
        env = geom.getEnvelopeInternal();
    }

    public String pageNumber() {
        return pageNumber;
    }

    public Geometry geometry() {
        return geom;
    }

    public Envelope envelope() {
        return env;
    }

    public PathOverlay pathOverlay() {
        return pathOverlay;
    }
}

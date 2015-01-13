/**
 * Created by Nicholas Hallahan on 1/3/15.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm;

import android.graphics.Paint;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.spatialdev.osm.model.OSMDataSet;
import com.spatialdev.osm.model.Node;
import com.spatialdev.osm.model.Way;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class OSMUtil {

    public static ArrayList<Object> createUIObjectsFromDataSet(OSMDataSet ds) {
        ArrayList<Object> uiObjects = new ArrayList<>();

        /**
         * POLYGONS
         */
        List<Way> closedWays = ds.getClosedWays();
        for (Way w : closedWays) {
            Iterator<Node> nodeIterator = w.getNodeIterator();
            PathOverlay path = new PathOverlay();
            path.setOptimizePath(false); // optimizePath does not work for polys
            Paint paint = path.getPaint();
            paint.setStyle(Paint.Style.FILL);
            paint.setARGB(85, 95, 237, 140);
            while (nodeIterator.hasNext()) {
                Node n = nodeIterator.next();
                LatLng latLng = n.getLatLng();
                path.addPoint(latLng);
            }
            uiObjects.add(path);
        }


        /**
         * LINES
         */
        List<Way> openWays = ds.getOpenWays();
        for (Way w : openWays) {
            Iterator<Node> nodeIterator = w.getNodeIterator();
            PathOverlay path = new PathOverlay();
            path.getPaint().setARGB(200, 209, 29, 119);
            while (nodeIterator.hasNext()) {
                Node n = nodeIterator.next();
                LatLng latLng = n.getLatLng();
                path.addPoint(latLng);
            }
            uiObjects.add(path);
        }


        /**
         * POINTS
         */
        List<Node> standaloneNodes = ds.getStandaloneNodes();
        for (Node n : standaloneNodes) {
            LatLng latLng = n.getLatLng();
            Marker marker = new Marker("stubTitle", "stubDesc", latLng);
            uiObjects.add(marker);
        }

        return uiObjects;
    }
}

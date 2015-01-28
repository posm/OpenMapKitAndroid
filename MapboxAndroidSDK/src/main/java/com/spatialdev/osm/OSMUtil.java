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
import com.spatialdev.osm.model.OSMNode;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMWay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class OSMUtil {

    public static ArrayList<Object> createUIObjectsFromDataSet(OSMDataSet ds) {
        ArrayList<Object> uiObjects = new ArrayList<>();

        /**
         * POLYGONS
         */
        List<OSMWay> closedWays = ds.getClosedWays();
        for (OSMWay w : closedWays) {
            Iterator<OSMNode> nodeIterator = w.getNodeIterator();
            PathOverlay path = new PathOverlay();
            path.setOptimizePath(false); // optimizePath does not work for polys
            Paint paint = path.getPaint();
            paint.setStyle(Paint.Style.FILL);
            paint.setARGB(85, 95, 237, 140);
            while (nodeIterator.hasNext()) {
                OSMNode n = nodeIterator.next();
                LatLng latLng = n.getLatLng();
                path.addPoint(latLng);
            }
            uiObjects.add(path);
        }


        /**
         * LINES
         */
        List<OSMWay> openWays = ds.getOpenWays();
        for (OSMWay w : openWays) {
            Iterator<OSMNode> nodeIterator = w.getNodeIterator();
            PathOverlay path = new PathOverlay();
            path.getPaint().setARGB(200, 209, 29, 119);
            while (nodeIterator.hasNext()) {
                OSMNode n = nodeIterator.next();
                LatLng latLng = n.getLatLng();
                path.addPoint(latLng);
            }
            uiObjects.add(path);
        }


        /**
         * POINTS
         */
        List<OSMNode> standaloneNodes = ds.getStandaloneNodes();
        for (OSMNode n : standaloneNodes) {
            LatLng latLng = n.getLatLng();
            Marker marker = new Marker(n.getClass().getSimpleName(), printTags(n), latLng);
            uiObjects.add(marker);
        }

        return uiObjects;
    }
    
    public static String printTags(OSMElement element) {
        Map<String, String> tags = element.getTags();
        Set<String> keys = tags.keySet();
        String str = "";
        for (String k : keys) {
            str += k + ": " + tags.get(k) + "\n";
        }
        return str;
    }
}

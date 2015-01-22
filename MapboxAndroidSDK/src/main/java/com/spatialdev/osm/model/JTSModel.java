/**
 * Created by Nicholas Hallahan on 1/7/15.
 * nhallahan@spatialdev.com
 */

package com.spatialdev.osm.model;

import android.util.Log;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.util.ArrayList;
import java.util.List;

public class JTSModel {

    private static final int TAP_PIXEL_TOLERANCE = 24;


    private ArrayList<OSMDataSet> dataSets;
    private GeometryFactory geometryFactory;
    private STRtree rtree;

    public JTSModel(OSMDataSet ds) {
        this();
        addOSMDataSet(ds);
    }

    public JTSModel() {
        geometryFactory = new GeometryFactory();
        rtree = new STRtree();
        dataSets = new ArrayList<>();
    }

    public void addOSMDataSet(OSMDataSet ds) {
        dataSets.add(ds);
        addOSMClosedWays(ds);
        addOSMOpenWays(ds);
        addOSMStandaloneNodes(ds);
    }

    public Envelope createTapEnvelope(ILatLng latLng, float zoom) {
        return createTapEnvelope(latLng.getLatitude(), latLng.getLongitude(), zoom);
    }

    public Envelope createTapEnvelope(double lat, double lng, float zoom) {
        Coordinate coord = new Coordinate(lng, lat);
        return createTapEnvelope(coord, lat, lng, zoom);
    }

    private Envelope createTapEnvelope(Coordinate coord, double lat, double lng, float zoom) {
        Envelope envelope = new Envelope(coord);

        // Creating a reasonably sized envelope around the tap location.
        // Tweak the TAP_PIXEL_TOLERANCE to get a better sized box for your needs.
        double degreesLngPerPixel = degreesLngPerPixel(zoom);
        double deltaX = degreesLngPerPixel * TAP_PIXEL_TOLERANCE;
        double deltaY = scaledLatDeltaForMercator(deltaX, lat);
        envelope.expandBy(deltaX, deltaY);
        return envelope;
    }

    public OSMElement queryFromTap(ILatLng latLng, float zoom) {
        double lat = latLng.getLatitude();
        double lng = latLng.getLongitude();
        Coordinate coord = new Coordinate(lng, lat);
        Envelope envelope = createTapEnvelope(coord, lat, lng, zoom);

        List results = rtree.query(envelope);

        int len = results.size();
        if (len == 0 ) {
           return null;
        }
        if (len == 1) {
            return (OSMElement) results.get(0);
        }

        Point clickPoint = geometryFactory.createPoint(coord);
        OSMElement closestElement = null;
        double closestDist = Double.POSITIVE_INFINITY; // should be replaced in first for loop iteration
        for (Object res : results) {
            OSMElement el = (OSMElement) res;
            if (closestElement == null) {
                closestElement = el;
                closestDist = el.getJTSGeom().distance(clickPoint);
                continue;
            }
            Geometry geom = el.getJTSGeom();
            double dist = geom.distance(clickPoint);

            if (dist > closestDist) {
                continue;
            }

            if (dist < closestDist) {
                closestElement = el;
                closestDist = dist;
                continue;
            }

            // If we are here, then the distances are the same,
            // so we prioritize which element is better based on their type.
            closestElement = prioritizeElementByType(closestElement, el);

        }

        Log.i("queryFromTap closestElement", closestElement.toString());
        return closestElement;
    }

    /**
     * Prioritizes points over lines over polygons.
     * @param el1
     * @param el2
     * @return the priority OSMElement type
     */
    private OSMElement prioritizeElementByType(OSMElement el1, OSMElement el2) {
        if (el1 instanceof Node) {
            return el1;
        }
        if (el2 instanceof Node) {
            return el2;
        }
        // It's gotta be a Way at this point...
        if ( ! ((Way)el1).isClosed() ) {
            return el1;
        }
        return el2;
    }

    private void addOSMClosedWays(OSMDataSet ds) {
        List<Way> closedWays = ds.getClosedWays();
        for (Way closedWay : closedWays) {
            List<Node> nodes = closedWay.getNodes();
            Coordinate[] coords = coordArrayFromNodeList(nodes);
            Polygon poly = geometryFactory.createPolygon(coords);
            closedWay.setJTSGeom(poly);
            Envelope envelope = poly.getEnvelopeInternal();
            rtree.insert(envelope, closedWay);
        }
    }

    private void addOSMOpenWays(OSMDataSet ds) {
        List<Way> openWays = ds.getOpenWays();
        for (Way w : openWays) {
            List<Node> nodes = w.getNodes();
            Coordinate[] coords = coordArrayFromNodeList(nodes);
            LineString line = geometryFactory.createLineString(coords);
            w.setJTSGeom(line);
            Envelope envelope = line.getEnvelopeInternal();
            rtree.insert(envelope, w);
        }
    }

    private Coordinate[] coordArrayFromNodeList(List<Node> nodes) {
        Coordinate[] coords = new Coordinate[nodes.size()];
        int i = 0;
        for (Node node : nodes) {
            double lat = node.getLat();
            double lng = node.getLng();
            Coordinate coord = new Coordinate(lng, lat);
            coords[i++] = coord;
        }
        return coords;
    }

    // NH TODO
    private void addOSMStandaloneNodes(OSMDataSet ds) {
        List<Node> standaloneNodes = ds.getStandaloneNodes();
        for (Node n : standaloneNodes) {

        }
    }


    /**
     * This is how degrees wide a given pixel is for a given zoom.
     *
     * @param zoom
     * @return
     */
    private static double degreesLngPerPixel(float zoom) {
        double degreesPerTile = 360 / Math.pow(2, zoom);
        return degreesPerTile / 256;
    }

    /**
     * This is how many degrees high a given Lat Delta is for a given
     * zoom in Spherical Mercator.
     *
     * http://en.wikipedia.org/wiki/Mercator_projection#Scale_factor
     *
     * @param deltaDeg, lng
     * @return
     */
    private static double scaledLatDeltaForMercator(double deltaDeg, double lat) {
        double scale =  1 / Math.cos(Math.toRadians(lat));
        return deltaDeg / scale;
    }

}

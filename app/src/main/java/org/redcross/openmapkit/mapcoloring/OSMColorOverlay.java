package org.redcross.openmapkit.mapcoloring;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.JTSModel;
import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMNode;
import com.spatialdev.osm.model.OSMWay;
import com.spatialdev.osm.renderer.OSMOverlay;
import com.spatialdev.osm.renderer.OSMPolygon;
import com.vividsolutions.jts.geom.Envelope;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by imwongela on 8/11/15.
 */
public class OSMColorOverlay extends Overlay {
    private static ArrayList<ColorElement> colorElements = new ArrayList<>();
    private static boolean hasColorSettings = true;

    protected Paint paint = new Paint();

    //Hexadecimal radix for color code format.
    private static final int HEX_RADIX = 16;
    private static final int DEFAULT_A = 200;

    private int r;
    private int g;
    private int b;

    // Index lower than points and lines overlays.
    private static final int DEFAULT_OVERLAY_INDEX = 3;

    private JTSModel model;
    private Envelope envelope;

    private float minVectorRenderZoom = 0;
    private float zoom = 0; // current zoom of map

    /**
     * This should only be created by OSMMap.
     * * *
     *
     * @param model
     */
    public OSMColorOverlay(JTSModel model) {
        this.model = model;
        setOverlayIndex(DEFAULT_OVERLAY_INDEX);
    }

    public void updateBoundingBox(BoundingBox bbox) {
        double x1 = bbox.getLonWest();
        double x2 = bbox.getLonEast();
        double y1 = bbox.getLatSouth();
        double y2 = bbox.getLatNorth();
        envelope = new Envelope(x1, x2, y1, y2);
    }

    @Override
    protected void draw(Canvas c, MapView mapView, boolean shadow) {
        // no shadow support & need a bounding box to query rtree & at or above min render zoom
        if (shadow || envelope == null || zoom < minVectorRenderZoom) {
            return;
        }

        List<OSMWay> polys = new ArrayList<>();
        List<OSMElement> viewPortElements = model.queryFromEnvelope(envelope);

        // Select the polygons from the OSMElements
        for (OSMElement el : viewPortElements) {
            if (el instanceof OSMWay) {
                OSMWay w = (OSMWay) el;
                if (w.isClosed()) {
                    polys.add(w);
                }
                continue;
            }
        }

        // Initialize colors
        loadColorElements(mapView);

        if (hasColorSettings) {
            for (OSMWay osmWay : polys) {
                OSMPolygon polygon = (OSMPolygon) osmWay.getOSMPath(mapView);

                //Determine the color to apply on polygon
                Map<String, String> tags = osmWay.getTags();
                String colorCode;
                for (ColorElement el : colorElements) {
                    String key = el.getKey();
                    if (tags.containsKey(key) && tags.get(key).equals(el.getValue())) {
                        //Choose highest priority coloring and exit loop.
                        try {
                            colorCode = el.getColorCode();
                            r = Integer.parseInt(colorCode.substring(1, 3), HEX_RADIX);
                            g = Integer.parseInt(colorCode.substring(3, 5), HEX_RADIX);
                            b = Integer.parseInt(colorCode.substring(5, 7), HEX_RADIX);
                            colorPolygon(polygon, c);
                        } catch (Exception e) {
                            // Suppress color code format exceptions.
                        }
                        break;
                    }
                }
            }
        }
    }

    // Color polygon depending on tag values.
    private void colorPolygon(OSMPolygon polygon, Canvas c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(DEFAULT_A, r, g, b);
        polygon.setPaint(paint);
        polygon.draw(c);
    }

    /**
     * Initialize the colors from xml file for painting the map.
     *
     * @param mv
     */
    private static void loadColorElements(MapView mv) {
        try {
            colorElements = ColorXmlParser.parseXML(mv.getContext());
            if (colorElements.size() == 0) {
                hasColorSettings = false;
            }
        } catch (XmlPullParserException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
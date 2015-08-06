package com.spatialdev.osm.renderer;

import android.graphics.Paint;
import android.graphics.Path;

import com.spatialdev.osm.coloring.ColorElement;
import com.spatialdev.osm.coloring.ColorXmlParser;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.OSMWay;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class OSMPolygon extends OSMPath {

    private static ArrayList<ColorElement> colorElements = new ArrayList<>();
    private static boolean initializedColors = false;

    // OSM LAVENDER
    private static final int DEFAULT_A = 50;
    private static final int DEFAULT_R = 62;
    private static final int DEFAULT_G = 107;
    private static final int DEFAULT_B = 255;
    
    // GOLD
    private static final int DEFAULT_SELECTED_A = 180;
    private static final int DEFAULT_SELECTED_R = 255;
    private static final int DEFAULT_SELECTED_G = 140;
    private static final int DEFAULT_SELECTED_B = 0;

    // MAROON
    private static final int DEFAULT_EDITED_A = 100;
    private static final int DEFAULT_EDITED_R = 245;
    private static final int DEFAULT_EDITED_G = 17;
    private static final int DEFAULT_EDITED_B = 135;

    //Hexadecimal radix for color code format.
    private static final int HEX_RADIX = 16;

    private int a;
    private int r;
    private int g;
    private int b;
    
    /**
     * This should only be constructed by
     * OSMPath.createOSMPath
     * * * *
     * @param w
     */
    protected OSMPolygon(OSMWay w, MapView mv) {
        super(w, mv);

        // color polygon according to if it has been edited before
        if (w.isModified()) {
            this.a = DEFAULT_EDITED_A;
            this.r = DEFAULT_EDITED_R;
            this.g = DEFAULT_EDITED_G;
            this.b = DEFAULT_EDITED_B;
        } else {
            this.a = DEFAULT_A;
            this.r = DEFAULT_R;
            this.g = DEFAULT_G;
            this.b = DEFAULT_B;
        }

        // Color polygon according to values in tags if provided by user.
        Map<String, String> tags = w.getTags();
        loadColorElements(mv);
        String colorCode;
        for (ColorElement el : colorElements) {
            String key = el.getKey();
            if (tags.containsKey(key)) {
                if (tags.get(key).equals(el.getValue())) {
                    //Choose highest priority coloring and exit loop.
                    colorCode = el.getColorCode();
                    a = DEFAULT_SELECTED_A;
                    r = Integer.parseInt(colorCode.substring(1,3), HEX_RADIX);
                    g = Integer.parseInt(colorCode.substring(3, 5), HEX_RADIX);
                    b = Integer.parseInt(colorCode.substring(5,7), HEX_RADIX);
                    break;
                }
            }
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(a, r, g, b);
    }

    @Override
    public void select() {
        paint.setARGB(DEFAULT_SELECTED_A, DEFAULT_SELECTED_R, DEFAULT_SELECTED_G, DEFAULT_SELECTED_B);
    }

    @Override
    public void deselect() {
        paint.setARGB(a, r, g, b);
    }
    


    /**
     * For now, we are drawing all of the polygons, even those outside of the canvas.
     * 
     * This isn't too much of a problem for now, because the Spatial Index will give us
     * only polygons that intersect.
     * 
     * This can be problematic for very large polygons.
     * * * * * * * * 
     * @param path
     * @param projectedPoint
     * @param nextProjectedPoint
     * @param screenPoint
     */
    @Override
    void clipOrDrawPath(Path path, double[] projectedPoint, double[] nextProjectedPoint, double[] screenPoint) {
        if (pathLineToReady) {
            path.lineTo( (float) screenPoint[0], (float) screenPoint[1] );
        } else {
            path.moveTo( (float) screenPoint[0], (float) screenPoint[1] );
            pathLineToReady = true;
        }
    }

    private static void loadColorElements(MapView mv) {
        if (!initializedColors) {
            try {
                colorElements = ColorXmlParser.parseXML(mv.getContext());
                initializedColors = true;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

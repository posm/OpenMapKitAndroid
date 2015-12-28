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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


public class OSMUtil {

    private static String dateFormatStr = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr, Locale.US);

    public static String nowTimestamp() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }


    /**
     * Returns a timestamp without ':'.
     *
     * @return timestamp without ':'
     */
    public static String nowFileTimestamp() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String str = dateFormat.format(new Date());
        return str.replaceAll(":", "");
    }
}

package com.mapbox.mapboxsdk.util;

import android.text.TextUtils;
import com.mapbox.mapboxsdk.constants.MapboxConstants;

public class MapboxUtils implements MapboxConstants {

    public static String qualityExtensionForImageQuality(RasterImageQuality imageQuality) {
        String qualityExtension;
        switch (imageQuality) {
            case MBXRasterImageQualityPNG32:
                qualityExtension = "png32";
                break;
            case MBXRasterImageQualityPNG64:
                qualityExtension = "png64";
                break;
            case MBXRasterImageQualityPNG128:
                qualityExtension = "png128";
                break;
            case MBXRasterImageQualityPNG256:
                qualityExtension = "png256";
                break;
            case MBXRasterImageQualityJPEG70:
                qualityExtension = "jpg70";
                break;
            case MBXRasterImageQualityJPEG80:
                qualityExtension = "jpg80";
                break;
            case MBXRasterImageQualityJPEG90:
                qualityExtension = "jpg90";
                break;
            case MBXRasterImageQualityFull:
            default:
                qualityExtension = "png";
                break;
        }
        return qualityExtension;
    }

    public static String markerIconURL(String size, String symbol, String color) {
        // Make a string which follows the MapBox Core API spec for stand-alone markers. This relies on the MapBox API
        // for error checking.
        //
        StringBuffer marker = new StringBuffer("pin-");

        if (size.toLowerCase(MapboxConstants.MAPBOX_LOCALE).charAt(0) == 'l') {
            marker.append("l"); // large
        } else if (size.toLowerCase(MapboxConstants.MAPBOX_LOCALE).charAt(0) == 's') {
            marker.append("s"); // small
        } else {
            marker.append("m"); // default to medium
        }

        if (!TextUtils.isEmpty(symbol)) {
            marker.append(String.format("-%s+", symbol));
        } else {
            marker.append("+");
        }

        marker.append(color.replaceAll("#", ""));

        // Get hi res version by default
        marker.append("@2x.png");

        // Using API 3 for now
        return String.format(MapboxConstants.MAPBOX_BASE_URL + "/marker/%s", marker);
    }

    public static String getMapTileURL(String mapID, int zoom, int x, int y, RasterImageQuality imageQuality) {
        return String.format(MAPBOX_BASE_URL + "%s/%d/%d/%d%s.%s%s", mapID, zoom, x, y, "@2x", MapboxUtils.qualityExtensionForImageQuality(imageQuality), "");
    }
}

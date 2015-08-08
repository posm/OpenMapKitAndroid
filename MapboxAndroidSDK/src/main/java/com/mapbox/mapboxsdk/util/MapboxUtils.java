package com.mapbox.mapboxsdk.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.constants.MathConstants;
import com.mapbox.mapboxsdk.exceptions.MissingTokenException;

public class MapboxUtils implements MapboxConstants {

    private static final String TAG = "MapboxUtils";

    // Access Token For V4 of API.  If it doesn't exist an exception will be thrown
    private static String accessToken = null;

    private static String versionNumber;

    public static String getAccessToken() {
        if (TextUtils.isEmpty(accessToken)) {
            Log.e(TAG, "Missing Token", new MissingTokenException());
            return null;
        }
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        MapboxUtils.accessToken = accessToken;
    }

    public static String getVersionNumber() {
        return versionNumber;
    }

    public static void setVersionNumber(String versionNumber) {
        MapboxUtils.versionNumber = versionNumber;
    }

    public static String getUserAgent() {
        StringBuffer sb = new StringBuffer("Mapbox Android SDK");

        if (getVersionNumber() != null) {
            sb.append("/");
            sb.append(getVersionNumber());
        }

        return sb.toString();
    }

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

    public static String markerIconURL(Context context, String size, String symbol, String color) {
        // Make a string which follows the Mapbox Core API spec for stand-alone markers. This relies on the Mapbox API
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

//        if (AppUtils.isRunningOn2xOrGreaterScreen(context)) {
//            marker.append("@2x");
//        }
        marker.append(".png");

        marker.append("?access_token=");
        marker.append(MapboxUtils.getAccessToken());
        return String.format(MAPBOX_LOCALE, MapboxConstants.MAPBOX_BASE_URL_V4 + "marker/%s", marker);
    }

    public static String getMapTileURL(Context context, String mapID, int zoom, int x, int y, RasterImageQuality imageQuality) {
        return (new StringBuilder(MAPBOX_BASE_URL_V4)).append(mapID).append('/').append(zoom).append('/').append(x).append('/').append(y).append('.').
                append(MapboxUtils.qualityExtensionForImageQuality(imageQuality)).append("?access_token=").append(MapboxUtils.getAccessToken()).toString();
    }

    /**
     * Build a UTFGrid string for given Coordinate and Zoom Level
     * @param latLng Geo Coordinate
     * @param zoom Zoom Level
     * @return UTFGrid String (z/x/y)
     */
    public static String getUTFGridString(ILatLng latLng, int zoom) {

        int tilesPerSide = Double.valueOf(Math.pow(2.0, zoom)).intValue();
        int x = Double.valueOf(Math.floor(((latLng.getLongitude() + 180.0) / 360.0) * tilesPerSide)).intValue();
        int y = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(latLng.getLatitude() * MathConstants.PI / 180.0) + 1.0 / Math.cos(latLng.getLatitude() * MathConstants.PI / 180.0)) / MathConstants.PI)) / 2.0 * tilesPerSide)).intValue();

        return String.format("%d/%d/%d", zoom, x, y);
    }
}

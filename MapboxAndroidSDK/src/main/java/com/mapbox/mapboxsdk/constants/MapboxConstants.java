package com.mapbox.mapboxsdk.constants;

import java.util.Locale;

/**
 * Storing certain attributes of the Mapbox online
 * service as constants to centralize references.
 */
public interface MapboxConstants {
    /**
     * The default base endpoint of Mapbox services.
     */
    public static final String MAPBOX_BASE_URL_V4 = "https://a.tiles.mapbox.com/v4/";

    public static final String USER_AGENT = "Mapbox Android SDK/0.7.0";

    public static final Locale MAPBOX_LOCALE = Locale.US;

    public enum RasterImageQuality {
        /** Full image quality. */
        MBXRasterImageQualityFull(0),
        /** 32 color indexed PNG. */
        MBXRasterImageQualityPNG32(1),
        /** 64 color indexed PNG. */
        MBXRasterImageQualityPNG64(2),
        /** 128 color indexed PNG. */
        MBXRasterImageQualityPNG128(3),
        /** 256 color indexed PNG. */
        MBXRasterImageQualityPNG256(4),
        /** 70% quality JPEG. */
        MBXRasterImageQualityJPEG70(5),
        /** 80% quality JPEG. */
        MBXRasterImageQualityJPEG80(6),
        /** 90% quality JPEG. */
        MBXRasterImageQualityJPEG90(7);

        private int value;

        private RasterImageQuality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static RasterImageQuality getEnumForValue(int value) {
            switch (value) {
                case 0:
                    return MBXRasterImageQualityFull;
                case 1:
                    return MBXRasterImageQualityPNG32;
                case 2:
                    return MBXRasterImageQualityPNG64;
                case 3:
                    return MBXRasterImageQualityPNG128;
                case 4:
                    return MBXRasterImageQualityPNG256;
                case 5:
                    return MBXRasterImageQualityJPEG70;
                case 6:
                    return MBXRasterImageQualityJPEG80;
                case 7:
                    return MBXRasterImageQualityJPEG90;
                default:
                    return MBXRasterImageQualityFull;
            }
        }
    }
}

package org.redcross.openmapkit.settings;

import android.content.Context;

import org.redcross.openmapkit.ExternalStorage;
import org.redcross.openmapkit.color.ColorElement;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by coder on 8/18/15.
 */
public class SettingsXmlParser {
    public static final String FILENAME = "settings.xml";
    // Color settings parameters
    public static final String MINIMUM_COLOR_ZOOM = "minimum_color_zoom";
    public static final String COLOR = "color";
    public static final String KEY = "tag";
    public static final String VALUE = "value";
    public static final String COLOR_CODE = "color_code";
    public static final String PRIORITY = "priority";

    private static float minVectorRenderZoom = 18;
    private static ArrayList<ColorElement> colorElementList = new ArrayList<>();

    public static XmlPullParser createPullParser(Context ctx) {
        XmlPullParserFactory pullParserFactory;
        try
        {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            final File file = new File(ExternalStorage.getSettingsDir()+FILENAME);
            InputStream in_s = new FileInputStream(file);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            return parser;

        } catch (XmlPullParserException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    public static void parseXML(Context ctx) throws XmlPullParserException, IOException {
        String input;
        //Add the default settings.
        XmlPullParser parser = createPullParser(ctx);
        if (!hasColorXmlFile() || parser == null) {
            return;
        }
        int eventType = parser.getEventType();
        ColorElement colorElement = new ColorElement();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals(KEY)) {
                        input = parser.nextText();
                        colorElement.setKey(input);

                    } else if (name.equals(VALUE)) {
                        input = parser.nextText().trim();
                        colorElement.setValue(input);

                    } else if (name.equals(COLOR_CODE)) {
                        input = parser.nextText().trim();
                        colorElement.setColorCode(input);

                    } else if (name.equals(PRIORITY)) {
                        input = parser.nextText().trim();
                        colorElement.setPriority(Integer.parseInt(input));
                    } else if (name.equals(MINIMUM_COLOR_ZOOM)) {
                        input = parser.nextText().trim();
                        try {
                            minVectorRenderZoom = Float.parseFloat(input);
                        } catch (NumberFormatException ex) {
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equals(COLOR)) {
                        colorElementList.add(colorElement);
                        colorElement = new ColorElement();
                    }
                    break;
            }
            eventType = parser.next();
        }
        //Sort the elements according to priority
        Collections.sort(colorElementList);
    }

    public static float getMinVectorRenderZoom() {
        return minVectorRenderZoom;
    }

    public static ArrayList<ColorElement> getColorElementList() {
        return colorElementList;
    }

    public static boolean hasColorXmlFile() {
        File file = new File(ExternalStorage.getSettingsDir() + FILENAME);
        return file.exists();
    }
}

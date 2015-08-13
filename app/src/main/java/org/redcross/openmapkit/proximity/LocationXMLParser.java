package org.redcross.openmapkit.proximity;

import android.content.Context;
import android.widget.Toast;

import org.redcross.openmapkit.ExternalStorage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by coder on 7/10/15.
 */

public class LocationXMLParser {
    public static final String FILENAME = "proximity_settings.xml";
    public static final String PROXIMITY_CHECK = "proximity_check";
    public static final String PROXIMITY_RADIUS = "proximity_radius";
    private static double radius = 50;
    public static boolean check = false;
    public static boolean proximityEnabled = true;

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
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        Toast.makeText(ctx, "Add the file " + FILENAME + " in the dir " + ExternalStorage.getSettingsDir(), Toast.LENGTH_LONG).show();
        return null;
    }

    public static void parseXML(Context ctx) throws XmlPullParserException, IOException {

        String input;
        //Add the default settings.
        XmlPullParser parser = createPullParser(ctx);
        if (parser == null) {
            return;
        }
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals(PROXIMITY_CHECK)) {
                        input = parser.nextText();
                        if (input.toLowerCase().startsWith("f") || input.toLowerCase().startsWith("n")) {
                            check = false;
                        } else if (input.toLowerCase().startsWith("t") || input.toLowerCase().startsWith("y")) {
                            check = true;
                        }
                    } else if (name.equals(PROXIMITY_RADIUS)) {
                        input = parser.nextText().trim();
                        try {
                            radius = Double.parseDouble(input);
                        } catch (NumberFormatException e) {
                            //e.printStackTrace();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }
    }

    /**
     *
     * @return proximity radius around user location.
     */
    public static double getProximityRadius() {
        return radius;
    }

    /**
     *
     * @return true if GPS must be enabled to show user location.
     */
    public static boolean getProximityCheck() {
        return check;
    }

    /**
     *
     * @param value set whether to apply proximity settings.
     */
    public static void setProximityEnabled(boolean value) {
        proximityEnabled = value;
    }

    /**
     *
     * @return true if proximity settings should be applied.
     */
    public static boolean isProximityEnabled() {
        return proximityEnabled;
    }
}

/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */

package com.spatialdev.osm.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;


public class OSMXmlParser {
    // We are not using namespaces.
    private static final String ns = null;

    private XmlPullParser parser;

    // This is the data set that gets populated from the XML.
    private OSMDataSet ds;

    /**
     * Access the parser through public static methods which function
     * as factories creating parser instances.
     */
    public static OSMDataSet parseFromAssets(final Context context, final String fileName) throws IOException {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("No OSM XML File Name passed in.");
        }
        InputStream in = context.getAssets().open(fileName);
        return parseFromInputStream(in);
    }

    public static OSMDataSet parseFromInputStream(InputStream in) throws IOException {
        OSMXmlParser osmXmlParser = new OSMXmlParser();
        try {
            osmXmlParser.parse(in);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return osmXmlParser.getDataSet();
    }

    private OSMXmlParser() {
        ds = new OSMDataSet();
    }

    public OSMDataSet getDataSet() {
        return ds;
    }

    private void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readOsm();
            ds.postProcessing();
        } finally {
            in.close();
        }
    }

    private void readOsm() throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "osm");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("note")) {
                readNote();
            }
            else if (name.equals("meta")) {
                readMeta();
            }
            else if (name.equals("node")) {
                readNode();
            }
            else if (name.equals("way")) {
                readWay();
            }
            else if (name.equals("relation")) {
                readRelation();
            }
            else {
                skip();
            }
        }
    }

    private void readNote() throws XmlPullParserException, IOException {
        String note = readText();
        ds.createNote(note);
    }

    private void readMeta() throws XmlPullParserException, IOException {
        parser.next();
        String osmBase = parser.getAttributeValue(ns, "osm_base");
        ds.createMeta(osmBase);
        parser.next();
    }

    private void readNode() throws XmlPullParserException, IOException {
        String idStr        = parser.getAttributeValue(ns, "id");
        String latStr       = parser.getAttributeValue(ns, "lat");
        String lonStr       = parser.getAttributeValue(ns, "lon");
        String versionStr   = parser.getAttributeValue(ns, "version");
        String timestampStr = parser.getAttributeValue(ns, "timestamp");
        String changesetStr = parser.getAttributeValue(ns, "changeset");
        String uidStr       = parser.getAttributeValue(ns, "uid");
        String userStr      = parser.getAttributeValue(ns, "user");

        Node node = ds.createNode(  idStr, latStr, lonStr, versionStr, timestampStr,
                                    changesetStr, uidStr, userStr   );

        // If the next thing is not an END_TAG, we have some tag elements in the node...
        if (parser.nextTag() != XmlPullParser.END_TAG && parser.getName().equals("tag")) {
            readTags(node);
        }
    }

    private void readWay() throws XmlPullParserException, IOException {
        String idStr        = parser.getAttributeValue(ns, "id");
        String versionStr   = parser.getAttributeValue(ns, "version");
        String timestampStr = parser.getAttributeValue(ns, "timestamp");
        String changesetStr = parser.getAttributeValue(ns, "changeset");
        String uidStr       = parser.getAttributeValue(ns, "uid");
        String userStr      = parser.getAttributeValue(ns, "user");

        Way way = ds.createWay(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);

        if (parser.nextTag() != XmlPullParser.END_TAG) {
            if (parser.getName().equals("nd")) {
                readNds(way);
            } else if (parser.getName().equals("tag")) {
                readTags(way);
            } else {
                skip();
            }
        }
    }

    private void readRelation() throws XmlPullParserException, IOException {
        String idStr        = parser.getAttributeValue(ns, "id");
        String versionStr   = parser.getAttributeValue(ns, "version");
        String timestampStr = parser.getAttributeValue(ns, "timestamp");
        String changesetStr = parser.getAttributeValue(ns, "changeset");
        String uidStr       = parser.getAttributeValue(ns, "uid");
        String userStr      = parser.getAttributeValue(ns, "user");

        Relation relation = ds.createRelation(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);

        if (parser.nextTag() != XmlPullParser.END_TAG) {
            if (parser.getName().equals("member")) {
                readMembers(relation);
            } else if (parser.getName().equals("tag")) {
                readTags(relation);
            } else {
                skip();
            }
        }
    }

    private void readTags(OSMElement el) throws XmlPullParserException, IOException {
        String k = parser.getAttributeValue(ns, "k");
        String v = parser.getAttributeValue(ns, "v");
        el.addTag(k, v);
        // we do this twice, because these are singular nodes that
        // function as start and end tags
        parser.nextTag();
        parser.nextTag();
        if (parser.getName().equals("tag")){
            readTags(el);
        } else if (parser.getName().equals("nd")) {
            readNds((Way)el);
        } else if (parser.getName().equals("member")) {
            readMembers((Relation)el);
        }
    }

    private void readNds(Way way)  throws XmlPullParserException, IOException {
        String ref = parser.getAttributeValue(ns, "ref");
        long id = Long.valueOf(ref);
        way.addNodeRef(id);
        // we do this twice, because these are singular nodes that
        // function as start and end tags
        parser.nextTag();
        parser.nextTag();
        if (parser.getName().equals("tag")){
            readTags(way);
        } else if (parser.getName().equals("nd")) {
            readNds(way);
        }
    }

    private void readMembers(Relation relation) throws XmlPullParserException, IOException {
        String type = parser.getAttributeValue(ns, "type");
        String ref = parser.getAttributeValue(ns, "ref");

        // TODO: Implement roles.
//        String role = parser.getAttributeValue(ns, "role");

        long id = Long.valueOf(ref);
        if (type.equals("node")) {
            relation.addNodeRef(id);
        } else if (type.equals("way")) {
            relation.addWayRef(id);
        } else if (type.equals("relation")) {
            relation.addRelationRef(id);
        }

        // we do this twice, because these are singular nodes that
        // function as start and end tags
        parser.nextTag();
        parser.nextTag();
        if (parser.getName().equals("tag")){
            readTags(relation);
        } else if (parser.getName().equals("member")) {
            readMembers(relation);
        }

    }


    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip() throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String readText() throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}

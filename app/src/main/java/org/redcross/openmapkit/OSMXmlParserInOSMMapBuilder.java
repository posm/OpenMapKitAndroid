package org.redcross.openmapkit;

import com.spatialdev.osm.model.OSMDataSet;
import com.spatialdev.osm.model.OSMXmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Nicholas Hallahan on 1/29/15.
 *
 * You should use this instead of OSMXmlParser if you want
 * to parse OSM XML within OSMMapBuilder. This class will notify
 * the task of the parsing activity as the data is being parsed.
 * * * * * * 
 */
public class OSMXmlParserInOSMMapBuilder extends OSMXmlParser {

    private OSMMapBuilder osmMapBuilder;

    public static OSMDataSet parseFromInputStream(InputStream in, OSMMapBuilder osmMapBuilder) throws IOException {
        OSMXmlParser osmXmlParser = new OSMXmlParserInOSMMapBuilder(osmMapBuilder);
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

    private OSMXmlParserInOSMMapBuilder(OSMMapBuilder osmMapBuilder) {
        super();
        this.osmMapBuilder = osmMapBuilder;
    }

    @Override
    protected void notifyProgress() {
        osmMapBuilder.updateFromParser(elementReadCount, nodeReadCount, wayReadCount, relationReadCount, tagReadCount);
    }
}
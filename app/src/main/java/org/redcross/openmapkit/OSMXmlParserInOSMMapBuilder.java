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
    
    private long elementsRead = 0;
    private long nodesRead = 0;
    private long waysRead = 0;
    private long relationsRead = 0;

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
    protected void elementRead(String elementName) {
        ++elementsRead;
        if (elementName.equals("node")) {
            ++nodesRead;
        } else if (elementName.equals("way")) {
            ++waysRead;
        } else if (elementName.equals("relation")) {
            ++relationsRead;
        }
        osmMapBuilder.updateFromParser(elementsRead, nodesRead, waysRead, relationsRead);
    }
}
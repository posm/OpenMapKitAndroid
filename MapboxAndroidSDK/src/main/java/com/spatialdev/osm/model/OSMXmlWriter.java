/**
 * Created by Nicholas Hallahan on 1/15/15.
 * nhallahan@spatialdev.com 
 */

package com.spatialdev.osm.model;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;

public class OSMXmlWriter {

    private static final String OSM_API_VERSION = "0.6";
    private static final String GENERATOR = "OpenMapKit 0.1";
    
    // We are not using namespaces.
    private static final String ns = null;

    private LinkedList<OSMElement> elements;
    private XmlSerializer xmlSerializer;
    StringWriter stringWriter;
    
    public static String elementToString(OSMElement element, String osmUser) throws IOException {
        LinkedList<OSMElement> elements = null;
        if (element != null) {
            elements = new LinkedList<>();
            elements.add(element);
        }
        return elementsToString(elements, osmUser);
    }
    
    public static String elementsToString(LinkedList<OSMElement> elements, String osmUser) throws IOException {
        OSMXmlWriter writer = new OSMXmlWriter(elements);
        writer.start(osmUser);
        
        if (elements != null) {
            writer.iterateElements(elements);
        }
        
        return writer.end();
    }
    
    private OSMXmlWriter(LinkedList<OSMElement> elements) {
        this.elements = elements;
        xmlSerializer = Xml.newSerializer();
        stringWriter = new StringWriter();
    }

    private void start(String osmUser) throws IOException {
        xmlSerializer.setOutput(stringWriter);
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.startTag(ns, "osm");
        xmlSerializer.attribute(ns, "version", OSM_API_VERSION);
        xmlSerializer.attribute(ns, "generator", GENERATOR);
        xmlSerializer.attribute(ns, "user", osmUser);
    }
    
    private String end() throws IOException {
        xmlSerializer.endTag(ns, "osm");
        xmlSerializer.endDocument();
        return stringWriter.toString();
    }
    
    private void iterateElements(LinkedList<OSMElement> elements) throws IOException {
        for (OSMElement element : elements) {
            element.xml(xmlSerializer);
        }
    }
}

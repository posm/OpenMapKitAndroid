package com.spatialdev.osm.test;

import android.test.InstrumentationTestCase;

import com.spatialdev.osm.model.OSMXmlWriter;

/**
 * Created by Nicholas Hallahan on 1/16/15.
 * nhallahan@spatialdev.com* 
 */
public class OSMXmlWriterTest extends InstrumentationTestCase {
    
    private static final String USER = "theoutpost";
    private static final String EMPTY_XML = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><osm version=\"0.6\" generator=\"OpenMapKit 0.1\" user=\"theoutpost\" />";

    public void setUp() throws Exception {
        super.setUp();
        
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testElementsToStringAsNull() throws Exception {
        String xml = OSMXmlWriter.elementsToString(null, USER);
        assertEquals(EMPTY_XML, xml);
    }
    
}

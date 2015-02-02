/**
 * Created by Nicholas Hallahan on 1/2/2015.
 * nhallahan@spatialdev.com
 */

package com.spatialdev.osm.model;

import android.test.InstrumentationTestCase;

import com.spatialdev.osm.model.OSMDataSet;
import com.spatialdev.osm.model.OSMWay;
import com.spatialdev.osm.model.OSMXmlParser;

import java.io.InputStream;

public class OSMXmlParserTest extends InstrumentationTestCase {

    private InputStream in;
    private OSMDataSet ds;

    public void setUp() throws Exception {
        super.setUp();
        in = getInstrumentation().getTargetContext().getResources().getAssets().open("test/osm/spatialdev_small.osm");
        ds = OSMXmlParser.parseFromInputStream(in);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    // should be 83 nodes
    public void testNumberOfNodes() throws Exception {
        assertEquals(83, ds.getNodeCount());
    }

    // should be 10 ways
    public void testNumberOfWays() throws Exception {
        assertEquals(10, ds.getWayCount());
    }

    public void testNumberUnlinkedNodes() throws Exception {
        OSMWay w = ds.getWays().get(Long.valueOf(178540022));
        int count = w.getUnlinkedNodesCount();
        assertEquals(0, count);
    }

    public void testNumberLinkedNodes() throws Exception {
        OSMWay w = ds.getWays().get(Long.valueOf(178540022));
        int count = w.getLinkedNodesCount();
        assertEquals(12, count);
    }

    // should be 0 relations
    public void testNumberOfRelations() throws Exception {
        assertEquals(0, ds.getRelationCount());
    }

    public void testNumberStandaloneNodes() throws Exception {
        assertEquals(1, ds.getStandaloneNodesCount());
    }

    public void testNumberClosedWays() throws Exception {
        assertEquals(7, ds.getClosedWaysCount());
    }

    public void testNumberOpenWays() throws Exception {
        assertEquals(3, ds.getOpenWaysCount());
    }

}

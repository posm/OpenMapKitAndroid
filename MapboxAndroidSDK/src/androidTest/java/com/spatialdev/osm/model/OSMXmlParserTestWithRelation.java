package com.spatialdev.osm.model;

import android.test.InstrumentationTestCase;

import com.spatialdev.osm.model.OSMDataSet;
import com.spatialdev.osm.model.OSMRelation;
import com.spatialdev.osm.model.OSMWay;
import com.spatialdev.osm.model.OSMXmlParser;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/9/15.
 * nhallahan@spatialdev.com
 */
public class OSMXmlParserTestWithRelation extends InstrumentationTestCase {

    private OSMDataSet ds;

    public void setUp() throws Exception {
        super.setUp();
        InputStream in = getInstrumentation().getTargetContext().getResources().getAssets().open("test/osm/ballard_relation.osm");
        ds = OSMXmlParser.parseFromInputStream(in);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNumberOfNodes() throws Exception {
        assertEquals(345, ds.getNodeCount());
    }

    public void testNumberOfWays() throws Exception {
        assertEquals(51, ds.getWayCount());
    }

    public void testNumberUnlinkedNodes() throws Exception {
        OSMWay w = ds.getWays().get((long)234714287);
        int count = w.getUnlinkedNodesCount();
        assertEquals(0, count);
    }

    public void testNumberLinkedNodes() throws Exception {
        OSMWay w = ds.getWays().get((long)234714287);
        int count = w.getLinkedNodesCount();
        assertEquals(12, count);
    }

    public void testBurkGilmanWayRelationCount() throws Exception {
        OSMWay w = ds.getWays().get((long)305197030);
        List<OSMRelation> rels = w.getRelations();
        assertEquals(1, rels.size());
    }

    public void testBurkGilmanWayLinkedToRelation() throws Exception {
        OSMWay w = ds.getWays().get((long)305197030);
        List<OSMRelation> rels = w.getRelations();
        OSMRelation r = rels.get(0);
        String relName = r.getTags().get("name");
        assertEquals("Burke-Gilman Trail", relName);
    }

    public void testNumberStandaloneNodes() throws Exception {
        assertEquals(0, ds.getStandaloneNodesCount());
    }

    public void testNumberClosedWays() throws Exception {
        assertEquals(21, ds.getClosedWaysCount());
    }

    public void testNumberOpenWays() throws Exception {
        assertEquals(30, ds.getOpenWaysCount());
    }

    public void testNumberOfRelations() throws Exception {
        assertEquals(5, ds.getRelationCount());
    }

    public void testNumberOfBurkeGilmanRelationUnlinkedMembers() throws Exception {
        OSMRelation r = ds.getRelations().get((long)2183654);
        int unlinkedCount = r.getUnlinkedMemberCount();
        assertEquals(78, unlinkedCount);
    }

    public void testNumberOfBurkeGilmanRelationTags() throws Exception {
        OSMRelation r = ds.getRelations().get((long)2183654);
        int tagCount = r.getTagCount();
        assertEquals(5, tagCount);
    }
}

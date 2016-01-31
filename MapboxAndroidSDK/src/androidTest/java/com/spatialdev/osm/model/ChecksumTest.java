package com.spatialdev.osm.model;


import android.test.InstrumentationTestCase;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;

public class ChecksumTest extends InstrumentationTestCase {

    private InputStream in;
    private OSMDataSet ds;
    private OSMWay way;

    public void setUp() throws Exception {
        super.setUp();
        in = getInstrumentation().getTargetContext().getResources().getAssets().open("test/osm/checksum_way.osm");
        ds = OSMXmlParser.parseFromInputStream(in);
        way = ds.getClosedWays().get(0);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSha1OnAString() throws Exception {
        String testStr = "test";
        String sha1 = new String(Hex.encodeHex(DigestUtils.sha1(testStr)));
        assertEquals("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", sha1);
    }

    public void testWayTagsAsSortedKVString() throws Exception {
        String str = way.tagsAsSortedKVString().toString();
        assertEquals("buildingcommercialbuilding:conditiongoodbuilding:levels1building:materialconcretenameJava the Hut", str);
    }

    public void testWayChecksum() throws Exception {
        String checksum = way.checksum();
        assertEquals("7be9866185c19bfff63029c96226105295b8ccf0", checksum);
    }
}

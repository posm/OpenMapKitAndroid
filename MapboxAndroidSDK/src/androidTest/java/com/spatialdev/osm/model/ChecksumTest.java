package com.spatialdev.osm.model;


import android.test.InstrumentationTestCase;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;
import java.util.List;

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
        assertEquals("add90109a0ca34d12d28292ccd05c588d2220f0a", checksum);
    }

    public void testNodesInWayChecksums() throws Exception {
        List<OSMNode> nodes = way.getNodes();
        for (OSMNode n : nodes) {
            long id = n.getId();
            if (id == 3969314187L) {
                assertEquals("6f3b2e85e05dbdc496f9250931693f7a3e427807", n.checksum());
            } else if (id == 3969314188L) {
                assertEquals("e52eb00f4e028a8010e32a9cfca788273f49a675", n.checksum());
            } else if (id == 3969314189L) {
                assertEquals("3f7514c1b2ca88dfc53fdd7daecc0851bfeba081", n.checksum());
            } else if (id == 3969314190L) {
                assertEquals("c3010e77a9f5d322bfd0081c607dfc7109b86ba9", n.checksum());
            }
        }
    }

    public void testPreWayChecksum() throws Exception {
        String preChecksum = way.preChecksum();
        assertEquals("buildingcommercialbuilding:conditiongoodbuilding:levels1building:materialconcretenameJava the Hut6f3b2e85e05dbdc496f9250931693f7a3e427807c3010e77a9f5d322bfd0081c607dfc7109b86ba93f7514c1b2ca88dfc53fdd7daecc0851bfeba081e52eb00f4e028a8010e32a9cfca788273f49a6756f3b2e85e05dbdc496f9250931693f7a3e427807", preChecksum);
    }
}

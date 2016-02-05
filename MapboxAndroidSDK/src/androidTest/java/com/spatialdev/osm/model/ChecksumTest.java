package com.spatialdev.osm.model;


import android.test.InstrumentationTestCase;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.InputStream;
import java.util.List;

public class ChecksumTest extends InstrumentationTestCase {

    private OSMWay way;
    private OSMWay way2;
    private OSMNode donutHappy;

    public void setUp() throws Exception {
        super.setUp();
        InputStream in = getInstrumentation().getTargetContext().getResources().getAssets().open("test/osm/checksum_way.osm");
        OSMDataSet ds = OSMXmlParser.parseFromInputStream(in);
        way = ds.getClosedWays().get(0);

        InputStream in2 = getInstrumentation().getTargetContext().getResources().getAssets().open("test/osm/checksum_way2.osm");
        OSMDataSet ds2 = OSMXmlParser.parseFromInputStream(in2);
        way2 = ds2.getClosedWays().get(0);

        InputStream in3 = getInstrumentation().getTargetContext().getResources().getAssets().open("test/osm/donut_happy.osm");
        OSMDataSet ds3 = OSMXmlParser.parseFromInputStream(in3);
        donutHappy = ds3.getStandaloneNodes().get(0);
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

    public void testWayChecksum2() throws Exception {
        String checksum = way2.checksum();
        assertEquals("3854296ef0b8fc4810454dd5d8de79dc59f7f007", checksum);
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

    public void testDonutHappyPreChecksum() throws Exception {
        String preChecksum = donutHappy.preChecksum();
        assertEquals("addr:citySacramentoaddr:housenumber5049-Daddr:postcode95841addr:stateCAaddr:streetCollege Oak Dr.amenitycafénameDonut Happyshopbakery38.65838277039187-121.3510830389408", preChecksum);
    }

    public void testDonutHappyChecksum() throws Exception {
        String checksum = donutHappy.checksum();
        assertEquals("27b1bf1412ab7f02f0991e37d783f92d83ed1d52", checksum);
    }

    public void testAccentEigu() throws Exception {
        String str = "café";
        String sha1 = new String(Hex.encodeHex(DigestUtils.sha1(str)));
        assertEquals("f424452a9673918c6f09b0cdd35b20be8e6ae7d7", sha1);
    }
}

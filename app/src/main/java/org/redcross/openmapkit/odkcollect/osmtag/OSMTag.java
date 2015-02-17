package org.redcross.openmapkit.odkcollect.osmtag;

import java.util.ArrayList;
import java.util.List;

/**
 *	Structure for OSM Tag elements in XForm.
 *  Created by Nicholas Hallahan nhallahan@spatialdev.com
 */
public class OSMTag {
    public String key;
    public String label;
    public List<OSMTagItem> items = new ArrayList<OSMTagItem>();
}

package com.spatialdev.osm.renderer;

import com.spatialdev.osm.model.Way;

import java.util.List;

/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */
public class OSMPolygon extends OSMPath {

    /**
     * This should only be constructed by
     * OSMPath.createOSMPathFromOSMElement
     * * * *
     * @param w
     */
    protected OSMPolygon(Way w) {
        super(w);
    }
}

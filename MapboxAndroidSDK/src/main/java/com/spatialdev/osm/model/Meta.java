/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

public class Meta {
    private String osmBase = null;

    public Meta(String osmBase) {
        this.osmBase = osmBase;
    }

    public String getOsmBase() {
        return this.osmBase;
    }
}

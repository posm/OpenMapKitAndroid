/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

public class OSMMeta {
    private String osmBase = null;

    public OSMMeta(String osmBase) {
        this.osmBase = osmBase;
    }

    public String getOsmBase() {
        return this.osmBase;
    }
}

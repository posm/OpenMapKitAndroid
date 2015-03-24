package org.redcross.openmapkit;

import android.os.AsyncTask;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.spatialdev.osm.model.OSMDataSet;

/**
 * Created by Nicholas Hallahan on 3/24/15.
 * nhallahan@spatialdev.com
 * * * 
 */
public class OSMDownloader extends AsyncTask<String, Long, String> {

    private String query = "(way[building]({{bbox}});way[highway]({{bbox}}););out meta;>;out meta qt;";
    double latSouth, lngWest, latNorth, lngEast;
    String fileName = "dl.osm";    
    
    public OSMDownloader(BoundingBox bbox) {
        latSouth = bbox.getLatSouth();
        lngWest = bbox.getLonWest();
        latNorth = bbox.getLatNorth();
        lngEast = bbox.getLonEast();
    }

    /**
     * * 
     * @param bbox      the bounding box the map is at
     * @param query     the OverpassQL Query
     * @param fileName  the name of the file to be written to disk
     */
    public OSMDownloader(BoundingBox bbox, String query, String fileName) {
        this(bbox);
        this.query = query;
        this.fileName = fileName;
    }
    
    public void start() {

    }
    
    @Override
    protected String doInBackground(String... strings) {
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        
    }

    @Override
    protected void onPostExecute(String absPath) {
        
    }
}

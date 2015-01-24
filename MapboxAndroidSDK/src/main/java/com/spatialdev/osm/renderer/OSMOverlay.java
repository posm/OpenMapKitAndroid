/**
 * Created by Nicholas Hallahan on 1/22/15.
 * nhallahan@spatialdev.com 
 */

package com.spatialdev.osm.renderer;

import android.graphics.Canvas;

import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.model.JTSModel;

public class OSMOverlay extends Overlay {

    private JTSModel model;
    
    public OSMOverlay(JTSModel model) {
        this.model = model;
        
    }
    
    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        
    }
    
}

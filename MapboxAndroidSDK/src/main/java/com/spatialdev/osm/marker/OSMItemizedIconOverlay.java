package com.spatialdev.osm.marker;

import android.content.Context;

import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;

import java.util.List;

/**
 * OSMItemizedIconOverlay is an extension of ItemizedIconOverlay.
 * Instead of having an internal list of markers as we do in
 * standard IconOverlays, we have a reference to the varying list
 * of markers set from a QuadTree in OSMOverlay#draw.
 */
public class OSMItemizedIconOverlay extends ItemizedIconOverlay {
    public OSMItemizedIconOverlay(Context pContext, List<Marker> pList, OnItemGestureListener<Marker> pOnItemGestureListener) {
        super(pContext, pList, pOnItemGestureListener);
    }

//    @Override
//    protected void populate() {
//        // no op
//    }
}

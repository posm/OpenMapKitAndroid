package com.spatialdev.osm.marker;

import android.content.Context;

import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.spatialdev.osm.model.OSMNode;
import com.spatialdev.osm.renderer.OSMOverlay;

import java.util.List;

/**
 * OSMItemizedIconOverlay is an extension of ItemizedIconOverlay.
 * Instead of having an internal list of markers as we do in
 * standard IconOverlays, we have a reference to the varying list
 * of markers set from a QuadTree in OSMOverlay#draw.
 */
public class OSMItemizedIconOverlay extends ItemizedIconOverlay {

    List<OSMNode> viewPortNodes;

    public OSMItemizedIconOverlay(OSMOverlay osmOverlay, Context pContext, List<Marker> pList, OnItemGestureListener<Marker> pOnItemGestureListener) {
        super(pContext, pList, pOnItemGestureListener);
        viewPortNodes = osmOverlay.getViewPortNodes();
    }

//    @Override
//    protected void populate() {
//        // no op
//    }
//
//    /**
//     * Returns the Item at the given index.
//     *
//     * @param position the position of the item to return
//     * @return the Item of the given index.
//     */
//    @Override
//    public Marker getItem(final int position) {
//        return viewPortNodes.get(position).getMarker();
//    }
//
//    @Override
//    public int size() {
//        return viewPortNodes.size();
//    }
}

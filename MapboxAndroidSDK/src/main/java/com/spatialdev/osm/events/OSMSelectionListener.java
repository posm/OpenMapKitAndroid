package com.spatialdev.osm.events;

import com.spatialdev.osm.model.OSMElement;

import java.util.LinkedList;

/**
 * Created by Nicholas Hallahan on 1/14/15.
 */
public interface OSMSelectionListener {

    public void selectedElementsChanged(LinkedList<OSMElement> selectedElements);

}

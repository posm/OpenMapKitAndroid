/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import android.graphics.Paint;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.PathOverlay;

import org.xmlpull.v1.XmlSerializer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Way extends OSMElement {

    /**
     * As the XML document is being parsed, ways have references to nodes' IDs.
     * The node itself may not yet be parsed, so we create a list of Node IDs
     * as we parse and will then do postprocessing to create that association.
     */
    private LinkedList<Long> nodeRefs = new LinkedList<>();

    private LinkedList<Node> linkedNodes = new LinkedList<>();

    /**
     * If a way is in a relation, it's relation is added to this list.
     */
    private LinkedList<Relation> linkedRelations = new LinkedList<>();

    /**
     * isClosed checks to see if this way is closed and sets it to true if so.
     */
    private boolean closed = false;

    public Way( String idStr,
                String versionStr,
                String timestampStr,
                String changesetStr,
                String uidStr,
                String userStr ) {

        super(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);
    }

    @Override
    public void xml(XmlSerializer xmlSerializer) {

    }

    public void addNodeRef(long id) {
        nodeRefs.add(id);
    }

    /**
     * Populates linked list of nodes referred to by this way.
     *
     * Takes nodes from nodes hash and puts them in the wayNodes hash
     * for nodes that are in the actual way.
     *
     * @param nodes
     * @return the number of node references NOT linked.
     */
    int linkNodes(Map<Long, Node> nodes, Set<Long> wayNodes) {
        // first check if the way is closed before doing this processing...
        checkIfClosed();
        LinkedList<Long> unlinkedRefs = new LinkedList<>();
        while (nodeRefs.size() > 0) {
            Long refId = nodeRefs.pop();
            Node node = nodes.get(refId);
            wayNodes.add(refId);
            if (node == null) {
                unlinkedRefs.push(refId);
            } else {
                linkedNodes.push(node);
            }
        }
        nodeRefs = unlinkedRefs;
        return nodeRefs.size();
    }

    public int getUnlinkedNodesCount() {
        return nodeRefs.size();
    }

    public int getLinkedNodesCount() {
        return linkedNodes.size();
    }

    private void checkIfClosed() {
        Long firstId = nodeRefs.getFirst();
        Long lastId = nodeRefs.getLast();
        if (firstId.equals(lastId)) {
            closed = true;
        }
    }

    /**
     * If the starting node is the same as ending node, this way
     * is closed.
     *
     * WARNING: This will be correct only AFTER linkNodes has been run.
     *
     * @return closed
     */
    boolean isClosed() {
        if (closed) {
            return true;
        }
        return false;
    }

    /**
     * This allows you to iterate through the nodes. This is great if you
     * want to give a renderer all of the lat longs to paint a line...
     */
    public Iterator<Node> getNodeIterator() {
        return linkedNodes.listIterator();
    }

    public List<Node> getNodes() {
        return linkedNodes;
    }

    /**
     * If this is in a relation, it's parent relation is added to an internal linked list.
     * @param relation
     */
    public void addRelation(Relation relation) {
        linkedRelations.push(relation);
    }

    public List<Relation> getRelations() {
        return linkedRelations;
    }




    @Override
    public Object getOverlay() {
        // if there is no overlay, make it for this element
        if (overlay == null) {
            overlay = createPathOverlay();
        }
        return overlay;
    }

    /**
     * Creating a PathOverlay for this element.
     *
     * @return a PathOverlay that represents just this element.
     */
    protected PathOverlay createPathOverlay() {
        PathOverlay path = new PathOverlay();

        /**
         * POLYGON
         */
        if (closed) {
            path.setOptimizePath(false); // optimizePath does not work for polys
            Paint paint = path.getPaint();
            paint.setStyle(Paint.Style.FILL);
            if (selected) {
                paint.setARGB(110, 255, 140, 0);
            } else {
                paint.setARGB(85, 95, 237, 140);
            }

        }

        /**
         * LINE
         */
        else {
            Paint paint = path.getPaint();
            paint.setARGB(200, 209, 29, 119);
            if (selected) {
                paint.setARGB(110, 255, 140, 0);
                paint.setStrokeWidth(4);
            } else {
                paint.setARGB(85, 95, 237, 140);
                paint.setStrokeWidth(2);
            }
        }

        /**
         * ADD POINTS TO PATH
         */
        Iterator<Node> nodeIterator = getNodeIterator();
        while (nodeIterator.hasNext()) {
            Node n = nodeIterator.next();
            LatLng latLng = n.getLatLng();
            path.addPoint(latLng);
        }

        return path;
    }

    @Override
    public void select() {
        super.select();
        if (overlay != null && overlay instanceof PathOverlay) {
            PathOverlay path = (PathOverlay) overlay;
            Paint paint = path.getPaint();
            paint.setARGB(110, 255, 140, 0);
            if (!closed) {
                paint.setStrokeWidth(4);
            }
        }
    }

    @Override
    public void deselect() {
        super.deselect();
        if (overlay != null && overlay instanceof PathOverlay) {
            PathOverlay path = (PathOverlay) overlay;
            Paint paint = path.getPaint();
            if (closed) {
                paint.setARGB(85, 95, 237, 140);
            } else {
                paint.setARGB(200, 209, 29, 119);
                paint.setStrokeWidth(2);
            }
        }
    }

}

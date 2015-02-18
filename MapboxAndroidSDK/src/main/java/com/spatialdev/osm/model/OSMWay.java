/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.renderer.OSMPath;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OSMWay extends OSMElement {

    /**
     * As the XML document is being parsed, ways have references to nodes' IDs.
     * The node itself may not yet be parsed, so we create a list of Node IDs
     * as we parse and will then do postprocessing to create that association.
     */
    private LinkedList<Long> nodeRefs = new LinkedList<>();

    private LinkedList<OSMNode> linkedNodes = new LinkedList<>();

    /**
     * If a way is in a relation, it's relation is added to this list.
     */
    private LinkedList<OSMRelation> linkedRelations = new LinkedList<>();

    /**
     * isClosed checks to see if this way is closed and sets it to true if so.
     */
    private boolean closed = false;

    
    public OSMWay(String idStr,
                  String versionStr,
                  String timestampStr,
                  String changesetStr,
                  String uidStr,
                  String userStr) {

        super(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);
    }

    @Override
    void xml(XmlSerializer xmlSerializer) throws IOException {
        for (OSMNode node : linkedNodes) {
            node.xml(xmlSerializer);
        }
        xmlSerializer.startTag(null, "way");
        setOsmElementXmlAttributes(xmlSerializer);
        // generate nds
        setWayXmlNds(xmlSerializer);
        // generate tags
        super.xml(xmlSerializer); 
        xmlSerializer.endTag(null, "way");
        for (OSMRelation relation : linkedRelations) {
            relation.xml(xmlSerializer);
        }
    }

    private void setWayXmlNds(XmlSerializer xmlSerializer) throws IOException {
        for (OSMNode node : linkedNodes) {
            xmlSerializer.startTag(null, "nd");
            xmlSerializer.attribute(null, "ref", String.valueOf(node.getId()));
            xmlSerializer.endTag(null, "nd");
        }
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
    int linkNodes(Map<Long, OSMNode> nodes, Set<Long> wayNodes) {
        // first check if the way is closed before doing this processing...
        checkIfClosed();
        LinkedList<Long> unlinkedRefs = new LinkedList<>();
        while (nodeRefs.size() > 0) {
            Long refId = nodeRefs.pop();
            OSMNode node = nodes.get(refId);
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
    public boolean isClosed() {
        if (closed) {
            return true;
        }
        return false;
    }

    /**
     * This allows you to iterate through the nodes. This is great if you
     * want to give a renderer all of the lat longs to paint a line...
     */
    public Iterator<OSMNode> getNodeIterator() {
        return linkedNodes.listIterator();
    }

    public List<OSMNode> getNodes() {
        return linkedNodes;
    }

    /**
     * If this is in a relation, it's parent relation is added to an internal linked list.
     * @param relation
     */
    public void addRelation(OSMRelation relation) {
        linkedRelations.push(relation);
    }

    public List<OSMRelation> getRelations() {
        return linkedRelations;
    }


    
    public OSMPath getOSMPath(MapView mv) {
        // if there is no overlay, make it for this element
        if (osmPath == null) {
            osmPath = OSMPath.createOSMPath(this, mv);
        }
        return osmPath;
    }

    @Override
    public void select() {
        super.select();
        if (osmPath != null) {
            osmPath.select();
        }
    }

    @Override
    public void deselect() {
        super.deselect();
        if (osmPath != null) {
            osmPath.deselect();
        }
    }

}

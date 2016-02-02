/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.renderer.OSMPath;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OSMWay extends OSMElement {

    /**
     * Used to keep track of the IDs of modified ways. This can be used
     * to check to see if we are already using a modified way.
     * * * *
     */
    private static Set<Long> modifiedWayIdSet = new HashSet<>();
    
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

    public static boolean containsModifiedWay(long wayId) {
        return modifiedWayIdSet.contains(wayId);
    }
    
    public OSMWay(String idStr,
                  String versionStr,
                  String timestampStr,
                  String changesetStr,
                  String uidStr,
                  String userStr,
                  String action) {

        super(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr, action);
    }

    @Override
    public String checksum() {
        String str = preChecksum();
        return new String(Hex.encodeHex(DigestUtils.sha1(str)));
    }

    public String preChecksum() {
        StringBuilder str = tagsAsSortedKVString();
        for (OSMNode n : linkedNodes) {
            str.append(n.checksum());
        }
        return str.toString();
    }

    @Override
    void xml(XmlSerializer xmlSerializer, String omkOsmUser) throws IOException {
        for (OSMNode node : linkedNodes) {
            node.xml(xmlSerializer, omkOsmUser);
        }
        xmlSerializer.startTag(null, "way");
        setOsmElementXmlAttributes(xmlSerializer, omkOsmUser);
        // generate nds
        setWayXmlNds(xmlSerializer);
        // generate tags
        super.xml(xmlSerializer, omkOsmUser);
        xmlSerializer.endTag(null, "way");
        for (OSMRelation relation : linkedRelations) {
            relation.xml(xmlSerializer, omkOsmUser);
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
        for (Long refId : nodeRefs) {
            OSMNode node = nodes.get(refId);
            wayNodes.add(refId);
            if (node == null) {
                unlinkedRefs.add(refId);
            } else {
                linkedNodes.add(node);
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
        // Sometimes the app exists or gets a new MapView, and we need to make
        // sure things get drawn on the actual active map view currently on the screen.
        else {
            osmPath.setMapView(mv);
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

    @Override
    protected void setAsModified() {
        super.setAsModified();
        modifiedWayIdSet.add(id);
    }
}

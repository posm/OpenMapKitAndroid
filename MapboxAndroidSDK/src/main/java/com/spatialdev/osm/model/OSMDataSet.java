/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class OSMDataSet {

    /**
     * A list of the notes. There is no ID, so we don't need a Hash.
     */
    private ArrayList<String> notes = new ArrayList<>();

    /**
     * We assume there will only be one meta tag.
     */
    private Meta meta;

    /**
     * Hash tables to look up Nodes, Ways, Relations by their IDs.
     */
    private LinkedHashMap<Long, Node>     nodes     = new LinkedHashMap<>();
    private LinkedHashMap<Long, Way>      ways      = new LinkedHashMap<>();
    private LinkedHashMap<Long, Relation> relations = new LinkedHashMap<>();

    /**
     * Gets filled with ids of nodes that are in a way. This is
     * used to construct standaloneNodes in postProcessing.
     */
    private Set<Long> wayNodeIds = new HashSet<>();

    /**
     * When the post-processing is done, the nodes that are not
     * in a way are put here.
     */
    private List<Node> standaloneNodes = new ArrayList<>();

    /**
     * Post-processing find all of the ways that are closed,
     *      ie: same first and last node
     */
    private List<Way> closedWays = new ArrayList<>();

    /**
     * If its not a closed way, then it is an open way.
     */
    private List<Way> openWays = new ArrayList<>();


    public OSMDataSet() {}

    public void createNote(String note) {
        notes.add(note);
    }

    public void createMeta(String osmBase) {
        meta = new Meta(osmBase);
    }

    public Node createNode(String idStr,
                           String latStr,
                           String lonStr,
                           String versionStr,
                           String timestampStr,
                           String changesetStr,
                           String uidStr,
                           String userStr) {

        Node n = new Node(  idStr, latStr, lonStr, versionStr, timestampStr,
                            changesetStr, uidStr, userStr );

        nodes.put(n.getId(), n);
        return n;
    }

    public Way createWay( String idStr,
                          String versionStr,
                          String timestampStr,
                          String changesetStr,
                          String uidStr,
                          String userStr ) {

        Way w = new Way(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);
        ways.put(w.getId(), w);
        return w;
    }

    public Relation createRelation( String idStr,
                                    String versionStr,
                                    String timestampStr,
                                    String changesetStr,
                                    String uidStr,
                                    String userStr ) {

        Relation r = new Relation(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);
        relations.put(r.getId(), r);
        return r;
    }

    /**
     * Should only be called by the parser.
     */
    void postProcessing() {

        Set<Long> wayKeys = ways.keySet();
        for (Long key : wayKeys) {
            /**
             * Link node references to the actual nodes
             * in the Way objects.
             */
            Way w = ways.get(key);
            w.linkNodes(nodes, wayNodeIds);

            /**
             * If a way has the same starting node as ending node,
             * it is a closed way.
             */
            if ( w.isClosed() ) {
                closedWays.add(w);
            } else {
                openWays.add(w);
            }
        }

        Set<Long> nodeKeys = nodes.keySet();
        for (Long key : nodeKeys) {
            /**
             * If a node is not in a way,
             * put that node in standaloneNodes.
             */
            if ( ! wayNodeIds.contains(key) ) {
                Node n = nodes.get(key);
                standaloneNodes.add(n);
            }
        }

        Set<Long> relationKeys = relations.keySet();
        for (Long key : relationKeys) {
            Relation r = relations.get(key);
            r.link(nodes, ways, relations);
        }
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getWayCount() {
        return ways.size();
    }

    public int getRelationCount() {
        return relations.size();
    }

    public ArrayList<String> getNotes() {
        return notes;
    }

    public Meta getMeta() {
        return meta;
    }

    /**
     * Returns all nodes in the data set, including those that are in and not
     * in ways.
     *
     * @return all nodes
     */
    public Map<Long, Node> getNodes() {
        return nodes;
    }
    /**
     * Returns only the nodes that are not part of ways / relations.
     *
     * @return standalone nodes
     */
    public List<Node> getStandaloneNodes() {
        return standaloneNodes;
    }

    public int getStandaloneNodesCount() {
        return standaloneNodes.size();
    }

    public Map<Long, Way> getWays() {
        return ways;
    }

    public List<Way> getClosedWays() {
        return closedWays;
    }

    public int getClosedWaysCount() {
        return closedWays.size();
    }

    public List<Way> getOpenWays() {
        return openWays;
    }

    public int getOpenWaysCount() {
        return openWays.size();
    }

    public Map<Long, Relation> getRelations() {
        return relations;
    }

}

/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import org.xmlpull.v1.XmlSerializer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Relation extends OSMElement {

    private LinkedList<Long> nodeRefs = new LinkedList<>();
    private LinkedList<Long> wayRefs = new LinkedList<>();
    private LinkedList<Long> relationRefs = new LinkedList<>();

    private LinkedList<Node> linkedNodes = new LinkedList<>();
    private LinkedList<Way> linkedWays = new LinkedList<>();
    private LinkedList<Relation> linkedRelations = new LinkedList<>();

    public Relation( String idStr,
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

    public void addWayRef(long id) {
        wayRefs.add(id);
    }

    public void addRelationRef(long id) {
        relationRefs.add(id);
    }

    int link(Map<Long, Node> nodes, Map<Long, Way> ways, Map<Long, Relation> relations) {
        int unlinkedNodes = linkNodes(nodes);
        int unlinkedWays = linkWays(ways);
        int unlinkedRelations = linkRelations(relations);
        return unlinkedNodes + unlinkedWays + unlinkedRelations;
    }

    private int linkNodes(Map<Long, Node> nodes) {
        LinkedList<Long> unlinkedRefs = new LinkedList<>();
        while (nodeRefs.size() > 0) {
            Long refId = nodeRefs.pop();
            Node node = nodes.get(refId);
            if (node == null) {
                unlinkedRefs.push(refId);
            } else {
                node.addRelation(this);
                linkedNodes.push(node);
            }
        }
        nodeRefs = unlinkedRefs;
        return nodeRefs.size();
    }

    private int linkWays(Map<Long, Way> ways) {
        LinkedList<Long> unlinkedRefs = new LinkedList<>();
        while (wayRefs.size() > 0) {
            Long refId = wayRefs.pop();
            Way way = ways.get(refId);
            if (way == null) {
                unlinkedRefs.push(refId);
            } else {
                way.addRelation(this);
                linkedWays.push(way);
            }
        }
        wayRefs = unlinkedRefs;
        return wayRefs.size();
    }

    private int linkRelations(Map<Long, Relation> relations) {
        LinkedList<Long> unlinkedRefs = new LinkedList<>();
        while (relationRefs.size() > 0) {
            Long refId = relationRefs.pop();
            Relation relation = relations.get(refId);
            if (relation == null) {
                unlinkedRefs.push(refId);
            } else {
                relation.addRelation(this);
                linkedRelations.push(relation);
            }
        }
        relationRefs = unlinkedRefs;
        return relationRefs.size();
    }

    public void addRelation(Relation relation) {
        linkedRelations.push(relation);
    }

    public List<Relation> getRelations() {
        return linkedRelations;
    }

    public int getUnlinkedMemberCount() {
        return nodeRefs.size() + wayRefs.size() + relationRefs.size();
    }

    @Override
    public Object getOverlay() {
        return null;
    }

}

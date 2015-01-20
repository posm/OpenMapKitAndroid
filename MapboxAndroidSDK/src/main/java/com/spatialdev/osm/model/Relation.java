/**
 * Created by Nicholas Hallahan on 12/24/14.
 * nhallahan@spatialdev.com
 */
package com.spatialdev.osm.model;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Relation extends OSMElement {

    // These are the members that refer to another OSM Element.
    private class RelationMember {
        public Long ref;
        public String type;
        public String role;
        public OSMElement linkedElement;
        
        public RelationMember(Long ref, String type, String role) {
            this.ref = ref;
            this.type = type;
            this.role = role;
        }
    }
    
    private LinkedList<RelationMember> nodeMembers = new LinkedList<>();
    private LinkedList<RelationMember> wayMembers = new LinkedList<>();
    private LinkedList<RelationMember> relationMembers = new LinkedList<>();

    private LinkedList<Node> linkedNodes = new LinkedList<>();
    private LinkedList<Way> linkedWays = new LinkedList<>();
    private LinkedList<Relation> linkedRelations = new LinkedList<>();
    
    private int unlinkedMembersCount = 0;

    public Relation( String idStr,
                     String versionStr,
                     String timestampStr,
                     String changesetStr,
                     String uidStr,
                     String userStr ) {

        super(idStr, versionStr, timestampStr, changesetStr, uidStr, userStr);
    }

    @Override
    void xml(XmlSerializer xmlSerializer) throws IOException {
        xmlSerializer.startTag(null, "relation");
        setOsmElementXmlAttributes(xmlSerializer);
        // generate members
        
        // generate tags
        super.xml(xmlSerializer);
        xmlSerializer.endTag(null, "relation");
    }
    
//    private void setRelationXmlMembers(XmlSerializer xmlSerializer) throws IOException {
//        for (Long wayRef : wayMembers) {
//            
//        }
//        for (Long nodeRef : nodeMembers) {
//            
//        }
//    }

    public void addNodeRef(long id, String role) {
        nodeMembers.add(new RelationMember(id, "node", role));
    }

    public void addWayRef(long id, String role) {
        wayMembers.add(new RelationMember(id, "way", role));
    }

    public void addRelationRef(long id, String role) {
        relationMembers.add(new RelationMember(id, "relation", role));
    }

    int link(Map<Long, Node> nodes, Map<Long, Way> ways, Map<Long, Relation> relations) {
        int unlinkedNodes = linkNodes(nodes);
        int unlinkedWays = linkWays(ways);
        int unlinkedRelations = linkRelations(relations);
        unlinkedMembersCount = unlinkedNodes + unlinkedWays + unlinkedRelations;
        return unlinkedMembersCount;
    }

    private int linkNodes(Map<Long, Node> nodes) {
        int unlinkedCount = 0;
        for (RelationMember mem : nodeMembers) {
            Node node = nodes.get(mem.ref);
            if (node == null) {
                ++unlinkedCount;
            } else {
                node.addRelation(this);
                mem.linkedElement = node;
                linkedNodes.push(node);
            }
        }
        return unlinkedCount;
    }

    private int linkWays(Map<Long, Way> ways) {
        int unlinkedCount = 0;
        for (RelationMember mem : wayMembers) {
            Way way = ways.get(mem.ref);
            if (way == null) {
                ++unlinkedCount;
            } else {
                way.addRelation(this);
                mem.linkedElement = way;
                linkedWays.push(way);
            }
        }
        return unlinkedCount;
    }

    private int linkRelations(Map<Long, Relation> relations) {
        int unlinkedCount = 0;
        for (RelationMember mem: relationMembers) {
            Relation rel = relations.get(mem.ref);
            if (rel == null) {
                ++unlinkedCount;
            } else {
                rel.addRelation(this);
                mem.linkedElement = rel;
                linkedRelations.push(rel);
            }
        }
        return unlinkedCount;
    }

    public void addRelation(Relation relation) {
        linkedRelations.push(relation);
    }

    public List<Relation> getRelations() {
        return linkedRelations;
    }

    public int getUnlinkedMemberCount() {
        return unlinkedMembersCount;
    }

    @Override
    public Object getOverlay() {
        return null;
    }

}

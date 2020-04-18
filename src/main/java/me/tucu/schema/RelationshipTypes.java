package me.tucu.schema;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    FOLLOWS,
    LIKES,
    MUTES,
    PROMOTES,
    REPLIED_TO,
    REPOSTED
}

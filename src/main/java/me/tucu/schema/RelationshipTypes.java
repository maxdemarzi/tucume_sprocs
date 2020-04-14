package me.tucu.schema;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    FOLLOWS,
    MUTES,
    LIKES,
    PROMOTES,
    REPLIED_TO,
    REPOSTED
}

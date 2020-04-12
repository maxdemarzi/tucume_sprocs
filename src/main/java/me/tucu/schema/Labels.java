package me.tucu.schema;

import org.neo4j.graphdb.Label;

public enum Labels implements Label {
    Conversation,
    Post,
    Product,
    User,
    Tag
}

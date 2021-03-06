package me.tucu.schema;

import me.tucu.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.stream.Stream;

import static me.tucu.schema.Properties.*;

public class Schema {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.schema.create", mode = Mode.SCHEMA)
    @Description("CALL me.tucu.schema.create() - create schema")
    public Stream<StringResult> create() {
        ArrayList<String> results = new ArrayList<>();

        // We have a need to quickly find out if a user Re-advertises an Advertisement
        try (Transaction tx = db.beginTx()) {
            org.neo4j.graphdb.schema.Schema schema = tx.schema();
            if (!schema.getIndexes(Labels.Post).iterator().hasNext()) {
                schema.indexFor(Labels.Post).on(USERNAME).on(POST_ID).create();
                tx.commit();
                results.add("(:Post {username, post_id}) index created");
            }
        }
        try (Transaction tx = db.beginTx()) {
            org.neo4j.graphdb.schema.Schema schema = tx.schema();
            if (!schema.getConstraints(Labels.Product).iterator().hasNext()) {
                schema.constraintFor(Labels.Product)
                        .assertPropertyIsUnique(ID)
                        .create();

                schema.indexFor(Labels.Product).on(NAME).create();
                tx.commit();
                results.add("(:Product {id}) constraint created");
                results.add("(:Product {name}) index created");
            }
        }

        try (Transaction tx = db.beginTx()) {
            org.neo4j.graphdb.schema.Schema schema = tx.schema();
            if (!schema.getConstraints(Labels.User).iterator().hasNext()) {
                schema.constraintFor(Labels.User)
                        .assertPropertyIsUnique(USERNAME)
                        .create();
                tx.commit();
                results.add("(:User {username}) constraint created");
            }
        }

        try (Transaction tx = db.beginTx()) {
            org.neo4j.graphdb.schema.Schema schema = tx.schema();
            if (!schema.getConstraints(Labels.Tag).iterator().hasNext()) {
                schema.constraintFor(Labels.Tag)
                        .assertPropertyIsUnique(NAME)
                        .create();
                tx.commit();
                results.add("(:Tag {name}) constraint created");
            }
        }

        return results.stream().map(StringResult::new);
    }
}

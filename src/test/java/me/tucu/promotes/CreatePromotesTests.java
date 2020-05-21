package me.tucu.promotes;

import me.tucu.fixtures.Users;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.time.ZonedDateTime;
import java.util.HashMap;

import static me.tucu.schema.Properties.STATUS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreatePromotesTests {
    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Promotes.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldCreatePromotes()
    {
        // In a try-block, to make sure we close the transaction after the test
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Node post = tx.findNode(Labels.Post, "test", 1);
            HashMap<String, Object> input = new HashMap<>();
            input.put(STATUS, post.getProperty(STATUS));

            // When I use the method
            Promotes.createPromotes(post, input, ZonedDateTime.now(), tx);

            tx.commit();
        }

        // Then I should get what I expect
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {
            Node post = tx.findNode(Labels.Post, "test", 1);
            assertThat(post.getDegree( RelationshipTypes.PROMOTES, Direction.OUTGOING), is(1));
        }
    }

    @Test
    void shouldNotCreatePromotesProductNotThere()
    {
        // In a try-block, to make sure we close the transaction after the test
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Node post = tx.findNode(Labels.Post, "test", 2);
            HashMap<String, Object> input = new HashMap<>();
            input.put(STATUS, post.getProperty(STATUS));

            // When I use the method
            Node product = Promotes.createPromotes(post, input, ZonedDateTime.now(), tx);

            // Then I should get what I expect
            assertThat(product ,is(nullValue()));
            tx.commit();
        }


        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {
            Node post = tx.findNode(Labels.Post, "test", 2);
            assertThat(post.getDegree( RelationshipTypes.PROMOTES, Direction.OUTGOING), is(0));
        }
    }

    @Test
    void shouldCreatePromotesButDeletePreviousRel()
    {
        // In a try-block, to make sure we close the transaction after the test
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Node post = tx.findNode(Labels.Post, "test", 3);
            HashMap<String, Object> input = new HashMap<>();
            input.put(STATUS, post.getProperty(STATUS));

            // When I use the method
            Promotes.createPromotes(post, input, ZonedDateTime.now(), tx);

            tx.commit();
        }

        // Then I should get what I expect
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {
            Node post = tx.findNode(Labels.Post, "test", 3);
            assertThat(post.getDegree( RelationshipTypes.PROMOTES, Direction.OUTGOING), is(1));
        }
    }

    @Test
    void shouldCreatePromotesButOnlyFirstProduct()
    {
        // In a try-block, to make sure we close the transaction after the test
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Node post = tx.findNode(Labels.Post, "test", 4);
            HashMap<String, Object> input = new HashMap<>();
            input.put(STATUS, post.getProperty(STATUS));

            // When I use the method
            Promotes.createPromotes(post, input, ZonedDateTime.now(), tx);

            tx.commit();
        }

        // Then I should get what I expect
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {
            Node post = tx.findNode(Labels.Post, "test", 4);
            assertThat(post.getDegree( RelationshipTypes.PROMOTES, Direction.OUTGOING), is(1));
        }
    }

    private static final String FIXTURE =
            Users.MAX + Users.JEXP +
                    "CREATE (product:Product {id:'stuff', price: 1000, time: datetime('2020-04-23T01:38:22.000+0100')} )" +
                    "CREATE (product2:Product {id:'stuff2', price: 1000, time: datetime('2020-04-23T01:38:22.000+0100')} )" +
                    "CREATE (post1:Post {status:'Buy my $stuff', time: datetime(), test:1})" +
                    "CREATE (post2:Post {status:'Buy my $ffuts', time: datetime(), test:2})" +
                    "CREATE (post3:Post {status:'Buy my $stuff again', time: datetime(), test:3})" +
                    "CREATE (post4:Post {status:'Buy my $stuff and $stuff2 please', time: datetime(), test:4})" +
                    "CREATE (post3)-[:PROMOTES]->(product1)";
}

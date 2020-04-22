package me.tucu.tags;

import me.tucu.schema.Labels;
import me.tucu.schema.Properties;
import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.time.ZonedDateTime;
import java.util.HashMap;

import static me.tucu.schema.Properties.STATUS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateTagsTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Tags.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldCreateTags()
    {

        // In a try-block, to make sure we close the transaction after the test
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {

            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            ResourceIterator<Node> iter = tx.findNodes(Labels.Post);
            while (iter.hasNext()) {
                Node post = iter.next();
                HashMap<String, Object> input = new HashMap<>();
                input.put(STATUS, post.getProperty(STATUS));
                // When I use the method
                Tags.createTags(post, input, ZonedDateTime.now(), tx);
            }

            tx.commit();
        }

        // Then I should get what I expect
        try(Transaction tx = neo4j.defaultDatabaseService().beginTx()) {
            ResourceIterator<Node> iter = tx.findNodes(Labels.Tag);
            while (iter.hasNext()) {
                Node tag = iter.next();
                assertThat(tag.getProperty(Properties.NAME), is("neo4j"));
            }
        }

    }

    private static final String FIXTURE =
                    "CREATE (post1:Post {status:'Hello World! #neo4j #neo4j', time: datetime()})" ;

}

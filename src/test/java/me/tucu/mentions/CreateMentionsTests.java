package me.tucu.mentions;

import me.tucu.schema.Labels;
import me.tucu.schema.Schema;
import me.tucu.tags.Tags;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.time.ZonedDateTime;
import java.util.HashMap;

import static me.tucu.schema.DatedRelationshipTypes.MENTIONED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.utils.Time.dateFormatter;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateMentionsTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Mentions.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldCreateMentions()
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
            ResourceIterator<Node> iter = tx.findNodes(Labels.Post);
            while (iter.hasNext()) {
                Node post = iter.next();
                ZonedDateTime time = (ZonedDateTime)post.getProperty(TIME);
                RelationshipType mentioned_on = RelationshipType.withName(MENTIONED_ON +
                        time.format(dateFormatter));

                for (Relationship mentioned : post.getRelationships(Direction.OUTGOING, mentioned_on)) {
                    Node user = mentioned.getEndNode();
                    assertThat(user.getProperty(USERNAME), is("jexp"));
                }
            }
        }

    }

    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'swordfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "time: 1490054400," +
                    "password: 'tunafish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (post1:Post {status:'Hello @jexp', time: datetime()})" ;

}

package me.tucu.search;

import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.tucu.schema.Properties.TIME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class GetSearchTests {
    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Search.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldGetSearch()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term);",
                    parameters("term", "hello"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual, is(EXPECTED));
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
                    "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'cuddlefish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (mark:User {username:'markhneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "name: 'Mark Needham'," +
                    "password: 'jellyfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (jerk:User {username:'jerk', " +
                    "email: 'jerk@neo4j.com', " +
                    "hash: 'some hash'," +
                    "name: 'Some Jerk'," +
                    "password: 'jellyfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (post1:Post {status:'Hello @jexp', " +
                    "time: datetime('2020-04-01T12:44:08.556+0100')})" +
                    "CREATE (post2:Post {status:'Hello world', " +
                    "time: datetime('2020-04-12T11:50:35.556+0100')})" +
                    "CREATE (post3:Post {status:'Lowercase hello', " +
                    "time: datetime('2020-04-13T04:20:12.000+0100')})" +
                    "CREATE (post4:Post {status:'I think @jexp sucks but hello', " +
                    "time: datetime('2020-04-14T09:53:23.000+0100')})" +
                    "CREATE (max)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.556+0100') }]->(post2)" +
                    "CREATE (max)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T04:20:12.000+0100') }]->(post3)" +
                    "CREATE (jerk)-[:POSTED_ON_2020_04_14 {time: datetime('2020-04-14T09:53:23.000+0100') }]->(post4)" +
                    "CREATE (post1)-[:MENTIONED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(jexp)" +
                    "CREATE (post2)-[:MENTIONED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.556+0100') }]->(jexp)" +
                    "CREATE (post3)-[:MENTIONED_ON_2020_04_13 {time: datetime('2020-04-13T04:20:12.000+0100') }]->(jexp)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:15:33.000+0100')}]->(post1)" +
                    "CREATE (max)-[:LIKES {time: datetime('2020-04-12T11:55:00.000+0100') }]->(post2)" +
                    "CREATE (jexp)-[:MUTES {time: datetime('2020-03-01T12:44:08.556+0100') }]->(jerk)" +
                    "CREATE (jexp)-[:FOLLOWS {time: datetime('2020-03-01T12:44:08.556+0100') }]->(max)" +
                    "CREATE (max)-[:MUTES {time: datetime('2020-03-01T12:44:08.556+0100') }]->(jerk)" ;

    private static final ArrayList<HashMap<String, Object>> EXPECTED = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "jerk");
            put("name", "Some Jerk");
            put("hash", "some hash");
            put("status", "I think @jexp sucks but hello");
            put("likes", 0L);
            put("reposts", 0L);
        }});
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Lowercase hello");
            put("likes", 0L);
            put("reposts", 0L);
        }});
    }};
}

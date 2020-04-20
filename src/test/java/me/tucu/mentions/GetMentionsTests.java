package me.tucu.mentions;

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

public class GetMentionsTests {

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
    void shouldGetMentions()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.mentions.get($username);",
                    parameters("username", "jexp"));

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

    @Test
    void shouldGetMentionsLimited()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.mentions.get($username, $limit);",
                    parameters("username", "jexp", "limit", 1));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.get(0), is(EXPECTED.get(0)));
        }
    }

    @Test
    void shouldGetMentionsSince()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            //1586111067 is Sunday, April 5, 2020 6:24:27 PM
            Result result = session.run( "CALL me.tucu.mentions.get($username, $limit, $since);",
                    parameters("username", "jexp", "limit", 1, "since", 1586111067));


            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.get(0), is(EXPECTED.get(2)));
        }
    }

    @Test
    void shouldGetMentionsSinceSecondUser()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            //1586111067 is Sunday, April 5, 2020 6:24:27 PM
            Result result = session.run( "CALL me.tucu.mentions.get($username, $limit, $since, $username2);",
                    parameters("username", "jexp", "limit", 1, "since", 1586111067, "username2", "maxdemarzi"));


            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.get(0), is(EXPECTED2.get(1)));
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
                    "CREATE (post1:Post {status:'Hello @jexp', " +
                    "time: datetime('2020-04-01T12:44:08.556+0100')})" +
                    "CREATE (post2:Post {status:'Hi @jexp', " +
                    "time: datetime('2020-04-12T11:50:35.556+0100')})" +
                    "CREATE (post3:Post {status:'Stalking @jexp', " +
                    "time: datetime('2020-04-13T04:20:12.000+0100')})" +
                    "CREATE (max)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.556+0100') }]->(post2)" +
                    "CREATE (mark)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T04:20:12.000+0100') }]->(post3)" +
                    "CREATE (post1)-[:MENTIONED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(jexp)" +
                    "CREATE (post2)-[:MENTIONED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.556+0100') }]->(jexp)" +
                    "CREATE (post3)-[:MENTIONED_ON_2020_04_13 {time: datetime('2020-04-13T04:20:12.000+0100') }]->(jexp)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:15:33.000+0100')}]->(post1)" +
                    "CREATE (max)-[:LIKES {time: datetime('2020-04-12T11:55:00.000+0100') }]->(post2)" +
                    "CREATE (jexp)-[:BLOCKS {time: datetime('2020-03-01T12:44:08.556+0100') }]->(mark)";

    private static final ArrayList<HashMap<String, Object>> EXPECTED = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "markhneedham");
            put("name", "Mark Needham");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Stalking @jexp");
            put("likes", 0L);
            put("reposts", 0L);
        }});
        add(new HashMap<>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hi @jexp");
            put("likes", 1L);
            put("reposts", 0L);
        }});
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("likes", 0L);
            put("reposts", 1L);
        }});

    }};

    private static final ArrayList<HashMap<String, Object>> EXPECTED2 = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hi @jexp");
            put("likes", 1L);
            put("reposts", 0L);
            put("liked", true);
            put("reposted", false);
        }});
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("likes", 0L);
            put("reposts", 1L);
            put("liked", false);
            put("reposted", false);
        }});
    }};
}

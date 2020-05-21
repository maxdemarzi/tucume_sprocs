package me.tucu.timeline;

import me.tucu.fixtures.Users;
import me.tucu.schema.Schema;
import me.tucu.users.UserExceptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.tucu.schema.Properties.REPOSTED_TIME;
import static me.tucu.schema.Properties.TIME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class GetTimelineTests {
    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Timeline.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldGetTimeline()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.timeline.get($username);",
                    parameters("username", "jexp"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                modifiable.remove(REPOSTED_TIME);
                actual.add(modifiable);
            });

            assertThat(actual, is(EXPECTED));
        }
    }

    @Test
    void shouldGetTimelineWithLimit()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.timeline.get($username, $limit);",
                    parameters("username", "jexp", "limit", 2));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                modifiable.remove(REPOSTED_TIME);
                actual.add(modifiable);
            });

            assertThat(actual, is(EXPECTED.subList(0,2)));
        }
    }
    @Test
    void shouldGetTimelineWithLimitSince()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.timeline.get($username, $limit, $since);",
                    parameters("username", "jexp", "limit", 2,
                            "since", ZonedDateTime.parse("2020-04-12T00:00:00+00:00[UTC]").toEpochSecond()));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                modifiable.remove(REPOSTED_TIME);
                actual.add(modifiable);
            });

            assertThat(actual.get(0), is(EXPECTED2));
        }
    }

    @Test
    void shouldNOTGetTimelinehWithLimitAndSinceAndUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.timeline.get($username, $limit, $since);",
                    parameters("username", "not_there", "limit", 3,
                            "since", ZonedDateTime.parse("2020-04-12T00:00:00+00:00[UTC]").toEpochSecond()));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    private static final String FIXTURE =
            Users.MAX + Users.JEXP + Users.LUKE + Users.MARK + Users.JERK +
                    "CREATE (hello:User {username:'hello_there', " +
                    "email: 'hello_there@neo4j.com', " +
                    "hash: 'some hash'," +
                    "name: 'hello there user'," +
                    "password: 'catfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (post1:Post {status:'Hello @jexp', " +
                    "time: datetime('2020-04-01T12:44:08.556+0100')})" +
                    "CREATE (post2:Post {status:'Hello world', " +
                    "time: datetime('2020-04-12T11:50:35.556+0100')})" +
                    "CREATE (post3:Post {status:'Lowercase #hello', " +
                    "time: datetime('2020-04-13T04:20:12.000+0100')})" +
                    "CREATE (post4:Post {status:'I think @jexp sucks but hello', " +
                    "time: datetime('2020-04-14T09:53:23.000+0100')})" +
                    "CREATE (tag:Tag {name:'hello', " +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (product:Product {id:'product1', name:'hello', " +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" +
                    "CREATE (max)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.556+0100') }]->(post2)" +
                    "CREATE (max)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T04:20:12.000+0100') }]->(post3)" +
                    "CREATE (jerk)-[:POSTED_ON_2020_04_14 {time: datetime('2020-04-14T09:53:23.000+0100') }]->(post4)" +
                    "CREATE (post1)-[:MENTIONED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(jexp)" +
                    "CREATE (post2)-[:MENTIONED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.556+0100') }]->(jexp)" +
                    "CREATE (post3)-[:MENTIONED_ON_2020_04_13 {time: datetime('2020-04-13T04:20:12.000+0100') }]->(jexp)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:15:33.000+0100')}]->(post1)" +
                    "CREATE (max)-[:LIKES {time: datetime('2020-04-12T11:55:00.000+0100') }]->(post2)" +
                    "CREATE (jexp)-[:FOLLOWS {time: datetime('2020-03-01T12:44:08.556+0100') }]->(max)" +
                    "CREATE (jexp)-[:FOLLOWS {time: datetime('2020-03-01T12:44:08.556+0100') }]->(laeg)" +
                    "CREATE (jexp)-[:FOLLOWS {time: datetime('2020-03-01T12:44:08.556+0100') }]->(mark)" +
                    "CREATE (mark)-[:REPOSTED_ON_2020_04_14 {time: datetime('2020-04-14T04:26:54.000+0100')}]->(post4)" +
                    "CREATE (max)-[:MUTES {time: datetime('2020-03-01T12:44:08.556+0100') }]->(jerk)" ;

    private static final ArrayList<HashMap<String, Object>> EXPECTED = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("reposter_username", "laexample");
            put("reposter_name", "Luke Gannon");
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("likes", 0L);
            put("reposts", 1L);
            put("liked", false);
            put("reposted", false);
        }});
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Lowercase #hello");
            put("likes", 0L);
            put("reposts", 0L);
            put("liked", false);
            put("reposted", false);
        }});
        add(new HashMap<>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello world");
            put("likes", 1L);
            put("reposts", 0L);
            put("liked", false);
            put("reposted", false);
        }});
    }};

    private static final HashMap<String, Object> EXPECTED2 = new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("likes", 0L);
            put("reposts", 1L);
            put("liked", false);
            put("reposted", false);
        }};
}

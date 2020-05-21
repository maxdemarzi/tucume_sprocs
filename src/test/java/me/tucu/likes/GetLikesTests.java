package me.tucu.likes;

import me.tucu.fixtures.Posts;
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

import static me.tucu.schema.Properties.LIKED_TIME;
import static me.tucu.schema.Properties.TIME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class GetLikesTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Likes.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldGetLikes()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.get($username);",
                    parameters("username", "maxdemarzi"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                modifiable.remove(LIKED_TIME);
                actual.add(modifiable);
            });

            assertThat(actual, is(EXPECTED));
        }
    }

    @Test
    void shouldGetLikesLimited()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.get($username, $limit);",
                    parameters("username", "maxdemarzi", "limit", 1));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                modifiable.remove(LIKED_TIME);

                actual.add(modifiable);
            });
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0), is(EXPECTED.get(0)));
        }
    }

    @Test
    void shouldGetLikesSince()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.get($username, $limit, $since);",
                    parameters("username", "maxdemarzi", "limit", 25, "since", ZonedDateTime.now().toEpochSecond() - 86400));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                modifiable.remove(LIKED_TIME);
                actual.add(modifiable);
            });
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0), is(EXPECTED.get(1)));
        }
    }

    @Test
    void shouldGetLikesSinceSecondUser()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.get($username, $limit, $since, $username2);",
                    parameters("username", "maxdemarzi",
                            "limit", 25,
                            "since", ZonedDateTime.now().toEpochSecond() - 86400,
                            "username2", "jexp"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                modifiable.remove(LIKED_TIME);
                actual.add(modifiable);
            });
            assertThat(actual.size(), is(1));
            assertThat(actual, is(EXPECTED2));
        }
    }

    @Test
    void shouldNotGetLikesNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.get($username);",
                    parameters("username", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotGetLikesSecondUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.get($username, $limit, $since, $username2);",
                    parameters("username", "jexp",
                            "limit", 25,
                            "since", ZonedDateTime.now().toEpochSecond() - 86400,
                            "username2", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    private static final String FIXTURE =
            Users.MAX + Users.JEXP + Users.LUKE + Users.MARK +
            Posts.POST1_0401 + Posts.POST2_0412 + Posts.POST3_0413 +
            "CREATE (jexp)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
            "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.556+0100') }]->(post2)" +
            "CREATE (laeg)-[:REPOSTED_ON_2020_04_12 {time: datetime('2020-04-12T12:33:00.556+0100')}]->(post1)" +
            "CREATE (max)-[:LIKES {time: datetime() - duration('P7D') }]->(post1)" +
            "CREATE (max)-[:LIKES {time: datetime() }]->(post2)" +
            "CREATE (jexp)-[:LIKES {time: datetime() }]->(post2)" ;

    private static final ArrayList<HashMap<String, Object>> EXPECTED = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "How are you!");
            put("likes", 2L);
            put("reposts", 0L);
        }});
        add(new HashMap<>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World!");
            put("likes", 1L);
            put("reposts", 1L);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> EXPECTED2 = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World!");
            put("likes", 1L);
            put("reposts", 1L);
            put("liked", false);
            put("reposted", false);
        }});
    }};
}

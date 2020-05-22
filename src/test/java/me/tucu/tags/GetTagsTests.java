package me.tucu.tags;

import me.tucu.fixtures.Graph;
import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.tucu.schema.Properties.TIME;
import static me.tucu.tags.TagExceptions.TAG_NOT_FOUND;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class GetTagsTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Tags.class)
                .withFixture(Graph.getGraph())
                .build();
    }

    @Test
    void shouldGetTags()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.tags.get($hashtag);",
                    parameters("hashtag", "neo4j"));

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
    void shouldGetTagsLimited()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.tags.get($hashtag, $limit);",
                    parameters("hashtag", "neo4j", "limit", 1));

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
    void shouldGetTagsSince()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            //1586111067 is Sunday, April 5, 2020 6:24:27 PM
            Result result = session.run( "CALL me.tucu.tags.get($hashtag, $limit, $since);",
                    parameters("hashtag", "neo4j", "limit", 25, "since", ZonedDateTime.parse("2020-04-05T00:01+01:00").toEpochSecond()));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.get(0), is(EXPECTED.get(1)));
        }
    }

    @Test
    void shouldGetTagsWithUser()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            //1586111067 is Sunday, April 5, 2020 6:24:27 PM
            Result result = session.run( "CALL me.tucu.tags.get($hashtag, $limit, $since, $username);",
                    parameters("hashtag", "neo4j", "limit", 25,
                            "since", ZonedDateTime.parse("2020-04-05T00:01+01:00").toEpochSecond(), "username", "maxdemarzi"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.get(0), is(EXPECTED2.get(0)));
        }
    }

    @Test
    void shouldGetTagsWithUserNotMuted()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            //1586111067 is Sunday, April 5, 2020 6:24:27 PM
            Result result = session.run( "CALL me.tucu.tags.get($hashtag, $limit, $since, $username);",
                    parameters("hashtag", "graphs", "limit", 25,
                            "since", ZonedDateTime.parse("2020-04-16T00:00:00+00:00[UTC]").toEpochSecond(),
                            "username", "maxdemarzi"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual, is(EXPECTED3));
        }
    }
    @Test
    void shouldNotGetTagsNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.tags.get($hashtag);",
                    parameters("hashtag", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(TAG_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotGetTagsUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.tags.get($hashtag, $limit, $since, $username);",
                    parameters("hashtag", "neo4j", "limit", 25,
                            "since", 1586111067, "username", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(USER_NOT_FOUND.value));
        }
    }

    private static final ArrayList<HashMap<String, Object>> EXPECTED = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "How are you! #neo4j #neo4j");
            put("likes", 2L);
            put("reposts", 0L);
        }});
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "I like #neo4j but I am biased.");
            put("likes", 1L);
            put("reposts", 0L);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> EXPECTED2 = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "I like #neo4j but I am biased.");
            put("likes", 1L);
            put("reposts", 0L);
            put("liked", false);
            put("reposted", false);
        }});
    }};

    private static final ArrayList<HashMap<String, Object>> EXPECTED3 = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Cannot like me! But like #graphs");
            put("likes", 0L);
            put("reposts", 0L);
            put("liked", false);
            put("reposted", false);
        }});
    }};
}

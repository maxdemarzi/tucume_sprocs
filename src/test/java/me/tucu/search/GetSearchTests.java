package me.tucu.search;

import me.tucu.fixtures.Graph;
import me.tucu.schema.Schema;
import me.tucu.users.UserExceptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.tucu.schema.Properties.TIME;
import static org.hamcrest.CoreMatchers.equalTo;
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
                .withFixture(INDEX)
                .withFixture(Graph.getGraph())
                .build();
    }

    @Test
    void shouldGetFullTextSearch()
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

    @Test
    void shouldGetFullTextSearchUserType()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type);",
                    parameters("term", "hello", "type", "user"));

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
    void shouldGetFullTextSearchPostType()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type);",
                    parameters("term", "hello", "type", "post"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual, is(EXPECTED.subList(2,6)));
        }
    }

    @Test
    void shouldGetFullTextSearchProductType()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type);",
                    parameters("term", "hello", "type", "product"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual, is(EXPECTED.subList(1,2)));
        }
    }

    @Test
    void shouldGetFullTextSearchWithLimit()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type, $limit);",
                    parameters("term", "hello", "type", "", "limit", 2));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.size(), is(2));
            assertThat(actual, is(EXPECTED.subList(0,2)));
        }
    }

    @Test
    void shouldGetFullTextSearchWithLimitAndOffset()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type, $limit, $offset);",
                    parameters("term", "hello", "type", "", "limit", 2, "offset", 2));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.size(), is(2));
            assertThat(actual, is(EXPECTED.subList(2,4)));
        }
    }

    @Test
    void shouldGetFullTextSearchWithLimitAndOffsetAndUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type, $limit, $offset, $username);",
                    parameters("term", "hello", "type", "post", "limit", 3, "offset", 2, "username", "jexp"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.size(), is(1));
            assertThat(actual.get(0), is(EXPECTED2));
        }
    }

    @Test
    void shouldGetFullTextSearchWithLimitAndOffsetAndUsernameTwo()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type, $limit, $offset, $username);",
                    parameters("term", "hello", "type", "post", "limit", 3, "offset", 1, "username", "maxdemarzi"));

            // Then I should get what I expect
            ArrayList<Map<String, Object>> actual = new ArrayList<>();
            result.forEachRemaining(e -> {
                Map<String, Object> record = e.get("value").asMap();
                HashMap<String, Object> modifiable = new HashMap<>(record);
                modifiable.remove(TIME);
                actual.add(modifiable);
            });

            assertThat(actual.size(), is(1));
            assertThat(actual.get(0), is(EXPECTED2));
        }
    }

    @Test
    void shouldNOTGetFullTextSearchWithLimitAndOffsetAndUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.search.get($term, $type, $limit, $offset, $username);",
                    parameters("term", "hello", "type", "post", "limit", 3, "offset", 2, "username", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    private static final String INDEX = "CALL db.index.fulltext.createNodeIndex('fulltext', ['Post','User','Product'], ['status','username','name'])";

    private static final ArrayList<HashMap<String, Object>> EXPECTED = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("label", "user");
            put("username", "hello_there");
            put("name", "hello there user");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("followers", 0L);
            put("following", 0L);
            put("posts", 0L);
            put("likes", 0L);
            put("i_follow", false);
        }});
        add(new HashMap<>() {{
            put("label", "product");
            put("id", "hello_product");
            put("name", "hello");
        }});
        add(new HashMap<>() {{
            put("label", "post");
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World!");
            put("likes", 1L);
            put("reposts", 1L);
        }});
        add(new HashMap<>() {{
            put("label", "post");
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello @jexp");
            put("likes", 0L);
            put("reposts", 0L);
        }});
        add(new HashMap<>() {{
            put("label", "post");
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Lowercase #hello");
            put("likes", 0L);
            put("reposts", 0L);
        }});
        add(new HashMap<>() {{
            put("label", "post");
            put("username", "jerk");
            put("name", "Some Jerk");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "I think @jexp sucks but hello");
            put("likes", 0L);
            put("reposts", 0L);
        }});
    }};

    private static final HashMap<String, Object> EXPECTED2 =new HashMap<>() {{
        put("label", "post");
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
        put("status", "Lowercase #hello");
        put("likes", 0L);
        put("reposts", 0L);
        put("liked", false);
        put("reposted", false);
    }};
}

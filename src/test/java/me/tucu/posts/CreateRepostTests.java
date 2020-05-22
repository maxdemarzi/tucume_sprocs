package me.tucu.posts;

import me.tucu.fixtures.Graph;
import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static me.tucu.posts.PostExceptions.POST_NOT_FOUND;
import static me.tucu.posts.PostExceptions.PRODUCT_NOT_PURCHASED;
import static me.tucu.schema.Properties.TIME;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class CreateRepostTests {
    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Posts.class)
                .withFixture(Graph.getGraph())
                .build();
    }

    @Test
    void shouldCreateRepost()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.repost($post_id, $username);",
                    parameters("username", "maxdemarzi", "post_id", 12));

            // Then I should get what I expect
            Map<String, Object> record = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(record);
            modifiable.remove(TIME);

            assertThat(modifiable, is(EXPECTED_GOLD));
        }
    }

    @Test
    void shouldCreateRepostProductPurchased()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.repost($post_id, $username);",
                    parameters("username", "darthvader42", "post_id", 12));

            // Then I should get what I expect
            // Then I should get what I expect
            Map<String, Object> record = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(record);
            modifiable.remove(TIME);

            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldNotCreateRepostUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.repost($post_id, $username);",
                    parameters("username", "not_there", "post_id", 4));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreateRepostProductNotPurchased()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.repost($post_id, $username);",
                    parameters("username", "rich", "post_id", 12));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(PRODUCT_NOT_PURCHASED.value));
        }
    }

    @Test
    void shouldNotCreateRepostPostNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.repost($post_id, $username);",
                    parameters("username", "laexample", "post_id", 400));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreateRepostPostIdDoesNotBelongToPost()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.repost($post_id, $username);",
                    parameters("username", "laexample", "post_id", 1));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
        put("status", "Please buy $mystuff");
        put("likes", 0L);
        put("silver", true);
        put("reposts", 2L);
        put("liked", false);
        put("reposted", true);
    }};

    private static final HashMap<String, Object> EXPECTED_GOLD = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
        put("status", "Please buy $mystuff");
        put("likes", 0L);
        put("gold", true);
        put("reposts", 1L);
        put("liked", false);
        put("reposted", true);
    }};
}

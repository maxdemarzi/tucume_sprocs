package me.tucu.posts;

import me.tucu.Exceptions;
import me.tucu.fixtures.Graph;
import me.tucu.schema.Schema;
import me.tucu.users.UserExceptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static me.tucu.Exceptions.INVALID_INPUT;
import static me.tucu.posts.PostExceptions.*;
import static me.tucu.schema.Properties.TIME;
import static me.tucu.users.UserExceptions.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class CreateReplyTests {

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
    void shouldCreateReply()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 8, "parameters", INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<String, Object>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldCreateReplyWithATag()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 8, "parameters", WITH_A_TAG_INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(WITH_A_TAG_EXPECTED));
        }
    }

    @Test
    void shouldCreateReplyWithAMention()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 8, "parameters", WITH_A_MENTION_INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(WITH_A_MENTION_EXPECTED));
        }
    }

    @Test
    void shouldCreateReplytWithAPromotes()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 8, "parameters", WITH_A_PROMOTES_INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(WITH_A_PROMOTES_EXPECTED));
        }
    }

    @Test
    void shouldNotCreateReplyEmptyStatus()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", EMPTY_STATUS_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(EMPTY_STATUS.value));
        }
    }

    @Test
    void shouldNotCreateReplyMissingStatus()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", NO_STATUS_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(MISSING_STATUS.value));
        }
    }

    @Test
    void shouldNotCreateReplyUserNotThere()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", USER_NOT_THERE_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreateReplyPostNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 400, "parameters", INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreateReplyPostIdDoesNotBelongToPost()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 1, "parameters", INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }


    @Test
    void shouldNotCreateReplyUserIsBroke()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 8, "parameters", USER_IS_BROKE_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(Exceptions.INSUFFICIENT_FUNDS.value));
        }
    }

    @Test
    void shouldNotCreateReplyUserOwesGold()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 8, "parameters", USER_OWES_GOLD_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(Exceptions.INSUFFICIENT_FUNDS.value));
        }
    }

    @Test
    void shouldNotCreateReplyMissingUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", MISSING_USERNAME_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(MISSING_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreateReplyBlankUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", BLANK_USERNAME_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(EMPTY_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreateReplyInvalidUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", INVALID_USERNAME_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INVALID_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreateReplyEmptyInput()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", EMPTY_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INVALID_INPUT.value));
        }
    }

    @Test
    void shouldNotCreateReplyNullInput()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 5, "parameters", NULL_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INVALID_INPUT.value));
        }
    }

    @Test
    void shouldCreateReplyUserHasSilver()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.reply($post_id, $parameters);",
                    parameters("post_id", 8, "parameters", WITH_SILVER_INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(WITH_SILVER_EXPECTED));
        }
    }

    private static final HashMap INPUT = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying something");
    }};

    private static final HashMap<String, Object> WITH_A_TAG_INPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying #something");
    }};

    private static final HashMap<String, Object> WITH_A_MENTION_INPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying something to @jexp");
    }};

    private static final HashMap<String, Object> WITH_A_PROMOTES_INPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "Just telling you to buy $mystuff");
    }};

    private static final HashMap<String, Object> WITH_SILVER_INPUT = new HashMap<>() {{
        put("username", "laexample");
        put("status", "Just saying something");
    }};

    private static final HashMap<String, Object> USER_OWES_GOLD_INPUT = new HashMap<>() {{
        put("username", "markneedham");
        put("status", "Just saying something");
    }};

    private static final HashMap<String, Object> EMPTY_STATUS_INPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "");
    }};

    private static final HashMap<String, Object> MISSING_USERNAME_INPUT = new HashMap<>() {{
        put("status", "some status");
    }};

    private static final HashMap<String, Object> BLANK_USERNAME_INPUT = new HashMap<>() {{
        put("username", "");
        put("status", "some status");
    }};

    private static final HashMap<String, Object> INVALID_USERNAME_INPUT = new HashMap<>() {{
        put("username", "1");
        put("status", "some status");
    }};

    private static final HashMap<String, Object> EMPTY_INPUT = new HashMap<>() {{ }};

    private static final HashMap<String, Object> NULL_INPUT = null;

    private static final HashMap<String, Object> USER_NOT_THERE_INPUT = new HashMap<>() {{
        put("username", "not_there");
        put("status", "Just saying something");
    }};

    private static final HashMap<String, Object> USER_IS_BROKE_INPUT = new HashMap<>() {{
        put("username", "markneedham");
        put("status", "Cannot say this because I a broke");
    }};

    private static final HashMap<String, Object> NO_STATUS_INPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("not_status", "Just saying something");
    }};

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying something");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap<String, Object> WITH_A_TAG_EXPECTED = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying #something");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap<String, Object> WITH_A_MENTION_EXPECTED = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying something to @jexp");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap<String, Object> WITH_A_PROMOTES_EXPECTED = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("status", "Just telling you to buy $mystuff");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap<String, Object> WITH_SILVER_EXPECTED = new HashMap<>() {{
        put("username", "laexample");
        put("status", "Just saying something");
        put("name", "Luke Gannon");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("silver", true);
    }};
}

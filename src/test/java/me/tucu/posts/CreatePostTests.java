package me.tucu.posts;

import me.tucu.Exceptions;
import me.tucu.fixtures.Nodes;
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
import static me.tucu.fixtures.Nodes.*;
import static me.tucu.fixtures.Relationships.MAX_LIKES_POST_1_SILVER;
import static me.tucu.fixtures.Relationships.MAX_LIKES_POST_2_GOLD;
import static me.tucu.posts.PostExceptions.EMPTY_STATUS;
import static me.tucu.posts.PostExceptions.MISSING_STATUS;
import static me.tucu.schema.Properties.TIME;
import static me.tucu.users.UserExceptions.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class CreatePostTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Posts.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldCreatePost()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<String, Object>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldCreatePostWithATag()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", WITH_A_TAG_INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(WITH_A_TAG_EXPECTED));
        }
    }

    @Test
    void shouldCreatePostWithAMention()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", WITH_A_MENTION_INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(WITH_A_MENTION_EXPECTED));
        }
    }

    @Test
    void shouldCreatePostWithAPromotes()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", WITH_A_PROMOTES_INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(WITH_A_PROMOTES_EXPECTED));
        }
    }

    @Test
    void shouldNotCreatePostEmptyStatus()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", EMPTY_STATUS_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(EMPTY_STATUS.value));
        }
    }

    @Test
    void shouldNotCreatePostMissingStatus()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", NO_STATUS_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(MISSING_STATUS.value));
        }
    }

    @Test
    void shouldNotCreatePostUserNotThere()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", USER_NOT_THERE_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreatePostUserIsBroke()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", USER_IS_BROKE_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(Exceptions.INSUFFICIENT_FUNDS.value));
        }
    }

    @Test
    void shouldNotCreatePostUserOwesGold()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", USER_OWES_GOLD_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(Exceptions.INSUFFICIENT_FUNDS.value));
        }
    }

    @Test
    void shouldNotCreatePostMissingUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", MISSING_USERNAME_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(MISSING_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreatePostBlankUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", BLANK_USERNAME_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(EMPTY_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreatePostInvalidUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", INVALID_USERNAME_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INVALID_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreatePostEmptyInput()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", EMPTY_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INVALID_INPUT.value));
        }
    }

    @Test
    void shouldNotCreatePostNullInput()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", NULL_INPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INVALID_INPUT.value));
        }
    }

    @Test
    void shouldCreatePostUserHasSilver()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.posts.create($parameters);",
                    parameters("parameters", WITH_SILVER_INPUT));

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

    private static final HashMap WITH_A_TAG_INPUT = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying #something");
    }};

    private static final HashMap WITH_A_MENTION_INPUT = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying something to @jexp");
    }};

    private static final HashMap WITH_A_PROMOTES_INPUT = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just telling you to buy $mystuff");
    }};

    private static final HashMap WITH_SILVER_INPUT = new HashMap<String, Object>() {{
        put("username", "laexample");
        put("status", "Just saying something");
    }};

    private static final HashMap USER_OWES_GOLD_INPUT = new HashMap<String, Object>() {{
        put("username", "markhneedham");
        put("status", "Just saying something");
    }};

    private static final HashMap EMPTY_STATUS_INPUT = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "");
    }};

    private static final HashMap MISSING_USERNAME_INPUT = new HashMap<String, Object>() {{
        put("status", "some status");
    }};

    private static final HashMap BLANK_USERNAME_INPUT = new HashMap<String, Object>() {{
        put("username", "");
        put("status", "some status");
    }};

    private static final HashMap INVALID_USERNAME_INPUT = new HashMap<String, Object>() {{
        put("username", "1");
        put("status", "some status");
    }};

    private static final HashMap EMPTY_INPUT = new HashMap<String, Object>() {{ }};

    private static final HashMap NULL_INPUT = null;

    private static final HashMap USER_NOT_THERE_INPUT = new HashMap<String, Object>() {{
        put("username", "not_there");
        put("status", "Just saying something");
    }};

    private static final HashMap USER_IS_BROKE_INPUT = new HashMap<String, Object>() {{
        put("username", "jexp");
        put("status", "Cannot say this because I a broke");
    }};

    private static final HashMap NO_STATUS_INPUT = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("not_status", "Just saying something");
    }};

    private static final HashMap EXPECTED = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying something");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap WITH_A_TAG_EXPECTED = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying #something");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap WITH_A_MENTION_EXPECTED = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just saying something to @jexp");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap WITH_A_PROMOTES_EXPECTED = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("status", "Just telling you to buy $mystuff");
        put("name", "Max De Marzi");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("gold", true);
    }};

    private static final HashMap WITH_SILVER_EXPECTED = new HashMap<String, Object>() {{
        put("username", "laexample");
        put("status", "Just saying something");
        put("name", "Luke Gannon");
        put("hash","0bd90aeb51d5982062f4f303a62df935");
        put("reposts", 0L);
        put("likes", 0L);
        put("silver", true);
    }};

    private static final String FIXTURE =
            Nodes.MAX + Nodes.JEXP + Nodes.LAEG + Nodes.MARK + Nodes.JERK +
                    PRODUCT +
                    POST1_0401 +
                    POST2_0412 +
                    POST3_0413 +
                    "CREATE (jexp)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.000+0100') }]->(post2)" +
                    "CREATE (max)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:21:42.123+0100') }]->(post3)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2020_04_12 {time: datetime('2020-04-12T12:33:00.556+0100')}]->(post1)" +
                    "CREATE (max)-[:SELLS]->(product)" +
                    MAX_LIKES_POST_1_SILVER +
                    MAX_LIKES_POST_2_GOLD +
                    "CREATE (jexp)-[:LIKES {time: datetime(), silver:true }]->(post2)" ;
}

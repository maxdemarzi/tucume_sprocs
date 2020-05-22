package me.tucu.users;

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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class GetProfileTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Users.class)
                .withFixture(Graph.getGraph())
                .build();
    }

    @Test
    void shouldProfileUser()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.profile($username);",
                    parameters("username", "maxdemarzi"));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<String, Object>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldGetProfileSecondUser()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.profile($username, $username2);",
                    parameters("username", "maxdemarzi","username2", "jexp"));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(EXPECTED2));
        }
    }

    @Test
    void shouldGetProfileSecondUserSame()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.profile($username, $username2);",
                    parameters("username", "maxdemarzi","username2", "maxdemarzi"));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<String, Object>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldNotProfileUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.profile($username);",
                    parameters("username", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotGetProfileSecondUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.profile($username, $username2);",
                    parameters("username", "maxdemarzi","username2", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("posts", 6L);
        put("likes", 2L);
        put("followers", 2L);
        put("following", 4L);
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
    }};

    private static final HashMap<String, Object> EXPECTED2 = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("name", "Max De Marzi");
        put("posts", 6L);
        put("likes", 2L);
        put("followers", 2L);
        put("following", 4L);
        put("i_follow", true);
        put("follows_me", true);
        put("followers_you_know_count", 2L);
        put("followers_you_know", new ArrayList<HashMap<String, Object>>(){{
            add(new HashMap<>() {{
                put("name", "Mark Needham");
                put("username", "markneedham");
                put("hash", "0bd90aeb51d5982062f4f303a62df935");
                put("time", ZonedDateTime.parse("2020-04-01T00:01+01:00"));
            }});
            add(new HashMap<>() {{
                put("name", "Stefan Armbruster");
                put("username", "darthvader42");
                put("hash", "0bd90aeb51d5982062f4f303a62df935");
                put("time", ZonedDateTime.parse("2020-04-01T00:01+01:00"));
            }});
        }});
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
    }};
}

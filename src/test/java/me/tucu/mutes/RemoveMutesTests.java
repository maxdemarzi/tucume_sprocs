package me.tucu.mutes;

import me.tucu.fixtures.Users;
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

import static me.tucu.mutes.MuteExceptions.NOT_MUTED;
import static me.tucu.mutes.MuteExceptions.SELF_UNMUTE;
import static me.tucu.schema.Properties.TIME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class RemoveMutesTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Mutes.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldRemoveMutes()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.mutes.remove($username, $username2);",
                    parameters("username", "jexp","username2", "maxdemarzi"));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, equalTo(EXPECTED.get(2)));
        }
    }

    @Test
    void shouldNotRemoveMutesNotMuted()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.mutes.remove($username, $username2);",
                    parameters("username", "maxdemarzi","username2", "jexp"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(NOT_MUTED.value));
        }
    }

    @Test
    void shouldNotRemoveMutesNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.mutes.remove($username, $username2);",
                    parameters("username", "not_there","username2", "jexp"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotRemoveMutesNotFound2()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.mutes.remove($username, $username2);",
                    parameters("username", "maxdemarzi","username2", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotRemoveMutesYourself()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.mutes.remove($username, $username2);",
                    parameters("username", "maxdemarzi","username2", "maxdemarzi"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(SELF_UNMUTE.value));
        }
    }

    private static final String FIXTURE =
            Users.MAX + Users.JEXP + Users.LUKE + Users.MARK + Users.JERK +
                    "CREATE (max)<-[:MUTES {time:datetime() - duration('P7D') }]-(jexp)" +
                    "CREATE (max)<-[:MUTES {time:datetime()  }]-(laeg)";

    private static final ArrayList<Map<String, Object>> EXPECTED = new ArrayList<>() {{
        add(new HashMap<>() {{
            put("username", "laexample");
            put("name", "Luke Gannon");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
        }});
        add(new HashMap<>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
        }});
        add(new HashMap<>() {{
            put("username", "maxdemarzi");
            put("name", "Max De Marzi");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
        }});
    }};
}

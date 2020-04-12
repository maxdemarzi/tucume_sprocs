package me.tucu.users;

import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class GetUserTests {

    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Users.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldGetUser()
    {
        neo4j.defaultDatabaseService();
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.get($username);",
                    parameters("username", "maxdemarzi"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(EXPECTED));
        }
    }

    @Test
    void shouldNotGetUserNotFound()
    {
        neo4j.defaultDatabaseService();
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.get($username);",
                    parameters("username", "not_there"));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.USER_NOT_FOUND.value));
        }
    }

    private static final HashMap EXPECTED = new HashMap<String, Object>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
        put("silver", 299L);
        put("gold", 0L);
    }};


    private static final String FIXTURE =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'maxdemarzi@hotmail.com', " +
                    "name: 'Max De Marzi'," +
                    "password: 'swordfish'," +
                    "silver:299," +
                    "gold:0})";
}

package me.tucu.users;

import me.tucu.Exceptions;
import me.tucu.fixtures.Graph;
import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static me.tucu.schema.Properties.TIME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class CreateUserTests {

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
    void shouldCreateUser()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", INPUT));

            // Then I should get what I expect
            Map<String, Object> actual = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<String, Object>(actual);
            modifiable.remove(TIME);
            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldNotCreateUserInvalidInput()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", null));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(Exceptions.INVALID_INPUT.value));
        }
    }

    @Test
    void shouldNotCreateUserMissingUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", MISSINGUSERNAMEINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.MISSING_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreateUserEmptyUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", EMPTYUSERNAMEINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.EMPTY_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreateUserInvalidUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", INVALIDUSERNAMEINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.INVALID_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreateUserMissingEmail()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", MISSINGEMAILINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.MISSING_EMAIL.value));
        }
    }

    @Test
    void shouldNotCreateUserEmptyEmail()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", EMPTYEMAILINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.EMPTY_EMAIL.value));
        }
    }

    @Test
    void shouldNotCreateUserInvalidEmail()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", INVALIDEMAILINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.INVALID_EMAIL.value));
        }
    }

    @Test
    void shouldNotCreateUserMissingName()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", MISSINGNAMEINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.MISSING_NAME.value));
        }
    }

    @Test
    void shouldNotCreateUserEmptyName()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", EMPTYNAMEINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.EMPTY_NAME.value));
        }
    }

    @Test
    void shouldNotCreateUserInvalidName()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", INVALIDNAMEINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.INVALID_NAME.value));
        }
    }

    @Test
    void shouldNotCreateUserMissingPassword()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", MISSINGPASSWORDINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.MISSING_PASSWORD.value));
        }
    }

    @Test
    void shouldNotCreateUserEmptyPassword()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", EMPTYPASSWORDINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.EMPTY_PASSWORD.value));
        }
    }

    @Test
    void shouldNotCreateUserInvalidPassword()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", INVALIDPASSWORDINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.INVALID_PASSWORD.value));
        }
    }

    @Test
    void shouldNotCreateUserExistingUsername()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", EXISTINGUSERNAMEINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.EXISTING_USERNAME.value));
        }
    }

    @Test
    void shouldNotCreateUserExistingEmail()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.users.create($parameters);",
                    parameters("parameters", EXISTINGEMAILINPUT));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(UserExceptions.EXISTING_EMAIL.value));
        }
    }

    private static final HashMap<String, Object> INPUT = new HashMap<>() {{
        put("username", "new_user");
        put("email", "new_user@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
    }};

    private static final HashMap<String, Object> MISSINGUSERNAMEINPUT = new HashMap<>() {{
        put("not_username", "maxdemarzi");
    }};

    private static final HashMap<String, Object> EMPTYUSERNAMEINPUT = new HashMap<>() {{
        put("username", "");
    }};

    private static final HashMap<String, Object> INVALIDUSERNAMEINPUT = new HashMap<>() {{
        put("username", " has spaces ");
    }};

    private static final HashMap<String, Object> MISSINGEMAILINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("not_email", "maxdemarzi@hotmail.com");
    }};

    private static final HashMap<String, Object> EMPTYEMAILINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "");
    }};

    private static final HashMap<String, Object> INVALIDEMAILINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "not an email address");
    }};

    private static final HashMap<String, Object> MISSINGNAMEINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
    }};

    private static final HashMap<String, Object> EMPTYNAMEINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "");
    }};

    private static final HashMap<String, Object> INVALIDNAMEINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "asdfafdasfafasdfasdfasfasfasfasdfasfdasdfasdfasdfasfasdfasdfasfafadfasdfasfafasdfasdfasdfdasfasdfadsfasdfasfasdfasdf");
    }};

    private static final HashMap<String, Object> MISSINGPASSWORDINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
    }};

    private static final HashMap<String, Object> EMPTYPASSWORDINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "");
    }};

    private static final HashMap<String, Object> INVALIDPASSWORDINPUT = new HashMap<>() {{
        put("username", "maxdemarzi");
        put("email", "maxdemarzi@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "123");
    }};

    private static final HashMap<String, Object> EXISTINGUSERNAMEINPUT = new HashMap<>() {{
        put("username", "jexp");
        put("email", "michael@hotmail.com");
        put("name", "Michael Hunger");
        put("password", "password");
    }};

    private static final HashMap<String, Object> EXISTINGEMAILINPUT = new HashMap<>() {{
        put("username", "jexp2");
        put("email", "michael@neo4j.com");
        put("name", "Michael Hunger");
        put("password", "password");
    }};

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
        put("username", "new_user");
        put("email", "new_user@hotmail.com");
        put("name", "Max De Marzi");
        put("password", "swordfish");
        put("hash", "3843e8863d8f988fb82b106262ce1587");
        put("silver", 99L);
        put("gold", 0L);
    }};
}

package me.tucu.likes;

import me.tucu.fixtures.Nodes;
import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static me.tucu.Exceptions.INSUFFICIENT_FUNDS;
import static me.tucu.fixtures.Relationships.*;
import static me.tucu.likes.LikesExceptions.ALREADY_LIKES;
import static me.tucu.posts.PostExceptions.POST_NOT_FOUND;
import static me.tucu.schema.Properties.LIKED_TIME;
import static me.tucu.schema.Properties.TIME;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class CreateLikesTests {

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
    void shouldCreateLikes()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.create($username, $post_id);",
                    parameters("username", "laexample", "post_id", 4));

            // Then I should get what I expect
            Map<String, Object> record = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(record);
            modifiable.remove(TIME);
            modifiable.remove(LIKED_TIME);

            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldNotCreateLikesUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.create($username, $post_id);",
                    parameters("username", "not_there", "post_id", 4));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreateLikesPostNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.create($username, $post_id);",
                    parameters("username", "laexample", "post_id", 400));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreateLikesPostIdDoesNotBelongToPost()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.create($username, $post_id);",
                    parameters("username", "laexample", "post_id", 1));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotCreateLikesAlreadyLiked()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.create($username, $post_id);",
                    parameters("username", "maxdemarzi", "post_id", 4));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(ALREADY_LIKES.value));
        }
    }

    @Test
    void shouldNotCreateLikesBroke()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.create($username, $post_id);",
                    parameters("username", "jexp", "post_id", 6));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INSUFFICIENT_FUNDS.value));
        }
    }

    @Test
    void shouldNotCreateLikesBrokeToo()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.likes.create($username, $post_id);",
                    parameters("username", "markhneedham", "post_id", 6));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INSUFFICIENT_FUNDS.value));
        }
    }

    private static final String FIXTURE =
            Nodes.MAX + Nodes.JEXP + Nodes.LAEG + Nodes.MARK +
            Nodes.POST1_0401 + Nodes.POST2_0412 + Nodes.POST3_0413 +
            JEXP_POSTED_POST_1 +
            LAEG_POSTED_POST_2 +
            MAX_POSTED_POST_3 +
            LAEG_REPOSTED_POST_1 +
            MAX_LIKES_POST_1_SILVER +
            MAX_LIKES_POST_2_GOLD +
            JEXP_LIKES_POST_2_SILVER ;

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
            put("username", "jexp");
            put("name", "Michael Hunger");
            put("hash", "0bd90aeb51d5982062f4f303a62df935");
            put("status", "Hello World!");
            put("likes", 2L);
            put("silver", true);
            put("reposts", 1L);
            put("liked", true);
            put("reposted", true);
        }};

}

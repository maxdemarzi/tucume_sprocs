package me.tucu.likes;

import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static me.tucu.Exceptions.INSUFFICIENT_FUNDS;
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
            "CREATE (max:User {username:'maxdemarzi', " +
            "email: 'max@neo4j.com', " +
            "name: 'Max De Marzi'," +
            "hash: '0bd90aeb51d5982062f4f303a62df935'," +
            "password: 'swordfish'," +
            "silver: 0," +
            "gold: 10}) " +
            "CREATE (jexp:User {username:'jexp', " +
            "email: 'michael@neo4j.com', " +
            "hash: '0bd90aeb51d5982062f4f303a62df935'," +
            "name: 'Michael Hunger'," +
            "password: 'tunafish'," +
            "silver: 0," +
            "gold: 0}) " +
            "CREATE (laeg:User {username:'laexample', " +
            "email: 'luke@neo4j.com', " +
            "name: 'Luke Gannon'," +
            "hash: '0bd90aeb51d5982062f4f303a62df935'," +
            "password: 'cuddlefish'," +
            "silver: 299," +
            "gold: -10}) " +
            "CREATE (mark:User {username:'markhneedham', " +
            "email: 'mark@neo4j.com', " +
            "name: 'Mark Needham'," +
            "password: 'jellyfish'," +
            "silver: 299," +
            "gold: -999})" +
            "CREATE (post1:Post {status:'Hello World!', " +
            "time: datetime('2020-04-01T12:44:08.556+0100')})" +
            "CREATE (post2:Post {status:'How are you!', " +
            "time: datetime('2020-04-12T11:50:35.000+0100')})" +
            "CREATE (post3:Post {status:'Cannot like me!', " +
            "time: datetime('2020-04-13T09:21:42.123+0100')})" +
            "CREATE (jexp)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
            "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.000+0100') }]->(post2)" +
            "CREATE (max)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:21:42.123+0100') }]->(post3)" +
            "CREATE (laeg)-[:REPOSTED_ON_2020_04_12 {time: datetime('2020-04-12T12:33:00.556+0100')}]->(post1)" +
            "CREATE (max)-[:LIKES {time: datetime() - duration('P7D'), silver:true }]->(post1)" +
            "CREATE (max)-[:LIKES {time: datetime(), gold:true }]->(post2)" +
            "CREATE (jexp)-[:LIKES {time: datetime(), silver:true }]->(post2)" ;

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

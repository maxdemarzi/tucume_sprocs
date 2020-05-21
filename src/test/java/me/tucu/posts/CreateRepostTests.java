package me.tucu.posts;

import me.tucu.fixtures.Nodes;
import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static me.tucu.fixtures.Nodes.*;
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
                .withFixture(FIXTURE)
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
                    parameters("username", "laexample", "post_id", 5));

            // Then I should get what I expect
            Map<String, Object> record = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(record);
            modifiable.remove(TIME);

            assertThat(modifiable, is(EXPECTED));
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
                    parameters("username", "maxdemarzi", "post_id", 9));

            // Then I should get what I expect
            // Then I should get what I expect
            Map<String, Object> record = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(record);
            modifiable.remove(TIME);

            assertThat(modifiable, is(EXPECTED5));
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
                    parameters("username", "jexp", "post_id", 9));

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

    private static final String FIXTURE =
            Nodes.MAX + Nodes.JEXP + Nodes.LAEG + Nodes.MARK + Nodes.JERK +
                    POST1_0401 +
                    POST2_0412 +
                    POST3_0413 +
                    PRODUCT +
                    "CREATE (max)-[:SELLS]->(product)" +
                    "CREATE (post5:Post {status:'Please buy $mystuff', " +
                    "time: datetime('2020-05-02T04:33:52.000+0100')})" +
                    "CREATE (post5)-[:PROMOTES]->(product)" +
                    "CREATE (jexp)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.000+0100') }]->(post2)" +
                    "CREATE (max)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:21:42.123+0100') }]->(post3)" +
                    "CREATE (laeg)-[:REPOSTED_ON_2020_04_12 {time: datetime('2020-04-12T12:33:00.556+0100'), silver:true}]->(post3)" +
                    "CREATE (jexp)-[:LIKES {time: datetime(), silver:true }]->(post2)" +
                    "CREATE (laeg)-[:POSTED_ON_2020_05_02 {time: datetime('2020-05-02T04:33:52.000+0100') }]->(post5)" ;

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
        put("username", "jexp");
        put("name", "Michael Hunger");
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
        put("status", "Hello World!");
        put("likes", 0L);
        put("silver", true);
        put("reposts", 1L);
        put("liked", false);
        put("reposted", true);
    }};

    private static final HashMap<String, Object> EXPECTED5 = new HashMap<>() {{
        put("username", "laexample");
        put("name", "Luke Gannon");
        put("hash", "0bd90aeb51d5982062f4f303a62df935");
        put("status", "Please buy $mystuff");
        put("likes", 0L);
        put("gold", true);
        put("reposts", 1L);
        put("liked", false);
        put("reposted", true);
    }};
}

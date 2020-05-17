package me.tucu.products;

import me.tucu.schema.Schema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.HashMap;
import java.util.Map;

import static me.tucu.Exceptions.INSUFFICIENT_FUNDS;
import static me.tucu.posts.PostExceptions.POST_NOT_FOUND;
import static me.tucu.schema.Properties.TIME;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.Values.parameters;

public class PurchaseProductTests {
    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Schema.class)
                .withProcedure(Products.class)
                .withFixture(FIXTURE)
                .build();
    }

    @Test
    void shouldPurchaseProductFromSeller()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.products.purchase($username, $post_id);",
                    parameters("username", "laexample", "post_id", 7));

            // Then I should get what I expect
            Map<String, Object> record = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(record);
            modifiable.remove(TIME);

            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldPurchaseProductFromMarketer()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.products.purchase($username, $post_id);",
                    parameters("username", "markneedham", "post_id", 9));

            // Then I should get what I expect
            Map<String, Object> record = result.single().get("value").asMap();
            HashMap<String, Object> modifiable = new HashMap<>(record);
            modifiable.remove(TIME);

            assertThat(modifiable, is(EXPECTED));
        }
    }

    @Test
    void shouldNotPurchaseProductUserNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.products.purchase($username, $post_id);",
                    parameters("username", "not_there", "post_id", 4));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(USER_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotPurchaseProductPostNotFound()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.products.purchase($username, $post_id);",
                    parameters("username", "laexample", "post_id", 400));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotPurchaseProductPostIdDoesNotBelongToPost()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.products.purchase($username, $post_id);",
                    parameters("username", "laexample", "post_id", 1));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(POST_NOT_FOUND.value));
        }
    }

    @Test
    void shouldNotPurchaseProductBroke()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run( "CALL me.tucu.products.purchase($username, $post_id);",
                    parameters("username", "jexp", "post_id", 7));

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
                    "gold: 1000}) " +
                    "CREATE (mark:User {username:'markneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "name: 'Mark Needham'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'jellyfish'," +
                    "silver: 299," +
                    "gold: 10000})" +
                    "CREATE (product:Product {id: 'mystuff', name:'My Stuff', price: 1000, time: datetime('2020-04-23T01:38:22.000+0100')} )" +
                    "CREATE (max)-[:SELLS]->(product)" +
                    "CREATE (post1:Post {status:'Hello World!', " +
                    "time: datetime('2020-04-01T12:44:08.556+0100')})" +
                    "CREATE (post2:Post {status:'How are you!', " +
                    "time: datetime('2020-04-12T11:50:35.000+0100')})" +
                    "CREATE (post3:Post {status:'Please buy $mystuff', " +
                    "time: datetime('2020-04-13T09:21:42.123+0100')})" +
                    "CREATE (jexp)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" +
                    "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.000+0100') }]->(post2)" +
                    "CREATE (max)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:21:42.123+0100') }]->(post3)" +
                    "CREATE (post3)-[:PROMOTES]->(product)" +
                    "CREATE (repost1:Post {post_id: 7, username:'laexample', time: datetime('2020-04-12T12:33:00.556+0100')})" +
                    "CREATE (laeg)-[:REPOSTED_ON_2020_04_12 { time: datetime('2020-04-12T12:33:00.556+0100') }]->(repost1)" +
                    "CREATE (repost1)-[:REPOSTED]->(post3)" +
                    "CREATE (repost2:Post {post_id: 7, username:'jexp', time: datetime('2020-04-12T14:49:00.556+0100')})" +
                    "CREATE (jexp)-[:REPOSTED_ON_2020_04_12 { time: datetime('2020-04-12T14:49:00.556+0100') }]->(repost2)" +
                    "CREATE (repost2)-[:REPOSTED]->(repost1)";

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
        put("id", "mystuff");
        put("name", "My Stuff");
        put("price", 1000L);
    }};
}

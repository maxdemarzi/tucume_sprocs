package me.tucu.products;

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
import static me.tucu.fixtures.Nodes.*;
import static me.tucu.fixtures.Relationships.*;
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
                    parameters("username", "rich", "post_id", 8));

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
                    parameters("username", "rich", "post_id", 10));

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
                    parameters("username", "jexp", "post_id", 8));

            // Then I should get what I expect
            assertThat(result.single().get("value").asMap(), equalTo(INSUFFICIENT_FUNDS.value));
        }
    }

    private static final String FIXTURE =
            Nodes.MAX + Nodes.JEXP + Nodes.LAEG + Nodes.MARK + Nodes.RICH +
                    PRODUCT +
                    MAX_SELLS_PRODUCT +
                    POST1_0401 +
                    POST2_0412 +
                    POST5_0502 +
                    JEXP_POSTED_POST_1 +
                    LAEG_POSTED_POST_2 +
                    MAX_POSTED_POST_5 +
                    POST_5_PROMOTES_PRODUCT +
                    "CREATE (repost1:Post {post_id: 7, username:'laexample', time: datetime('2020-04-12T12:33:00.556+0100')})" +
                    "CREATE (laeg)-[:REPOSTED_ON_2020_04_12 { time: datetime('2020-04-12T12:33:00.556+0100') }]->(repost1)" +
                    "CREATE (repost1)-[:REPOSTED]->(post5)" +
                    "CREATE (repost2:Post {post_id: 7, username:'jexp', time: datetime('2020-04-12T14:49:00.556+0100')})" +
                    "CREATE (jexp)-[:REPOSTED_ON_2020_04_12 { time: datetime('2020-04-12T14:49:00.556+0100') }]->(repost2)" +
                    "CREATE (repost2)-[:REPOSTED]->(repost1)";

    private static final HashMap<String, Object> EXPECTED = new HashMap<>() {{
        put("id", "mystuff");
        put("name", "My Stuff");
        put("price", 1000L);
    }};
}

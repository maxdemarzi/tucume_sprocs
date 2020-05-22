package me.tucu.fixtures;

public class Nodes {

    public static final String MAX =
            "CREATE (max:User {username:'maxdemarzi', " +
                    "email: 'max@neo4j.com', " +
                    "name: 'Max De Marzi'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'swordfish', " +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 0," +
                    "gold: 10}) " ;

    public static final String JEXP =
            "CREATE (jexp:User {username:'jexp', " +
                    "email: 'michael@neo4j.com', " +
                    "name: 'Michael Hunger'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'tunafish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 0," +
                    "gold: 0})" ;

    public static final String LAEG =
            "CREATE (laeg:User {username:'laexample', " +
                    "email: 'luke@neo4j.com', " +
                    "name: 'Luke Gannon'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'cuddlefish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 299," +
                    "gold: -10}) ";

    public static final String MARK =
            "CREATE (mark:User {username:'markneedham', " +
                    "email: 'mark@neo4j.com', " +
                    "name: 'Mark Needham'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'jellyfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 299," +
                    "gold: -999})";

    public static final String JERK =
            "CREATE (jerk:User {username:'jerk', " +
                    "email: 'jerk@neo4j.com', " +
                    "hash: 'some hash'," +
                    "name: 'Some Jerk'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'catfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 0," +
                    "gold: -999})";

    public static final String RICH =
            "CREATE (rich:User {username:'rich', " +
                    "email: 'rich@neo4j.com', " +
                    "hash: 'some hash'," +
                    "name: 'Rich Person'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'arowana'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 0," +
                    "gold: 99999})";

    public static final String STEFAN =
            "CREATE (stefan:User {username:'darthvader42', " +
                    "email: 'stefan@neo4j.com', " +
                    "name: 'Stefan Armbruster'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'catfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 299," +
                    "gold:0})";

    public static final String HELLO_USER =
            "CREATE (hello:User {username:'hello_there', " +
                    "email: 'hello_there@neo4j.com', " +
                    "hash: 'some hash'," +
                    "name: 'hello there user'," +
                    "hash: '0bd90aeb51d5982062f4f303a62df935'," +
                    "password: 'bluefish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })";

    public static final String POST1_0401 =
           "CREATE (post1:Post {status:'Hello World!', " +
                   "time: datetime('2020-04-01T12:44:08.556+0100')})" ;

    public static final String POST2_0412 =
           "CREATE (post2:Post {status:'How are you! #neo4j #neo4j', " +
                   "time: datetime('2020-04-12T11:50:35.000+0100')})";

    public static final String POST3_0413 = // Node 10
           "CREATE (post3:Post {status:'Cannot like me! But like #graphs', " +
                   "time: datetime('2020-04-13T09:21:42.123+0100')})" ;

    public static final String POST4_0413 =
            "CREATE (post4:Post {status:'I think @jexp sucks but hello', " +
                    "time: datetime('2020-04-14T09:53:23.000+0100')})";

    public static final String POST5_0502 =
            "CREATE (post5:Post {status:'Please buy $mystuff', " +
                    "time: datetime('2020-05-02T04:33:52.000+0100')})";

    public static final String POST6_0401 =
            "CREATE (post6:Post {status:'Hello @jexp', " +
                    "time: datetime('2020-04-01T12:44:08.556+0100')})";

    public static final String POST7_0401 =
            "CREATE (post7:Post {status:'I like #neo4j but I am biased.', " +
            "time: datetime('2020-04-01T12:44:08.556+0100')})";

    public static final String POST8_0401 =
            "CREATE (post8:Post {status:'Lowercase #hello', " +
                    "time: datetime('2020-04-01T12:44:08.556+0100')})";

    public static final String PRODUCT =
            "CREATE (product:Product {id: 'mystuff', name:'My Stuff', price: 1000, " +
                    "time: datetime('2020-04-23T01:38:22.000+0100')} )";

    public static final String HELLO_PRODUCT =
            "CREATE (hello_product:Product {id:'hello_product', name:'hello', " +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })" ;

    public static final String HELLO_TAG =
            "CREATE (tag:Tag {name:'hello', " +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })";

    public static final String NEO4J_TAG =
            "CREATE (neo4j:Tag {name:'neo4j', " +
                    "time: datetime('2020-04-01T11:44:08.556+0100')})";

    public static final String GRAPHS_TAG =
            "CREATE (graphs:Tag {name:'graphs', " +
                    "time: datetime('2020-04-13T04:20:12.000+0100')})";

}

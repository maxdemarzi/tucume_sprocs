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
            "CREATE (mark:User {username:'markhneedham', " +
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
                    "password: 'catfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 0," +
                    "gold: -999})";

    public static final String RICH =
            "CREATE (rich:User {username:'rich', " +
                    "email: 'rich@neo4j.com', " +
                    "hash: 'some hash'," +
                    "name: 'Rich Person'," +
                    "password: 'arowana'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 0," +
                    "gold: 99999})";

    public static final String STEFAN =
            "CREATE (stefan:User {username:'darthvader42', " +
                    "email: 'stefan@neo4j.com', " +
                    "name: 'Stefan Armbruster'," +
                    "password: 'catfish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100'), " +
                    "silver: 299," +
                    "gold:0})";

    public static final String HELLO_USER =
            "CREATE (hello:User {username:'hello_there', " +
                    "email: 'hello_there@neo4j.com', " +
                    "hash: 'some hash'," +
                    "name: 'hello there user'," +
                    "password: 'bluefish'," +
                    "time: datetime('2020-04-01T00:01:00.000+0100') })";

    public static final String POST1_0401 =
           "CREATE (post1:Post {status:'Hello World!', " +
                   "time: datetime('2020-04-01T12:44:08.556+0100')})" ;

    public static final String POST2_0412 =
           "CREATE (post2:Post {status:'How are you!', " +
                   "time: datetime('2020-04-12T11:50:35.000+0100')})";

    public static final String POST3_0413 =
           "CREATE (post3:Post {status:'Cannot like me!', " +
                   "time: datetime('2020-04-13T09:21:42.123+0100')})" ;

    public static final String POST4_0413 =
            "CREATE (post4:Post {status:'I think @jexp sucks but hello', " +
                    "time: datetime('2020-04-14T09:53:23.000+0100')})";

    public static final String POST5_0502 =
            "CREATE (post5:Post {status:'Please buy $mystuff', " +
                    "time: datetime('2020-05-02T04:33:52.000+0100')})";

    public static final String PRODUCT =
            "CREATE (product:Product {id: 'mystuff', name:'My Stuff', price: 1000, " +
                    "time: datetime('2020-04-23T01:38:22.000+0100')} )";
}

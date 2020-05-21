package me.tucu.fixtures;

public class Users {

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

    public static final String LUKE =
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

}

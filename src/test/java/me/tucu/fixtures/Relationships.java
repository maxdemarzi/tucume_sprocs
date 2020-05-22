package me.tucu.fixtures;

public class Relationships {
    public static final String MAX_FOLLOWED_BY_JEXP =
    "CREATE (max)<-[:FOLLOWS {time:datetime() - duration('P7D') }]-(jexp)";

    public static final String MAX_FOLLOWED_BY_LAEG =
           "CREATE (max)<-[:FOLLOWS {time:datetime() }]-(laeg)";

    public static final String MAX_FOLLOWS_JEXP =
            "CREATE (max)-[:FOLLOWS {time:datetime() - duration('P7D') }]->(jexp)";

    public static final String MAX_FOLLOWS_LAEG =
            "CREATE (max)-[:FOLLOWS {time:datetime() }]->(laeg)";

    public static final String MAX_FOLLOWS_STEFAN =
            "CREATE (max)-[:FOLLOWS {time:datetime() }]->(stefan)";

    public static final String MAX_FOLLOWS_MARK =
            "CREATE (max)-[:FOLLOWS {time:datetime() }]->(mark)";

    public static final String JEXP_FOLLOWS_STEFAN =
            "CREATE (jexp)-[:FOLLOWS {time:datetime() }]->(stefan)";

    public static final String JEXP_FOLLOWS_MARK =
            "CREATE (jexp)-[:FOLLOWS {time:datetime() }]->(mark)";

    public static final String MAX_MUTED_BY_JEXP =
            "CREATE (max)<-[:MUTES {time:datetime() - duration('P7D') }]-(jexp)";

    public static final String MAX_MUTED_BY_LAEG =
            "CREATE (max)<-[:MUTES {time:datetime() }]-(laeg)";

    public static final String MAX_MUTES_JEXP =
            "CREATE (max)-[:MUTES {time:datetime() - duration('P7D') }]->(jexp)";

    public static final String MAX_MUTES_LAEG =
            "CREATE (max)-[:MUTES {time:datetime() }]->(laeg)";

    public static final String MAX_LIKES_POST_1_SILVER =
            "CREATE (max)-[:LIKES {time: datetime() - duration('P7D'), silver:true }]->(post1)";

    public static final String MAX_LIKES_POST_2_GOLD =
            "CREATE (max)-[:LIKES {time: datetime(), gold:true }]->(post2)";

    public static final String JEXP_POSTED_POST_1 =
            "CREATE (jexp)-[:POSTED_ON_2020_04_01 {time: datetime('2020-04-01T12:44:08.556+0100') }]->(post1)" ;

    public static final String LAEG_POSTED_POST_2 =
            "CREATE (laeg)-[:POSTED_ON_2020_04_12 {time: datetime('2020-04-12T11:50:35.000+0100') }]->(post2)";

    public static final String MAX_POSTED_POST_3 =
            "CREATE (max)-[:POSTED_ON_2020_04_13 {time: datetime('2020-04-13T09:21:42.123+0100') }]->(post3)";

    public static final String JERK_POSTED_POST_4 =
            "CREATE (jerk)-[:POSTED_ON_2020_04_14 {time: datetime('2020-04-14T09:53:23.000+0100') }]->(post4)";

    public static final String MAX_POSTED_POST_5 =
            "CREATE (max)-[:POSTED_ON_2020_05_02 {time: datetime('2020-05-02T04:33:52.000+0100') }]->(post5)";

    public static final String LAEG_REPOSTED_POST_1 =
            "CREATE (laeg)-[:REPOSTED_ON_2020_04_12 {time: datetime('2020-04-12T12:33:00.556+0100')}]->(post1)";

    public static final String JEXP_LIKES_POST_2_SILVER =
            "CREATE (jexp)-[:LIKES {time: datetime(), silver:true }]->(post2)";

    public static final String MAX_SELLS_PRODUCT =
            "CREATE (max)-[:SELLS]->(product)";

    public static final String POST_5_PROMOTES_PRODUCT =
            "CREATE (post5)-[:PROMOTES]->(product)";
}

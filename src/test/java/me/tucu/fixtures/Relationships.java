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

}

package me.tucu.fixtures;

public class Posts {
    public static final String POST1_0401 =
               "CREATE (post1:Post {status:'Hello World!', " +
                       "time: datetime('2020-04-01T12:44:08.556+0100')})" ;
    public static final String POST2_0412 =
                       "CREATE (post2:Post {status:'How are you!', " +
                       "time: datetime('2020-04-12T11:50:35.000+0100')})";
    public static final String POST3_0413 =
                       "CREATE (post3:Post {status:'Cannot like me!', " +
                       "time: datetime('2020-04-13T09:21:42.123+0100')})" ;
}

package me.tucu.likes;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.reverseOrder;
import static me.tucu.posts.Posts.*;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.getUserAttributes;

public class Likes {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.likes.get", mode = Mode.READ)
    @Description("CALL me.tucu.likes.get(username, limit, since, username2)")
    public Stream<MapResult> getLikes(@Name(value = "username", defaultValue = "") String username,
                                      @Name(value = "limit", defaultValue = "25") Long limit,
                                      @Name(value = "since", defaultValue = "-1") Long since,
                                      @Name(value = "username2", defaultValue = "") String username2) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit =  abs(limit);

        ZonedDateTime dateTime;
        if (since == -1L) {
            dateTime = ZonedDateTime.now(Clock.systemUTC());
        } else {
            Instant i = Instant.ofEpochSecond(since);
            dateTime = ZonedDateTime.ofInstant(i, UTC);
        }

        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }

            // If a different user asked for the likes, add a few things
            Node user2 = null;
            if (!username2.isEmpty() && !username.equals(username2)) {
                user2 = tx.findNode(Labels.User, USERNAME, username2);
                if (user2 == null) {
                    return Stream.of(USER_NOT_FOUND);
                }
            }

            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                ZonedDateTime time = (ZonedDateTime)r1.getProperty(TIME);
                if(time.isBefore(dateTime)) {
                    Node post = r1.getEndNode();
                    Map<String, Object> properties = post.getAllProperties();
                    properties.put(LIKED_TIME, time);
                    Node author = getAuthor(post);
                    properties.put(USERNAME, author.getProperty(USERNAME));
                    properties.put(NAME, author.getProperty(NAME));
                    properties.put(HASH, author.getProperty(HASH));
                    properties.put(LIKES, post.getDegree(RelationshipTypes.LIKES));
                    properties.put(REPOSTS, getRepostedCount(post));
                    if (user2 != null) {
                        properties.put(LIKED, userLikesPost(user2, post));
                        properties.put(REPOSTED, userRepostedPost(tx, user2, post));
                    }
                    results.add(properties);
                }
            }

            tx.commit();
        }
        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

    public static boolean userLikesPost(Node user, Node post) {
        boolean alreadyLiked = false;
        if (user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING)
                < post.getDegree(RelationshipTypes.LIKES, Direction.INCOMING) ) {
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                if (r1.getEndNode().equals(post)) {
                    alreadyLiked = true;
                    break;
                }
            }
        } else {
            for (Relationship r1 : post.getRelationships(Direction.INCOMING, RelationshipTypes.LIKES)) {
                if (r1.getStartNode().equals(user)) {
                    alreadyLiked = true;
                    break;
                }
            }
        }
        return alreadyLiked;
    }
}

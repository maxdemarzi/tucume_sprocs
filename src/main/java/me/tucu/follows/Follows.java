package me.tucu.follows;

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
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.reverseOrder;
import static me.tucu.follows.FollowExceptions.*;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.getUserAttributes;

public class Follows {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.follows.followers", mode = Mode.READ)
    @Description("CALL me.tucu.follows.followers(username, limit, since)")
    public Stream<MapResult> getFollowers(@Name(value = "username", defaultValue = "") String username,
                                          @Name(value = "limit", defaultValue = "25") Long limit,
                                          @Name(value = "since", defaultValue = "-1") Long since) {
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
            for (Relationship r1: user.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                ZonedDateTime time = (ZonedDateTime)r1.getProperty(TIME);
                if(time.isBefore(dateTime)) {
                    Node follower = r1.getStartNode();
                    Map<String, Object> result = getUserAttributes(follower);
                    result.put(TIME, time);
                    results.add(result);
                }
            }

            tx.commit();
        }
        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

    @Procedure(name = "me.tucu.follows.following", mode = Mode.READ)
    @Description("CALL me.tucu.follows.following(username, limit, since)")
    public Stream<MapResult> getFollowing(@Name(value = "username", defaultValue = "") String username,
                                          @Name(value = "limit", defaultValue = "25") Long limit,
                                          @Name(value = "since", defaultValue = "-1") Long since) {
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
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                ZonedDateTime time = (ZonedDateTime)r1.getProperty(TIME);
                if(time.isBefore(dateTime)) {
                    Node following = r1.getEndNode();
                    Map<String, Object> result = getUserAttributes(following);
                    result.put(TIME, time);
                    results.add(result);
                }
            }

            tx.commit();
        }
        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

    @Procedure(name = "me.tucu.follows.create", mode = Mode.WRITE)
    @Description("CALL me.tucu.follows.create(username, username2)")
    public Stream<MapResult> createFollows(@Name(value = "username", defaultValue = "") String username,
                                           @Name(value = "username2", defaultValue = "") String username2) {
        // Can't follow yourself
        if (username.equals(username2)) {
            return Stream.of(SELF_FOLLOW);
        }

        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            Node user2 = tx.findNode(Labels.User, USERNAME, username2);
            if (user2 == null) {
                return Stream.of(USER_NOT_FOUND);
            }

            // Check to see if they are already followed from the node with the least FOLLOWS relationships
            HashSet<Node> followed = new HashSet<>();
            if (user.getDegree(RelationshipTypes.FOLLOWS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.FOLLOWS, Direction.INCOMING)) {
                for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS) ) {
                    followed.add(r1.getEndNode());
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                    followed.add(r1.getStartNode());
                }
            }

            if(followed.contains(user2)) {
                return Stream.of(ALREADY_FOLLOW);
            }

            Relationship follows = user.createRelationshipTo(user2, RelationshipTypes.FOLLOWS);
            follows.setProperty(TIME, ZonedDateTime.now());
            results = user2.getAllProperties();
            results.remove(EMAIL);
            results.remove(PASSWORD);
            results.remove(SILVER);
            results.remove(GOLD);
            tx.commit();
        }
        return Stream.of(new MapResult(results));
    }

    @Procedure(name = "me.tucu.follows.remove", mode = Mode.WRITE)
    @Description("CALL me.tucu.follows.remove(username, username2)")
    public Stream<MapResult> removeFollows(@Name(value = "username", defaultValue = "") String username,
                                           @Name(value = "username2", defaultValue = "") String username2) {
        // Can't unfollow yourself
        if (username.equals(username2)) {
            return Stream.of(SELF_UNFOLLOW);
        }

        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            Node user2 = tx.findNode(Labels.User, USERNAME, username2);
            if (user2 == null) {
                return Stream.of(USER_NOT_FOUND);
            }

            // Check to see if user is really following from the node with the least FOLLOWS relationships
            Relationship follows = null;
            if (user.getDegree(RelationshipTypes.FOLLOWS, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.FOLLOWS, Direction.INCOMING)) {
                for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS) ) {
                    if (r1.getEndNode().equals(user2)) {
                        follows = r1;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                    if (r1.getStartNode().equals(user)) {
                        follows = r1;
                        break;
                    }
                }
            }

            if (follows == null) {
                return Stream.of(NOT_FOLLOWING);
            }

            follows.delete();

            results = user2.getAllProperties();
            results.remove(EMAIL);
            results.remove(PASSWORD);
            results.remove(SILVER);
            results.remove(GOLD);
            tx.commit();
        }
        return Stream.of(new MapResult(results));
    }
}

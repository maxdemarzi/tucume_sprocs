package me.tucu.mutes;

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
import static me.tucu.mutes.MuteExceptions.*;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.getUserAttributes;

public class Mutes {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.mutes.get", mode = Mode.READ)
    @Description("CALL me.tucu.mutes.get(username, limit, since)")
    public Stream<MapResult> getMuted(@Name(value = "username", defaultValue = "") String username,
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
            for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                ZonedDateTime time = (ZonedDateTime)r1.getProperty(TIME);
                if(time.isBefore(dateTime)) {
                    Node muted = r1.getEndNode();
                    Map<String, Object> result = getUserAttributes(muted);
                    result.put(TIME, time);
                    results.add(result);
                }
            }

            tx.commit();
        }
        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

    @Procedure(name = "me.tucu.mutes.create", mode = Mode.WRITE)
    @Description("CALL me.tucu.mutes.create(username, username2)")
    public Stream<MapResult> createMutes(@Name(value = "username", defaultValue = "") String username,
                                           @Name(value = "username2", defaultValue = "") String username2) {
        // Can't mute yourself
        if (username.equals(username2)) {
            return Stream.of(SELF_MUTE);
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

            // Check to see if they are already muted from the node with the least MUTES relationships
            HashSet<Node> muted = new HashSet<>();
            if (user.getDegree(RelationshipTypes.MUTES, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.MUTES, Direction.INCOMING)) {
                for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES) ) {
                    muted.add(r1.getEndNode());
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.MUTES)) {
                    muted.add(r1.getStartNode());
                }
            }

            if(muted.contains(user2)) {
                return Stream.of(ALREADY_MUTED);
            }

            Relationship mutes = user.createRelationshipTo(user2, RelationshipTypes.MUTES);
            mutes.setProperty(TIME, ZonedDateTime.now());
            results = user2.getAllProperties();
            results.remove(EMAIL);
            results.remove(PASSWORD);
            results.remove(SILVER);
            results.remove(GOLD);
            tx.commit();
        }
        return Stream.of(new MapResult(results));
    }

    @Procedure(name = "me.tucu.mutes.remove", mode = Mode.WRITE)
    @Description("CALL me.tucu.mutes.remove(username, username2)")
    public Stream<MapResult> removeMutes(@Name(value = "username", defaultValue = "") String username,
                                           @Name(value = "username2", defaultValue = "") String username2) {
        // Can't unmute yourself
        if (username.equals(username2)) {
            return Stream.of(SELF_UNMUTE);
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

            // Check to see if user is really muted from the node with the least MUTES relationships
            Relationship mutes = null;
            if (user.getDegree(RelationshipTypes.MUTES, Direction.OUTGOING)
                    < user2.getDegree(RelationshipTypes.MUTES, Direction.INCOMING)) {
                for (Relationship r1: user.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES) ) {
                    if (r1.getEndNode().equals(user2)) {
                        mutes = r1;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : user2.getRelationships(Direction.INCOMING, RelationshipTypes.MUTES)) {
                    if (r1.getStartNode().equals(user)) {
                        mutes = r1;
                        break;
                    }
                }
            }

            if (mutes == null) {
                return Stream.of(NOT_MUTED);
            }

            mutes.delete();

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

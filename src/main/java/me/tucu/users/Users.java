package me.tucu.users;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.utils.Time.utc;

public class Users {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.users.get", mode = Mode.READ)
    @Description("CALL me.tucu.users.get(username)")
    public Stream<MapResult> usersGet(@Name(value = "username", defaultValue = "") String username) {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            results = user.getAllProperties();
        }
        return Stream.of(new MapResult(results));
    }

    @Procedure(name = "me.tucu.users.profile", mode = Mode.READ)
    @Description("CALL me.tucu.users.profile(username, username2)")
    public Stream<MapResult> usersProfile(@Name(value = "username", defaultValue = "") String username,
                                          @Name(value = "username2", defaultValue = "") String username2) {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) { return Stream.of(USER_NOT_FOUND); }

            results = getUserAttributes(user);

            // If a different user asked for the profile, add a few things
            if (!username2.isEmpty() && !username.equals(username2)) {
                Node user2 = tx.findNode(Labels.User, USERNAME, username2);
                if(user2 == null) { return Stream.of(USER_NOT_FOUND); }

                // Figure out if they follow me, or I follow them
                HashSet<Node> followed = new HashSet<>();
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                    followed.add(r1.getEndNode());
                }
                HashSet<Node> followed2 = new HashSet<>();
                for (Relationship r1 : user2.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                    followed2.add(r1.getEndNode());
                }

                boolean follows_me = followed.contains(user2);
                boolean i_follow = followed2.contains(user);
                results.put(I_FOLLOW, i_follow);
                results.put(FOLLOWS_ME, follows_me);

                // Figure out which of their followers I follow
                followed.retainAll(followed2);

                results.put(FOLLOWERS_YOU_KNOW_COUNT, Long.valueOf(followed.size()));
                ArrayList<Map<String, Object>> followers_sample = new ArrayList<>();
                int count = 0;
                for (Node follower : followed) {
                    count++;
                    Map<String, Object> properties = follower.getAllProperties();
                    properties.remove(PASSWORD);
                    properties.remove(EMAIL);
                    properties.remove(SILVER);
                    properties.remove(GOLD);
                    followers_sample.add(properties);
                    if (count > 10) { break; };
                }

                results.put(FOLLOWERS_YOU_KNOW, followers_sample);

            }

            tx.commit();

        }
        return Stream.of(new MapResult(results));
    }

    @Procedure(name = "me.tucu.users.create", mode = Mode.WRITE)
    @Description("CALL me.tucu.users.create(properties)")
    public Stream<MapResult> usersCreate(@Name(value = "properties") Map parameters) {
        Map<String, Object> results = null;
        MapResult validation = UserValidator.validate(parameters);
        if (!validation.isEmpty()) { return  Stream.of(validation); }

        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, parameters.get(USERNAME));
            if (user == null) {
                user = tx.findNode(Labels.User, EMAIL, parameters.get(EMAIL));
                if (user == null) {
                    user = tx.createNode(Labels.User);
                    user.setProperty(EMAIL, parameters.get(EMAIL));
                    user.setProperty(NAME, parameters.get(NAME));
                    user.setProperty(USERNAME, parameters.get(USERNAME));
                    user.setProperty(PASSWORD, parameters.get(PASSWORD));
                    user.setProperty(HASH, new Md5Hash(((String)parameters.get(EMAIL)).toLowerCase()).toString());
                    user.setProperty(SILVER, 299L);
                    user.setProperty(GOLD,0L);

                    LocalDateTime dateTime = LocalDateTime.now(utc);
                    user.setProperty(TIME, dateTime.truncatedTo(ChronoUnit.DAYS).toEpochSecond(ZoneOffset.UTC));

                    results = user.getAllProperties();
                } else {
                    return  Stream.of(UserExceptions.EXISTING_EMAIL);
                }
            } else {
                return  Stream.of(UserExceptions.EXISTING_USERNAME);
            }
            tx.commit();
        }
        return Stream.of(new MapResult(results));
    }

    public static Map<String, Object> getUserAttributes(Node user) {
        Map<String, Object> results;
        results = user.getAllProperties();
        results.remove(EMAIL);
        results.remove(PASSWORD);
        results.remove(SILVER);
        results.remove(GOLD);
        long following = user.getDegree(RelationshipTypes.FOLLOWS, Direction.OUTGOING);
        long followers = user.getDegree(RelationshipTypes.FOLLOWS, Direction.INCOMING);
        long likes = user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING);
        long posts = user.getDegree(Direction.OUTGOING) - following - likes;
        results.put("following", following);
        results.put("followers", followers);
        results.put("likes", likes);
        results.put("posts", posts);
        return results;
    }

    public static void getMutedAndFollows(Node user, HashSet<Node> muted, HashSet<Node> followed) {
        // Hide posts by muted users
        for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
            muted.add(r1.getEndNode());
        }

        // Find the people I follow
        for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
            Node follow = r1.getEndNode();
            followed.add(follow);
        }

        // Add the muted users of the people I follow
        for (Node follow : followed) {
            for (Relationship r2 : follow.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                Node theyMuted = r2.getEndNode();
                // Don't mute myself
                if (theyMuted.equals(user)) { continue; }
                // Add to muted as long as I don't directly follow them
                if(!followed.contains(theyMuted) ) {
                  muted.add(theyMuted);
                }
            }
        }
    }

    public static void payUser(Map<String, Object> results, Node user, Relationship like, Node author, Long silver, Long gold) {
        if (silver > 0) {
            like.setProperty(SILVER, true);
            silver = silver - 1;
            user.setProperty(SILVER, silver);
            author.setProperty(SILVER, (Long)author.getProperty(SILVER) + 1);
            results.put(SILVER, true);
        } else {
            like.setProperty(GOLD, true);
            gold = gold - 1;
            user.setProperty(GOLD, gold);
            author.setProperty(GOLD, (Long)author.getProperty(GOLD) + 1);
            results.put(GOLD, true);
        }
    }
}

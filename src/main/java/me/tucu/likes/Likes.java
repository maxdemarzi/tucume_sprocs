package me.tucu.likes;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.util.Collections.reverseOrder;
import static me.tucu.Exceptions.INSUFFICIENT_FUNDS;
import static me.tucu.likes.LikesExceptions.*;
import static me.tucu.posts.PostExceptions.POST_NOT_FOUND;
import static me.tucu.posts.Posts.*;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.utils.Time.getLatestTime;

public class Likes {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final int TIMEOUT = 1;

    @Procedure(name = "me.tucu.likes.get", mode = Mode.READ)
    @Description("CALL me.tucu.likes.get(username, limit, since, username2)")
    public Stream<MapResult> getLikes(@Name(value = "username", defaultValue = "") String username,
                                      @Name(value = "limit", defaultValue = "25") Long limit,
                                      @Name(value = "since", defaultValue = "-1") Long since,
                                      @Name(value = "username2", defaultValue = "") String username2) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit =  abs(limit);

        ZonedDateTime dateTime = getLatestTime(since);

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
                    properties.put(LIKES, (long)post.getDegree(RelationshipTypes.LIKES));
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
        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(LIKED_TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

    @Procedure(name = "me.tucu.likes.create", mode = Mode.WRITE)
    @Description("CALL me.tucu.likes.create(username, post_id)")
    public Stream<MapResult> createLikes(@Name(value = "username", defaultValue = "") String username,
                                        @Name(value = "post_id", defaultValue = "-1") Long post_id) {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            Node post;
            try {
                post = tx.getNodeById(post_id);
            } catch (Exception exception) {
                return Stream.of(POST_NOT_FOUND);
            }

            if (!post.hasLabel(Labels.Post)) {
                return Stream.of(POST_NOT_FOUND);
            }

            // Get the first Reposted Post if the post being liked is a Promoting Post.
            post = getOriginalPost(post);
            if (userLikesPost(user, post)) {
                return Stream.of(ALREADY_LIKES);
            }

            // We are preparing the like relationship and the results before we
            // find out if the user has the funds needed to perform the action
            // we do this to minimize the time the nodes are locked.
            results = post.getAllProperties();
            Relationship like = user.createRelationshipTo(post, RelationshipTypes.LIKES);
            like.setProperty(TIME, ZonedDateTime.now());
            results.put(LIKED_TIME, ZonedDateTime.now());

            Node author = getAuthor(post);
            results.put(USERNAME, author.getProperty(USERNAME));
            results.put(NAME, author.getProperty(NAME));
            results.put(HASH, author.getProperty(HASH));
            results.put(LIKES, (long)post.getDegree(RelationshipTypes.LIKES));
            results.put(REPOSTS, getRepostedCount(post));
            results.put(LIKED, true);
            results.put(REPOSTED, userRepostedPost(tx, user, post));

            // Lock the users so nobody else can touch them,
            // the lock will be release at the end of the transaction
            tx.acquireWriteLock(user);
            tx.acquireWriteLock(author);

            Long silver = (Long)user.getProperty(SILVER);
            Long gold = (Long)user.getProperty(GOLD);

            // User must have a positive balance of gold and silver
            if (gold + silver < 1) {
                return Stream.of(INSUFFICIENT_FUNDS);
            }

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
            tx.commit();
        }

        return Stream.of(new MapResult(results));
    }

    @Procedure(name = "me.tucu.likes.remove", mode = Mode.WRITE)
    @Description("CALL me.tucu.likes.remove(username, post_id)")
    public Stream<MapResult> removeLikes(@Name(value = "username", defaultValue = "") String username,
                                         @Name(value = "post_id", defaultValue = "-1") Long post_id) {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            Node post;
            try {
                post = tx.getNodeById(post_id);
            } catch (Exception exception) {
                return Stream.of(POST_NOT_FOUND);
            }

            if (!post.hasLabel(Labels.Post)) {
                return Stream.of(POST_NOT_FOUND);
            }

            // Get the first Reposted Post if the post being liked is a Promoting Post.
            post = getOriginalPost(post);

            // User has only a minute to unlike a post in the case of a mistaken click
            Relationship like = null;

            if (user.getDegree(RelationshipTypes.LIKES, Direction.OUTGOING)
                    < post.getDegree(RelationshipTypes.LIKES, Direction.INCOMING) ) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.LIKES)) {
                    if (r1.getEndNode().equals(post)) {
                        like = r1;
                        break;
                    }
                }
            } else {
                for (Relationship r1 : post.getRelationships(Direction.INCOMING, RelationshipTypes.LIKES)) {
                    if (r1.getStartNode().equals(user)) {
                        like = r1;
                        break;
                    }
                }
            }

            if (like == null) {
                return Stream.of(NOT_LIKING);
            }

            results = post.getAllProperties();

            // Prevent more than one unlike
            tx.acquireWriteLock(like);
            Map<String, Object> likeProperties = like.getAllProperties();
            results.put(LIKED_TIME, likeProperties.get(TIME));

            // Users can only unlike a post within a small window (if liked in error)
            if(((ZonedDateTime)results.get(LIKED_TIME))
                    .isBefore(ZonedDateTime.now().minus(TIMEOUT, ChronoUnit.MINUTES))) {
                return Stream.of(UNLIKE_TIMEOUT);
            }

            Node author = getAuthor(post);
            results.put(USERNAME, author.getProperty(USERNAME));
            results.put(NAME, author.getProperty(NAME));
            results.put(HASH, author.getProperty(HASH));
            results.put(LIKES, (long)post.getDegree(RelationshipTypes.LIKES) - 1L);
            results.put(REPOSTS, getRepostedCount(post));
            results.put(LIKED, false);
            results.put(REPOSTED, userRepostedPost(tx, user, post));

            // Lock the users so nobody else can touch them,
            // the lock will be release at the end of the transaction
            tx.acquireWriteLock(user);
            tx.acquireWriteLock(author);

            // Refund whatever they paid the first time
            if(likeProperties.containsKey(SILVER)){
                user.setProperty(SILVER, 1L + (long)user.getProperty(SILVER));
                author.setProperty(SILVER, (Long)author.getProperty(SILVER) - 1L);
                results.put(SILVER, true);
            } else {
                user.setProperty(GOLD, 1L + (long)user.getProperty(GOLD));
                author.setProperty(GOLD, (Long)author.getProperty(GOLD) - 1L);
                results.put(GOLD, true);
            }

            like.delete();
            tx.commit();
        }
        return Stream.of(new MapResult(results));
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

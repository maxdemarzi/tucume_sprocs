package me.tucu.posts;

import me.tucu.mentions.Mentions;
import me.tucu.promotes.Promotes;
import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import me.tucu.tags.Tags;
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
import static me.tucu.likes.Likes.userLikesPost;
import static me.tucu.posts.PostExceptions.POST_ALREADY_REPOSTED;
import static me.tucu.posts.PostExceptions.POST_NOT_FOUND;
import static me.tucu.schema.DatedRelationshipTypes.POSTED_ON;
import static me.tucu.schema.DatedRelationshipTypes.REPOSTED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.payUser;
import static me.tucu.utils.Time.dateFormatter;
import static me.tucu.utils.Time.getLatestTime;

public class Posts {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;


    @Procedure(name = "me.tucu.posts.get", mode = Mode.READ)
    @Description("CALL me.tucu.posts.get(username, limit, since, username2)")
    public Stream<MapResult> gePosts(@Name(value = "username", defaultValue = "") String username,
                                      @Name(value = "limit", defaultValue = "25") Long limit,
                                      @Name(value = "since", defaultValue = "-1") Long since,
                                      @Name(value = "username2", defaultValue = "") String username2) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit = abs(limit);

        ZonedDateTime dateTime = getLatestTime(since);
        ZonedDateTime now = getLatestTime(since);

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

            Map<String, Object> userProperties = user.getAllProperties();
            ZonedDateTime earliest = ((ZonedDateTime) user.getProperty(TIME)).truncatedTo(ChronoUnit.DAYS);
            int count = 0;

            while (count < limit && now.isAfter(earliest)) {
                RelationshipType posted_on = RelationshipType.withName(POSTED_ON +
                        now.format(dateFormatter));

                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, posted_on)) {
                    Node post = r1.getEndNode();
                    Map<String, Object> properties = post.getAllProperties();

                    ZonedDateTime time = (ZonedDateTime) post.getProperty(TIME);
                    if (time.isBefore(dateTime)) {
                        properties.put(USERNAME, username);
                        properties.put(NAME, userProperties.get(NAME));
                        properties.put(HASH, userProperties.get(HASH));
                        properties.put(LIKES, (long) post.getDegree(RelationshipTypes.LIKES));
                        properties.put(REPOSTS, getRepostedCount(post));
                        if (user2 != null) {
                            properties.put(LIKED, userLikesPost(user2, post));
                            properties.put(REPOSTED, userRepostedPost(tx, user2, post));
                        }
                        results.add(properties);
                        count++;
                    }
                }
                // Check the day before
                now = now.minusDays(1);
            }
        }

        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

    @Procedure(name = "me.tucu.posts.create", mode = Mode.WRITE)
    @Description("CALL me.tucu.posts.create(parameters)")
    public Stream<MapResult> createPost(@Name(value = "parameters") Map parameters) {
        Map<String, Object> results = null;
        MapResult validation = PostValidator.validate(parameters);
        if (!validation.isEmpty()) {
            return Stream.of(validation);
        }

        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, parameters.get(USERNAME));
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            ZonedDateTime dateTime = ZonedDateTime.now();
            Node post = tx.createNode(Labels.Post);
            post.setProperty(STATUS, parameters.get(STATUS));
            post.setProperty(TIME, dateTime);
            Relationship posted_on = user.createRelationshipTo(post, RelationshipType.withName(POSTED_ON +
                    dateTime.format(dateFormatter)));
            posted_on.setProperty(TIME, dateTime);

            Tags.createTags(post, parameters, dateTime, tx);
            Mentions.createMentions(post, parameters, dateTime, tx);
            Promotes.createPromotes(post, parameters, dateTime, tx);
            results = post.getAllProperties();
            results.put(USERNAME, parameters.get(USERNAME));
            results.put(NAME, user.getProperty(NAME));
            results.put(HASH, user.getProperty(HASH));
            results.put(REPOSTS, 0);
            results.put(LIKES, 0);

            // Lock the users so nobody else can touch them,
            // the lock will be release at the end of the transaction
            tx.acquireWriteLock(user);

            Long silver = (Long)user.getProperty(SILVER);
            Long gold = (Long)user.getProperty(GOLD);

            // User must have a positive balance of gold and silver
            if (gold + silver < 1) {
                return Stream.of(INSUFFICIENT_FUNDS);
            }

            if (silver > 0) {
                posted_on.setProperty(SILVER, true);
                silver = silver - 1;
                user.setProperty(SILVER, silver);
                results.put(SILVER, true);
            } else {
                posted_on.setProperty(GOLD, true);
                gold = gold - 1;
                user.setProperty(GOLD, gold);
                results.put(GOLD, true);
            }

            tx.commit();
        }

        return Stream.of(new MapResult(results));
    }

    @Procedure(name = "me.tucu.posts.repost", mode = Mode.WRITE)
    @Description("CALL me.tucu.posts.repost(username, post_id)")
    public Stream<MapResult> createRepost(@Name(value = "username", defaultValue = "") String username,
                                         @Name(value = "post_id", defaultValue = "-1") Long post_id) {
        Map<String, Object> results = null;
        ZonedDateTime dateTime = ZonedDateTime.now();
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

            if (userRepostedPost(tx, user, post)) {
                return Stream.of(POST_ALREADY_REPOSTED);
            }

            RelationshipType reposted_on = RelationshipType.withName(REPOSTED_ON +
                    dateTime.format(dateFormatter));

            Node repost;
            Relationship reposted;

            // It's an advertisement
            if(post.hasRelationship(RelationshipTypes.PROMOTES) || !post.hasProperty(STATUS) ) {
                repost = tx.createNode(Labels.Post);
                repost.setProperty(POST_ID, post_id);
                repost.setProperty(USERNAME, username);
                reposted = repost.createRelationshipTo(post, reposted_on);
                reposted.setProperty(TIME, dateTime);
            } else {
                reposted = user.createRelationshipTo(post, reposted_on);
                reposted.setProperty(TIME, dateTime);
            }

            // Get the actual Post if the post being reposted is a Promoting Post.
            post = getOriginalPost(post);

            results = post.getAllProperties();
            results.put(LIKES, (long)post.getDegree(RelationshipTypes.LIKES));
            results.put(REPOSTS, 1 + getRepostedCount(post));
            results.put(LIKED, userLikesPost(user, post));
            results.put(REPOSTED, true);

            Node author = getAuthor(tx, post);

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

            payUser(results, user, reposted, author, silver, gold);
            tx.commit();
        }
        return Stream.of(new MapResult(results));
    }


    public static boolean userRepostedPost(Transaction tx, Node user, Node post) {

        // It's an advertisement
        if(post.hasRelationship(RelationshipTypes.PROMOTES)) {
            Long postId = post.getId();
            String username = (String)user.getProperty(USERNAME);
            ResourceIterator<Node> iterator = tx.findNodes(Labels.Post, USERNAME, username, POST_ID, postId);
            return iterator.hasNext();
        }

        // It's a regular post

        // If the post has a few incoming relationships, just brute force it
        if (post.getDegree(Direction.INCOMING) < 1000) {
            for (Relationship r1 : post.getRelationships(Direction.INCOMING)) {
                if (r1.getStartNode().equals(user) && r1.getType().name().startsWith(REPOSTED_ON)) {
                    return true;
                }
            }
        }

        // If the post has lots of relationships, start from now and go backwards
        // until the post creation date checking it or the user for a repost relationship
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime time = ((ZonedDateTime)post.getProperty(TIME)).truncatedTo(ChronoUnit.DAYS);
        while(now.isAfter(time)) {
            RelationshipType repostedOn = RelationshipType.withName(REPOSTED_ON +
                    now.format(dateFormatter));

            if (user.getDegree(repostedOn, Direction.OUTGOING)
                    < post.getDegree(repostedOn, Direction.INCOMING)) {
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, repostedOn)) {
                    if (r1.getEndNode().equals(post)) {
                        return true;
                    }
                }
            } else {
                for (Relationship r1 : post.getRelationships(Direction.INCOMING, repostedOn)) {
                    if (r1.getStartNode().equals(user)) {
                        return true;
                    }
                }
            }
            // Check the day before
            now = now.minusDays(1);
        }

            return false;
    }

    public static Node getAuthor(Transaction tx, Node post) {
        // I am almost positive we never actually deal with this, but leaving it in for now.
        // We should be jumping to the original post before asking for the author.
        if(!post.hasProperty(STATUS)) {
            long post_id = (long)post.getProperty(POST_ID);
            post = tx.getNodeById(post_id);
        }

        ZonedDateTime time = (ZonedDateTime)post.getProperty(TIME);
        RelationshipType original = RelationshipType.withName(POSTED_ON +
                time.format(dateFormatter));
        return post.getSingleRelationship(original, Direction.INCOMING).getStartNode();
    }

    public static Long getRepostedCount(Node post) {
        // It's a regular post
        if(!post.hasRelationship(RelationshipTypes.PROMOTES)) {
            return (long) (post.getDegree(Direction.INCOMING)
                    - 1 // for the Posted Relationship Type
                    - post.getDegree(RelationshipTypes.LIKES)
                    - post.getDegree(RelationshipTypes.REPLIED_TO));
        }

        // It's an advertisement
        long count = 0;
        ArrayList<Node> posts = new ArrayList<>();
        posts.add(post);

        while (!posts.isEmpty()) {
            Node node = posts.remove(0);
            for (Relationship rel : node.getRelationships(Direction.INCOMING, RelationshipTypes.REPOSTED)) {
                count++;
                posts.add(rel.getStartNode());
            }
        }

        return count;
    }

    public static Node getOriginalPost(Node post) {
        while(post.hasRelationship(Direction.OUTGOING, RelationshipTypes.REPOSTED)) {
            post = post.getSingleRelationship(RelationshipTypes.REPOSTED, Direction.OUTGOING).getEndNode();
        }
        return post;
    }

}

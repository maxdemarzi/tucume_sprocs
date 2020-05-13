package me.tucu.mentions;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.util.Collections.reverseOrder;
import static me.tucu.likes.Likes.userLikesPost;
import static me.tucu.posts.Posts.*;
import static me.tucu.schema.DatedRelationshipTypes.MENTIONED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.utils.Time.dateFormatter;
import static me.tucu.utils.Time.getLatestTime;

public class Mentions {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final Pattern MENTIONS_PATTERN = Pattern.compile("@([a-z][a-z0-9_]{2,31})");

    @Procedure(name = "me.tucu.mentions.get", mode = Mode.READ)
    @Description("CALL me.tucu.mentions.get(username, limit, since, username2)")
    public Stream<MapResult> getMentions(@Name(value = "username", defaultValue = "") String username,
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

            // If a different user asked for the mentions, add a few things
            Node user2 = null;
            if (!username2.isEmpty() && !username.equals(username2)) {
                user2 = tx.findNode(Labels.User, USERNAME, username2);
                if (user2 == null) {
                    return Stream.of(USER_NOT_FOUND);
                }
            }

            // Hide mentions by muted users
            HashSet<Node> muted = new HashSet<>();
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                muted.add(r1.getEndNode());
            }

            // Hide mentions of muted users by the people I follow
            for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                Node followed = r1.getEndNode();
                for (Relationship r2 : followed.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                    muted.add(r2.getEndNode());
                }
            }

            ZonedDateTime earliest = ((ZonedDateTime) user.getProperty(TIME)).truncatedTo(ChronoUnit.DAYS);
            int count = 0;

            while (count < limit && now.isAfter(earliest)) {
                RelationshipType mentioned_on = RelationshipType.withName(MENTIONED_ON +
                        now.format(dateFormatter));

                for (Relationship r1 : user.getRelationships(Direction.INCOMING, mentioned_on)) {
                    Node post = r1.getStartNode();
                    Map<String, Object> properties = post.getAllProperties();

                    ZonedDateTime time = (ZonedDateTime) post.getProperty(TIME);
                    if (time.isBefore(dateTime)) {
                        Node author = getAuthor(tx, post);
                        // Ignore any mentions by muted users
                        if (!muted.contains(author)) {
                            properties.put(USERNAME, author.getProperty(USERNAME));
                            properties.put(NAME, author.getProperty(NAME));
                            properties.put(HASH, author.getProperty(HASH));
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

                }
                // Check the day before
                now = now.minusDays(1);
            }
        }

        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

    public static void createMentions(Node post, Map input, ZonedDateTime dateTime, Transaction tx) {
        Matcher mat = MENTIONS_PATTERN.matcher(((String)input.get(STATUS)).toLowerCase());

        RelationshipType mentioned_on =  RelationshipType.withName(MENTIONED_ON + dateTime.format(dateFormatter));
        for (Relationship r1 : post.getRelationships(Direction.OUTGOING,mentioned_on)) {
            r1.delete();
        }

        Set<Node> mentioned = new HashSet<>();
        while (mat.find()) {
            String username = mat.group(1);
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user != null && !mentioned.contains(user)) {
                Relationship r1 = post.createRelationshipTo(user, mentioned_on);
                r1.setProperty(TIME, dateTime);
                mentioned.add(user);
            }
        }
    }
}
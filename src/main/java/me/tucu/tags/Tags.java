package me.tucu.tags;

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
import static me.tucu.schema.DatedRelationshipTypes.TAGGED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.tags.TagExceptions.TAG_NOT_FOUND;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.getMutedAndFollows;
import static me.tucu.utils.Time.dateFormatter;
import static me.tucu.utils.Time.getLatestTime;

public class Tags {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final Pattern TAGS_PATTERN = Pattern.compile("#([a-z][a-z0-9_]{2,31})");


    @Procedure(name = "me.tucu.tags.get", mode = Mode.READ)
    @Description("CALL me.tucu.tags.get(hashtag, limit, since, username)")
    public Stream<MapResult> getTags(@Name(value = "hashtag", defaultValue = "") String hashtag,
                                         @Name(value = "limit", defaultValue = "25") Long limit,
                                         @Name(value = "since", defaultValue = "-1") Long since,
                                         @Name(value = "username", defaultValue = "") String username) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit = abs(limit);

        ZonedDateTime dateTime = getLatestTime(since);
        ZonedDateTime now = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            // Get the tag
            Node tag = tx.findNode(Labels.Tag, NAME, hashtag.toLowerCase());
            if (tag == null) {
                return Stream.of(TAG_NOT_FOUND);
            }

            // Get the User
            Node user = null;
            HashSet<Node> muted = new HashSet<>();
            HashSet<Node> follows = new HashSet<>();

            if (!username.isEmpty()) {
                user = tx.findNode(Labels.User, USERNAME, username);
                if (user == null) {
                    return Stream.of(USER_NOT_FOUND);
                }

                // Hide posts by muted users
                getMutedAndFollows(user, muted, follows);
            }

            ZonedDateTime earliest = ((ZonedDateTime) tag.getProperty(TIME)).truncatedTo(ChronoUnit.DAYS);
            int count = 0;

            while (count < limit && now.isAfter(earliest)) {
                RelationshipType tagged_on = RelationshipType.withName(TAGGED_ON +
                        now.format(dateFormatter));

                for (Relationship r1 : tag.getRelationships(Direction.INCOMING, tagged_on)) {
                    Node post = r1.getStartNode();
                    Map<String, Object> properties = post.getAllProperties();

                    ZonedDateTime time = (ZonedDateTime) post.getProperty(TIME);
                    if (time.isBefore(dateTime)) {
                        Node author = getAuthor(post);
                        // Ignore any mentions by muted users
                        if (!muted.contains(author)) {
                            properties.put(USERNAME, author.getProperty(USERNAME));
                            properties.put(NAME, author.getProperty(NAME));
                            properties.put(HASH, author.getProperty(HASH));
                            properties.put(LIKES, (long) post.getDegree(RelationshipTypes.LIKES));
                            properties.put(REPOSTS, getRepostedCount(post));
                            if (user != null) {
                                properties.put(LIKED, userLikesPost(user, post));
                                properties.put(REPOSTED, userRepostedPost(tx, user, post));
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



    public static void createTags(Node post, Map input, ZonedDateTime dateTime, Transaction tx) {
        Matcher mat = TAGS_PATTERN.matcher(((String)input.get(STATUS)).toLowerCase());
        for (Relationship r1 : post.getRelationships(Direction.OUTGOING, RelationshipType.withName(TAGGED_ON +
                dateTime.format(dateFormatter)))) {
            r1.delete();
        }
        Set<Node> tagged = new HashSet<>();
        while (mat.find()) {
            String tag = mat.group(1);
            Node hashtag = tx.findNode(Labels.Tag, NAME, tag);
            if (hashtag == null) {
                hashtag = tx.createNode(Labels.Tag);
                hashtag.setProperty(NAME, tag);
                hashtag.setProperty(TIME, dateTime);
            }
            if (!tagged.contains(hashtag)) {
                post.createRelationshipTo(hashtag, RelationshipType.withName(TAGGED_ON +
                        dateTime.format(dateFormatter)));
                tagged.add(hashtag);
            }
        }
    }
}

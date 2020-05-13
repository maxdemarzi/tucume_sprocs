package me.tucu.timeline;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static me.tucu.likes.Likes.userLikesPost;
import static me.tucu.posts.Posts.*;
import static me.tucu.schema.DatedRelationshipTypes.POSTED_ON;
import static me.tucu.schema.DatedRelationshipTypes.REPOSTED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.getMutedAndFollows;
import static me.tucu.utils.Comparators.DESC_TIME_COMPARATOR;
import static me.tucu.utils.Time.dateFormatter;
import static me.tucu.utils.Time.getLatestTime;

public class Timeline {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.timeline.get", mode = Mode.READ)
    @Description("CALL me.tucu.timeline.get(username, limit, since)")
    public Stream<MapResult> geTimeline(@Name(value = "username", defaultValue = "") String username,
                                     @Name(value = "limit", defaultValue = "25") Long limit,
                                     @Name(value = "since", defaultValue = "-1") Long since) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit = abs(limit);

        ZonedDateTime dateTime = getLatestTime(since);
        ZonedDateTime now = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }

            HashSet<Long> seen = new HashSet<>();
            HashSet<Node> muted = new HashSet<>();
            HashSet<Node> follows = new HashSet<>();
            getMutedAndFollows(user, muted, follows);

            // Adding user to see their posts on timeline as well
            follows.add(user);

            ZonedDateTime earliest = ((ZonedDateTime) user.getProperty(TIME)).truncatedTo(ChronoUnit.DAYS);
            int count = 0;

            while (count < limit && now.isAfter(earliest)) {
                RelationshipType posted_on = RelationshipType.withName(POSTED_ON +
                        now.format(dateFormatter));
                RelationshipType reposted_on = RelationshipType.withName(REPOSTED_ON +
                        now.format(dateFormatter));

                for (Node follow : follows) {
                    Map userProperties = follow.getAllProperties();

                    for (Relationship r1 : follow.getRelationships(Direction.OUTGOING, posted_on)) {
                        Node post = r1.getEndNode();

                        if (seen.add(post.getId())) {
                            ZonedDateTime time = (ZonedDateTime) r1.getProperty(TIME);
                            if (time.isBefore(dateTime)) {
                                Map<String, Object> properties = post.getAllProperties();

                                properties.put(USERNAME, userProperties.get(USERNAME));
                                properties.put(NAME, userProperties.get(NAME));
                                properties.put(HASH, userProperties.get(HASH));
                                properties.put(LIKES, (long) post.getDegree(RelationshipTypes.LIKES));
                                properties.put(REPOSTS, getRepostedCount(post));
                                properties.put(LIKED, userLikesPost(user, post));
                                properties.put(REPOSTED, userRepostedPost(tx, user, post));
                                results.add(properties);
                            }
                        }
                    }

                    for (Relationship r1 : follow.getRelationships(Direction.OUTGOING, reposted_on)) {
                        Node post = r1.getEndNode();
                        if (seen.add(post.getId())) {
                            ZonedDateTime time = (ZonedDateTime) r1.getProperty(TIME);
                            if (time.isBefore(dateTime)) {
                                Map<String, Object> properties = post.getAllProperties();
                                properties.put(REPOSTED_TIME, time);
                                properties.put(REPOSTER_USERNAME, userProperties.get(USERNAME));
                                properties.put(REPOSTER_NAME, userProperties.get(NAME));
                                properties.put(HASH, userProperties.get(HASH));
                                properties.put(LIKES, (long) post.getDegree(RelationshipTypes.LIKES));
                                properties.put(REPOSTS, getRepostedCount(post));
                                properties.put(LIKED, userLikesPost(user, post));
                                properties.put(REPOSTED, userRepostedPost(tx, user, post));
                                Node author = getAuthor(tx, post);
                                if (!muted.contains(author)) {
                                    properties.put(USERNAME, author.getProperty(USERNAME));
                                    properties.put(NAME, author.getProperty(NAME));
                                    results.add(properties);
                                }
                            }
                        }
                    }
                }
                // Check the day before
                now = now.minusDays(1);
            }
            tx.commit();
        }

        results.sort(DESC_TIME_COMPARATOR);
        return results.stream().limit(limit).map(MapResult::new);
    }
}

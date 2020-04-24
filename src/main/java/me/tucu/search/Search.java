package me.tucu.search;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.reverseOrder;
import static me.tucu.likes.Likes.userLikesPost;
import static me.tucu.posts.Posts.*;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.utils.Time.getLatestTime;

public class Search {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final ZonedDateTime SITE_CREATION =
            ZonedDateTime.of(2020, 4, 1, 0, 0, 0, 0, UTC);

    @Procedure(name = "me.tucu.search.get", mode = Mode.READ)
    @Description("CALL me.tucu.search.get(username, limit, since, username2)")
    public Stream<MapResult> getSearch(@Name(value = "term", defaultValue = "") String term,
                                      @Name(value = "limit", defaultValue = "25") Long limit,
                                      @Name(value = "since", defaultValue = "-1") Long since,
                                      @Name(value = "username", defaultValue = "") String username) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit = abs(limit);

        ZonedDateTime dateTime = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            // Get the User
            Node user = null;
            HashSet<Node> muted = new HashSet<>();

            if (!username.isEmpty()) {
                user = tx.findNode(Labels.User, USERNAME, username);
                if (user == null) {
                    return Stream.of(USER_NOT_FOUND);
                }

                // Hide posts by muted users
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                    muted.add(r1.getEndNode());
                }

                // Hide posts of muted users by the people I follow
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                    Node followed = r1.getEndNode();
                    for (Relationship r2 : followed.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                        muted.add(r2.getEndNode());
                    }
                }
            }

            long maximumDays = ChronoUnit.DAYS.between(SITE_CREATION, dateTime);
            int days = 0;
            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(TIME, dateTime);
            queryParameters.put(TERM, term);

            do {
                queryParameters.put(DAYS, "P" + days + "D");

                Result result = tx.execute("MATCH (p:Post) " +
                        "WHERE ( $time - duration($days) )  > p.time > ( $time - duration($days) - duration('P7D') ) " +
                        "  AND p.status CONTAINS $term " +
                        "RETURN p", queryParameters);

                try( ResourceIterator<Node> iterator = result.columnAs("p")) {
                    while (iterator.hasNext()) {
                        Node post = iterator.next();
                        Map<String, Object> properties = post.getAllProperties();
                        ZonedDateTime time = (ZonedDateTime) properties.get(TIME);
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
                            }
                        }
                    }
                }

                days = days + 7;
            } while (maximumDays > days || results.size() > limit);

        }

        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }


    @Procedure(name = "me.tucu.search.fulltext.get", mode = Mode.READ)
    @Description("CALL me.tucu.search.fulltext.get(username, limit, since, username2)")
    public Stream<MapResult> getFullTextSearch(@Name(value = "term", defaultValue = "") String term,
                                       @Name(value = "limit", defaultValue = "25") Long limit,
                                       @Name(value = "since", defaultValue = "-1") Long since,
                                       @Name(value = "username", defaultValue = "") String username) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit = abs(limit);

        ZonedDateTime dateTime = getLatestTime(since);

        try (Transaction tx = db.beginTx()) {
            // Get the User
            Node user = null;
            HashSet<Node> muted = new HashSet<>();

            if (!username.isEmpty()) {
                user = tx.findNode(Labels.User, USERNAME, username);
                if (user == null) {
                    return Stream.of(USER_NOT_FOUND);
                }

                // Hide posts by muted users
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                    muted.add(r1.getEndNode());
                }

                // Hide posts of muted users by the people I follow
                for (Relationship r1 : user.getRelationships(Direction.OUTGOING, RelationshipTypes.FOLLOWS)) {
                    Node followed = r1.getEndNode();
                    for (Relationship r2 : followed.getRelationships(Direction.OUTGOING, RelationshipTypes.MUTES)) {
                        muted.add(r2.getEndNode());
                    }
                }
            }


            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(TERM, term);
            Result result = tx.execute("CALL db.index.fulltext.queryNodes('fulltext', $term) YIELD node RETURN node",
                    queryParameters);

            try( ResourceIterator<Node> iterator = result.columnAs("node")) {
                while (iterator.hasNext()) {
                    Node post = iterator.next();
                    Map<String, Object> properties = post.getAllProperties();
                    ZonedDateTime time = (ZonedDateTime) properties.get(TIME);
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
                        }
                    }
                }
            }
        }

        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }

}

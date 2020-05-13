package me.tucu.search;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static me.tucu.likes.Likes.userLikesPost;
import static me.tucu.posts.Posts.*;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.getMutedAndFollows;
import static me.tucu.users.Users.getUserAttributes;
import static me.tucu.utils.Comparators.LABEL_COMPARATOR;

public class Search {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;


    @Procedure(name = "me.tucu.search.get", mode = Mode.READ)
    @Description("CALL me.tucu.search.get(username, limit, since, username2)")
    public Stream<MapResult> getSearch(@Name(value = "term", defaultValue = "") String term,
                                       @Name(value = "type", defaultValue = "") String type,
                                       @Name(value = "limit", defaultValue = "25") Long limit,
                                       @Name(value = "offset", defaultValue = "0") Long offset,
                                       @Name(value = "username", defaultValue = "") String username) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit = abs(limit);
        offset = abs(offset);

        boolean userType = true;
        boolean productType = true;
        boolean postType = true;
        if (!type.isEmpty()) {
            switch (type) {
                case "user":
                    productType = false;
                    postType = false;
                    break;
                case "product":
                    userType = false;
                    postType = false;
                    break;
                case "post":
                    userType = false;
                    productType = false;
                    break;
            }
        }

        try (Transaction tx = db.beginTx()) {
            // Get the User
            Node user = null;
            HashSet<Node> muted = new HashSet<>();
            HashSet<Node> follows = new HashSet<>();

            if (!username.isEmpty()) {
                user = tx.findNode(Labels.User, USERNAME, username);
                if (user == null) {
                    return Stream.of(USER_NOT_FOUND);
                }
                getMutedAndFollows(user, muted, follows);

            }

            Map<String, Object> queryParameters = new HashMap<>();
            queryParameters.put(TERM, term);
            Result result = tx.execute("CALL db.index.fulltext.queryNodes('fulltext', $term) " +
                            "YIELD node RETURN node", queryParameters);

            try( ResourceIterator<Node> iterator = result.columnAs("node")) {
                while (iterator.hasNext() && results.size() < (offset + limit) ) {
                    Node node = iterator.next();

                    if(node.hasLabel(Labels.User) && userType) {
                        // Ignore muted users
                        if (!muted.contains(node)) {
                            Map<String, Object> properties = getUserAttributes(node);
                            properties.put(I_FOLLOW, follows.contains(node));
                            properties.put(LABEL, USER);

                            results.add(properties);
                        }
                    }

                    if (node.hasLabel(Labels.Product) && productType) {
                        Map<String, Object> properties = node.getAllProperties();
                        //todo: Handle Product type
                        properties.put(LABEL, PRODUCT);
                        results.add(properties);
                    }

                    if (node.hasLabel(Labels.Post) && postType) {
                        Node author = getAuthor(node);
                        // Ignore any mentions by muted users
                        if (!muted.contains(author)) {
                            Map<String, Object> properties = node.getAllProperties();
                            properties.put(USERNAME, author.getProperty(USERNAME));
                            properties.put(NAME, author.getProperty(NAME));
                            properties.put(HASH, author.getProperty(HASH));
                            properties.put(LIKES, (long) node.getDegree(RelationshipTypes.LIKES));
                            properties.put(REPOSTS, getRepostedCount(node));
                            if (user != null) {
                                properties.put(LIKED, userLikesPost(user, node));
                                properties.put(REPOSTED, userRepostedPost(tx, user, node));
                            }
                            properties.put(LABEL, POST);
                            results.add(properties);
                        }
                    }
                }

            }
        }

        results.sort(LABEL_COMPARATOR);
        return results.stream().skip(offset).map(MapResult::new);
    }

}

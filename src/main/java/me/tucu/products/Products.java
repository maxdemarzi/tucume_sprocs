package me.tucu.products;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static me.tucu.posts.PostExceptions.POST_NOT_FOUND;
import static me.tucu.posts.Posts.getProduct;
import static me.tucu.schema.DatedRelationshipTypes.PURCHASED_ON;
import static me.tucu.schema.DatedRelationshipTypes.REPOSTED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.utils.Time.dateFormatter;

public class Products {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final double siteCommission = 0.10;
    private static final double splitCommission = 0.70;
    private static final double directCommission = 0.20;

    private static final HashMap<Integer, ArrayList<Double>> splits = new HashMap<>() {{
        put(1, new ArrayList<>() {{ add(0.20); }});
        put(2, new ArrayList<>() {{ add(0.14); add(0.06);}});
        put(3, new ArrayList<>() {{ add(0.14); add(0.042); add(0.018);}});
        put(4, new ArrayList<>() {{ add(0.14); add(0.029); add(0.022); add(0.009);}});
    }};

    @Procedure(name = "me.tucu.products.purchase", mode = Mode.WRITE)
    @Description("CALL me.tucu.products.purchase(username, limit, since, username2)")
    public Stream<MapResult> purchaseProduct(@Name(value = "username", defaultValue = "") String username,
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

            RelationshipType purchased_on = RelationshipType.withName(PURCHASED_ON +
                    dateTime.format(dateFormatter));

            Relationship purchased = user.createRelationshipTo(post, purchased_on);
            purchased.setProperty(TIME, dateTime);



            Node product = getProduct(post);
            Relationship purchased2 = user.createRelationshipTo(product, RelationshipTypes.PURCHASED);
            purchased2.setProperty(TIME, dateTime);

            Long price = (Long) product.getProperty(PRICE);
            Double sellerCommission = Math.floor(price * splitCommission);

            // Handle purchase direct from seller
            if (post.hasRelationship(Direction.OUTGOING, RelationshipTypes.PROMOTES)) {
                sellerCommission = Math.floor(price * (splitCommission + directCommission));
            } else {
                List<Node> chain = new ArrayList<>();
                List<Integer> commisions = new ArrayList<>();
                while (post.hasRelationship(Direction.OUTGOING, RelationshipTypes.REPOSTED)) {
                    chain.add(getReposter(post));
                    post = post.getSingleRelationship(RelationshipTypes.REPOSTED, Direction.OUTGOING).getEndNode();
                }

                chain = chain.subList(0, Math.min(chain.size(), 3));

                for (Double split : splits.get(chain.size())){
                    commisions.add(((Double)Math.floor(price * split)).intValue());
                }

                //todo: calculate commission right here and now
                // lock the buyer and the seller, and the chain and pay them.


            }



        }
        return Stream.of(new MapResult(results));
    }

    public static Node getReposter(Node post) {
        ZonedDateTime time = (ZonedDateTime)post.getProperty(TIME);
        RelationshipType original = RelationshipType.withName(REPOSTED_ON +
                time.format(dateFormatter));
        return post.getSingleRelationship(original, Direction.INCOMING).getStartNode();
    }

}

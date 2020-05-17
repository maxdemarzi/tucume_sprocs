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

import static me.tucu.Exceptions.INSUFFICIENT_FUNDS;
import static me.tucu.posts.PostExceptions.POST_NOT_FOUND;
import static me.tucu.posts.Posts.*;
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

            Node product = getProduct(post);
            results = product.getAllProperties();
            Long price = (Long) results.get(PRICE);

            RelationshipType purchased_on = RelationshipType.withName(PURCHASED_ON +
                    dateTime.format(dateFormatter));

            Relationship purchased = user.createRelationshipTo(post, purchased_on);
            purchased.setProperty(TIME, dateTime);
            purchased.setProperty(PRICE, price);

            // I am duplicating data here, an altenative is to create a Purchase node instead
            // The first purchased relationship lets me quickly build the commission tree and see new purchases
            // The second purchased relationship lets me know what the user has purchased quickly
            Relationship purchased2 = user.createRelationshipTo(product, RelationshipTypes.PURCHASED);
            purchased2.setProperty(TIME, dateTime);
            purchased2.setProperty(PRICE, price);

            Node seller = product.getSingleRelationship(RelationshipTypes.SELLS, Direction.INCOMING).getStartNode();

            Double sellerCommission = Math.floor(price * splitCommission);

            List<Node> chain = new ArrayList<>();
            List<Integer> commisions = new ArrayList<>();

            while (post.hasRelationship(Direction.OUTGOING, RelationshipTypes.REPOSTED)) {
                chain.add(getReposter(post));
                post = post.getSingleRelationship(RelationshipTypes.REPOSTED, Direction.OUTGOING).getEndNode();
            }
            chain.add(getAuthor(post));
            chain = chain.subList(0, Math.min(chain.size(), 4));

            for (Double split : splits.get(chain.size())){
                commisions.add(((Double)Math.floor(price * split)).intValue());
            }

            // Lock the users so nobody else can touch them,
            // the lock will be released at the end of the transaction
            tx.acquireWriteLock(user);
            tx.acquireWriteLock(seller);
            for (Node marketer : chain) {
                tx.acquireWriteLock(marketer);
            }

            Long userGold = (Long)user.getProperty(GOLD);
            if (userGold < price) {
                return Stream.of(INSUFFICIENT_FUNDS);
            }

            Long sellerGold = (Long)seller.getProperty(GOLD);

            userGold -= price;
            sellerGold += sellerCommission.longValue();

            user.setProperty(GOLD, userGold);
            seller.setProperty(GOLD, sellerGold);

            for(int counter = 0; counter <= chain.size(); counter++){
                Node marketer = chain.get(counter);
                Long marketerGold = (Long)marketer.getProperty(GOLD);
                marketerGold += commisions.get(counter);
                marketer.setProperty(GOLD, marketerGold);
            }


        }
        return Stream.of(new MapResult(results));
    }

}

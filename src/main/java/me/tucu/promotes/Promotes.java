package me.tucu.promotes;

import me.tucu.schema.Labels;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.tucu.schema.Properties.*;
import static me.tucu.schema.RelationshipTypes.PROMOTES;

public class Promotes {

    // Dollar Sign followed by a character, followed by up to 31 more characters and numbers
    private static final Pattern PROMOTES_PATTERN = Pattern.compile("\\$([a-z][a-z0-9_]{2,31})");

    public static void createPromotes(Node post, Map input, ZonedDateTime dateTime, Transaction tx) {
        Matcher mat = PROMOTES_PATTERN.matcher(((String)input.get(STATUS)).toLowerCase());

        for (Relationship r1 : post.getRelationships(Direction.OUTGOING, PROMOTES)) {
            r1.delete();
        }

        // Users can only promote ONE Product in a Post
        boolean promoted = false;
        while (mat.find() && !promoted) {
            String id = mat.group(1);
            Node product = tx.findNode(Labels.Product, ID, id);
            if (product != null) {
                Relationship r1 = post.createRelationshipTo(product, PROMOTES);
                r1.setProperty(TIME, dateTime);
                promoted = true;
            }
        }
    }
}

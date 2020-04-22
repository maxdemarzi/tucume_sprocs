package me.tucu.tags;

import me.tucu.schema.Labels;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.tucu.schema.DatedRelationshipTypes.TAGGED_ON;
import static me.tucu.schema.Properties.*;
import static me.tucu.utils.Time.dateFormatter;

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

    public static void createTags(Node post, HashMap<String, Object> input, ZonedDateTime dateTime, Transaction tx) {
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

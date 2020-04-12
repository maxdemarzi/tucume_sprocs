package me.tucu.follows;

import me.tucu.results.MapResult;
import me.tucu.schema.Labels;
import me.tucu.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.reverseOrder;
import static me.tucu.schema.Properties.TIME;
import static me.tucu.schema.Properties.USERNAME;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.users.Users.getUserAttributes;

public class Follows {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.follows.followers", mode = Mode.READ)
    @Description("CALL me.tucu.follows.followers(username, limit, since)")
    public Stream<MapResult> getFollowers(@Name(value = "username", defaultValue = "") String username,
                                          @Name(value = "limit", defaultValue = "25") Long limit,
                                          @Name(value = "since", defaultValue = "-1") Long since) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        limit =  abs(limit);

        ZonedDateTime dateTime;
        if (since == -1L) {
            dateTime = ZonedDateTime.now(Clock.systemUTC());
        } else {
            Instant i = Instant.ofEpochSecond(since);
            dateTime = ZonedDateTime.ofInstant(i, UTC);
        }

        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            for (Relationship r1: user.getRelationships(Direction.INCOMING, RelationshipTypes.FOLLOWS)) {
                ZonedDateTime time = (ZonedDateTime)r1.getProperty(TIME);
                if(time.isBefore(dateTime)) {
                    Node follower = r1.getStartNode();
                    Map<String, Object> result = getUserAttributes(follower);
                    result.put(TIME, time);
                    results.add(result);
                }
            }

            tx.commit();
        }
        results.sort(Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder()));
        return results.stream().limit(limit).map(MapResult::new);
    }
}

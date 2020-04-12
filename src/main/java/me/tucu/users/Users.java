package me.tucu.users;

import me.tucu.results.MapResult;
import me.tucu.results.StringResult;
import me.tucu.schema.Labels;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.USER_NOT_FOUND;
import static me.tucu.utils.Time.utc;

public class Users {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    @Procedure(name = "me.tucu.users.get", mode = Mode.READ)
    @Description("CALL me.tucu.users.get(username)")
    public Stream<MapResult> usersGet(@Name(value = "username", defaultValue = "") String username) {
        Map<String, Object> results;
        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, username);
            if (user == null) {
                return Stream.of(USER_NOT_FOUND);
            }
            results = user.getAllProperties();
        }
        return Stream.of(new MapResult(results));
    }

    @Procedure(name = "me.tucu.users.create", mode = Mode.WRITE)
    @Description("CALL me.tucu.users.create(properties)")
    public Stream<MapResult> usersCreate(@Name(value = "properties") Map parameters) {
        Map<String, Object> results = null;
        MapResult validation = UserValidator.validate(parameters);
        if (!validation.isEmpty()) { return  Stream.of(validation); }

        try (Transaction tx = db.beginTx()) {
            Node user = tx.findNode(Labels.User, USERNAME, parameters.get(USERNAME));
            if (user == null) {
                user = tx.findNode(Labels.User, EMAIL, parameters.get(EMAIL));
                if (user == null) {
                    user = tx.createNode(Labels.User);
                    user.setProperty(EMAIL, parameters.get(EMAIL));
                    user.setProperty(NAME, parameters.get(NAME));
                    user.setProperty(USERNAME, parameters.get(USERNAME));
                    user.setProperty(PASSWORD, parameters.get(PASSWORD));
                    user.setProperty(HASH, new Md5Hash(((String)parameters.get(EMAIL)).toLowerCase()).toString());

                    LocalDateTime dateTime = LocalDateTime.now(utc);
                    user.setProperty(TIME, dateTime.truncatedTo(ChronoUnit.DAYS).toEpochSecond(ZoneOffset.UTC));

                    results = user.getAllProperties();
                } else {
                    return  Stream.of(UserExceptions.EXISTING_EMAIL);
                }
            } else {
                return  Stream.of(UserExceptions.EXISTING_USERNAME);
            }
            tx.commit();
        }
        return Stream.of(new MapResult(results));
    }
}

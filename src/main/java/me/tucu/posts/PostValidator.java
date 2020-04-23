package me.tucu.posts;

import me.tucu.results.MapResult;

import java.util.Map;

import static me.tucu.Exceptions.INVALID_INPUT;
import static me.tucu.posts.PostExceptions.EMPTY_STATUS;
import static me.tucu.posts.PostExceptions.MISSING_STATUS;
import static me.tucu.schema.Properties.STATUS;
import static me.tucu.schema.Properties.USERNAME;
import static me.tucu.users.UserExceptions.*;
import static me.tucu.users.UserValidator.USERNAME_PATTERN;

public class PostValidator {

    public static MapResult validate(Map parameters) {
        if (parameters == null) {
            return INVALID_INPUT;
        }
        if (parameters.isEmpty()) {
            return INVALID_INPUT;
        }

        // Check that the username is not empty and matches pattern described above
        if (!parameters.containsKey(STATUS)) {
            return MISSING_STATUS;
        } else {
            String status = (String) parameters.get(STATUS);
            if (status.isBlank()) {
                return EMPTY_STATUS;
            }
        }

        // Check that the username is not empty and matches the username pattern
        if(!parameters.containsKey(USERNAME)) {
            return MISSING_USERNAME;
        } else {
            String username = (String)parameters.get(USERNAME);
            if (username.isBlank()) { return EMPTY_USERNAME; }
            if (!username.matches(USERNAME_PATTERN)) { return INVALID_USERNAME; }
        }

        return MapResult.empty();
    }
}

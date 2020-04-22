package me.tucu.posts;

import me.tucu.results.MapResult;

import java.util.Map;

import static me.tucu.Exceptions.INVALID_INPUT;
import static me.tucu.posts.PostExceptions.EMPTY_STATUS;
import static me.tucu.posts.PostExceptions.MISSING_STATUS;
import static me.tucu.schema.Properties.STATUS;

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

        return MapResult.empty();
    }
}

package me.tucu.users;

import me.tucu.results.MapResult;

import java.util.Map;

import static me.tucu.Exceptions.INVALID_INPUT;
import static me.tucu.schema.Properties.*;
import static me.tucu.users.UserExceptions.*;

public class UserValidator {

    private static final String usernamePattern = "^[a-z][a-z0-9_]{2,31}";

    public static MapResult validate(Map parameters) {
        if(parameters == null) { return INVALID_INPUT; }
        if(parameters.isEmpty()) { return INVALID_INPUT; }

        // Check that the username is not empty and matches pattern described above
        if(!parameters.containsKey(USERNAME)) {
            return MISSING_USERNAME;
        } else {
            String username = (String)parameters.get(USERNAME);
            if (username.isBlank()) { return EMPTY_USERNAME; }
            if (!username.matches(usernamePattern)) { return INVALID_USERNAME; }
        }

        // Check that the email is not empty and has an @ sign
        if(!parameters.containsKey(EMAIL)) {
            return MISSING_EMAIL;
        } else {
            String email = (String)parameters.get(EMAIL);
            if (email.isBlank()) { return EMPTY_EMAIL; }
            if (!email.contains("@")) { return INVALID_EMAIL; }
        }

        // Check that the name is not empty and is not longer than 64 characters
        if(!parameters.containsKey(NAME)) {
            return MISSING_NAME;
        } else {
            String name = (String)parameters.get(NAME);
            if (name.isBlank()) { return EMPTY_NAME; }
            if (name.length() > 64 ) { return INVALID_NAME; }
        }

        // Check that the password exists and is 8 characters or longer
        if(!parameters.containsKey(PASSWORD)) {
            return MISSING_PASSWORD;
        } else {
            String password = (String)parameters.get(PASSWORD);
            if (password.isBlank()) { return EMPTY_PASSWORD; }
            if (password.length() < 8 ) { return INVALID_PASSWORD; }
        }

        return MapResult.empty();
    }
}

package me.tucu.users;

import me.tucu.results.MapResult;

import java.util.Map;

public class UserExceptions {
    public static final MapResult MISSING_USERNAME = new MapResult(Map.of("Error", "Missing username Parameter."));
    public static final MapResult EMPTY_USERNAME = new MapResult(Map.of("Error", "Empty username Parameter."));
    public static final MapResult INVALID_USERNAME = new MapResult(Map.of("Error", "Invalid username Parameter."));
    public static final MapResult EXISTING_USERNAME = new MapResult(Map.of("Error", "Existing username Parameter."));

    public static final MapResult MISSING_EMAIL = new MapResult(Map.of("Error", "Missing email Parameter."));
    public static final MapResult EMPTY_EMAIL = new MapResult(Map.of("Error", "Empty email Parameter."));
    public static final MapResult INVALID_EMAIL = new MapResult(Map.of("Error", "Invalid email Parameter."));
    public static final MapResult EXISTING_EMAIL = new MapResult(Map.of("Error", "Existing email Parameter."));

    public static final MapResult MISSING_NAME = new MapResult(Map.of("Error", "Missing name Parameter."));
    public static final MapResult EMPTY_NAME = new MapResult(Map.of("Error", "Empty name Parameter."));
    public static final MapResult INVALID_NAME = new MapResult(Map.of("Error", "Invalid name Parameter."));

    public static final MapResult MISSING_PASSWORD = new MapResult(Map.of("Error", "Missing password Parameter."));
    public static final MapResult EMPTY_PASSWORD = new MapResult(Map.of("Error", "Empty password Parameter."));
    public static final MapResult INVALID_PASSWORD = new MapResult(Map.of("Error", "Invalid password Parameter."));

    public static final MapResult USER_NOT_FOUND = new MapResult(Map.of("Error", "User not Found."));
}

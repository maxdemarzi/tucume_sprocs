package me.tucu.likes;

import me.tucu.results.MapResult;

import java.util.Map;

public class LikesExceptions {
    public static final MapResult NOT_LIKING = new MapResult(Map.of("Error", "Cannot unlike Post not liked."));
    public static final MapResult ALREADY_LIKES = new MapResult(Map.of("Error", "Cannot like twice."));
    public static final MapResult UNLIKE_TIMEOUT = new MapResult(Map.of("Error", "Cannot unlike Post after timeout."));

}

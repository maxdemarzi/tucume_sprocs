package me.tucu.posts;

import me.tucu.results.MapResult;

import java.util.Map;

public class PostExceptions {
    public static final MapResult POST_NOT_FOUND = new MapResult(Map.of("Error", "Post not Found."));
    public static final MapResult MISSING_STATUS = new MapResult(Map.of("Error", "Missing status Parameter."));
    public static final MapResult EMPTY_STATUS = new MapResult(Map.of("Error", "Empty status Parameter."));
    public static final MapResult POST_ALREADY_REPOSTED = new MapResult(Map.of("Error", "Post already reposted."));

}

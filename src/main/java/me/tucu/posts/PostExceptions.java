package me.tucu.posts;

import me.tucu.results.MapResult;

import java.util.Map;

public class PostExceptions {
    public static final MapResult POST_NOT_FOUND = new MapResult(Map.of("Error", "Post not Found."));
}

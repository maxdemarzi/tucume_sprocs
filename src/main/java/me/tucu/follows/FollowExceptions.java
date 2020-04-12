package me.tucu.follows;

import me.tucu.results.MapResult;

import java.util.Map;

public class FollowExceptions {
    public static final MapResult SELF_FOLLOW = new MapResult(Map.of("Error", "Cannot follow self."));
    public static final MapResult ALREADY_FOLLOW = new MapResult(Map.of("Error", "Cannot follow twice."));

}

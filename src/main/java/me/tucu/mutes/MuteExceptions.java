package me.tucu.mutes;

import me.tucu.results.MapResult;

import java.util.Map;

public class MuteExceptions {
    public static final MapResult SELF_MUTE = new MapResult(Map.of("Error", "Cannot mute self."));
    public static final MapResult SELF_UNMUTE = new MapResult(Map.of("Error", "Cannot unmute self."));
    public static final MapResult NOT_MUTED = new MapResult(Map.of("Error", "Cannot unmute user not muted."));
    public static final MapResult ALREADY_MUTED = new MapResult(Map.of("Error", "Cannot mute twice."));

}

package me.tucu;

import me.tucu.results.MapResult;

import java.util.Map;

public class Exceptions {

    public static final MapResult INVALID_INPUT = new MapResult(Map.of("Error", "Invalid Input."));
    public static final MapResult INSUFFICIENT_FUNDS = new MapResult(Map.of("Error", "Insufficient Funds."));
}

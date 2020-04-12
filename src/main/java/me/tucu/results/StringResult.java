package me.tucu.results;

public class StringResult {
    public final static me.tucu.results.StringResult EMPTY = new me.tucu.results.StringResult(null);

    public final String value;

    public StringResult(String value) {
        this.value = value;
    }
}
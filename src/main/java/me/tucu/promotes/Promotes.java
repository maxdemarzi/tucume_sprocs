package me.tucu.promotes;

import java.util.regex.Pattern;

public class Promotes {

    // Dollar Sign followed by a character, followed by up to 31 more characters and numbers
    private static final Pattern PROMOTES_PATTERN = Pattern.compile("\\$[a-z][a-z0-9_]{2,31}");
}

package me.tucu.utils;

import org.parboiled.common.ImmutableList;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static me.tucu.schema.Properties.*;

public class Comparators {

    public static final Comparator<Map<String, Object>> LABEL_COMPARATOR = new Comparator<>() {
        final List<String> typeOrder = ImmutableList.of("user","product","post");

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            return typeOrder.indexOf(o1.get(LABEL)) - typeOrder.indexOf(o2.get(LABEL));
        }
    };

    public static final Comparator<Map<String, Object>> DESC_TIME_COMPARATOR =
            (o1, o2) -> ((ZonedDateTime)o2.getOrDefault(REPOSTED_TIME,o2.get(TIME))).compareTo(
            (ZonedDateTime)o1.getOrDefault(REPOSTED_TIME, o1.get(TIME)));

}

package me.tucu.utils;

import org.parboiled.common.ImmutableList;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.reverseOrder;
import static me.tucu.schema.Properties.LABEL;
import static me.tucu.schema.Properties.TIME;

public class Comparators {

    public static final Comparator<Map<String, Object>> TIME_COMPARATOR = Comparator.comparing(m -> (ZonedDateTime) m.get(TIME), reverseOrder());

    public static final Comparator<Map<String, Object>> LABEL_COMPARATOR = new Comparator<>() {
        final List<String> typeOrder = ImmutableList.of("user","product","post");

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            return typeOrder.indexOf(o1.get(LABEL)) - typeOrder.indexOf(o2.get(LABEL));
        }

    };

}

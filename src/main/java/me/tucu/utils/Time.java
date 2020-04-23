package me.tucu.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;

public class Time {
    public static final ZoneId utc = TimeZone.getTimeZone("UTC").toZoneId();

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofPattern("yyyy_MM_dd")
            .withZone(utc);


    public static ZonedDateTime getLatestTime(Long since) {
        if (since == -1L) {
            return ZonedDateTime.now(Clock.systemUTC());
        } else {
            Instant i = Instant.ofEpochSecond(since);
            return ZonedDateTime.ofInstant(i, UTC);
        }
    }
}

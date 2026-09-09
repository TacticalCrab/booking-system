package com.example.bookingsystem.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public final class TestDateTimeFactory {

    private TestDateTimeFactory() {
    }

    public static LocalDateTime future(
            DayOfWeek dayOfWeek,
            int hour,
            int minute
    ) {
        return LocalDate.now()
                .with(TemporalAdjusters.next(dayOfWeek))
                .atTime(hour, minute);
    }

    public static LocalDateTime future(
            DayOfWeek dayOfWeek,
            LocalTime time
    ) {
        return LocalDate.now()
                .with(TemporalAdjusters.next(dayOfWeek))
                .atTime(time);
    }

    public static LocalDateTime past(
            DayOfWeek dayOfWeek,
            int hour,
            int minute
    ) {
        return LocalDate.now()
                .with(TemporalAdjusters.previous(dayOfWeek))
                .atTime(hour, minute);
    }

    public static LocalDateTime daysFromNow(
            long days,
            int hour,
            int minute
    ) {
        return LocalDate.now()
                .plusDays(days)
                .atTime(hour, minute);
    }
}

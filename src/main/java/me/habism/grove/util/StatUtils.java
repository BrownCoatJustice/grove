package me.habism.grove.util;

import me.habism.grove.UserStats;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class StatUtils {

    private StatUtils() {
        // Utility class: no instantiation
    }

    public static void updateFocusStreak(UserStats stats) {
        LocalDate today = LocalDate.now();
        LocalDate lastDate = stats.lastFocusDate.isEmpty()
                ? today.minusDays(1)
                : LocalDate.parse(stats.lastFocusDate);

        long daysBetween = ChronoUnit.DAYS.between(lastDate, today);

        if (daysBetween == 1) {
            stats.focusStreak++;
        } else if (daysBetween > 1) {
            stats.focusStreak = 1;
        }

        stats.lastFocusDate = today.toString();
    }
}

package com.app.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Set;

class CalendarUtility {
    // Holidays that occur on same date each year. If they fall on a weekend, observe them on the nearest weekday
    private static final Set<MonthDay> observedHolidays = Set.of(
            MonthDay.of(Month.JULY, 4)
    );

    // Holidays that occur on different dates each year
    private static final Map<Month, TemporalAdjuster> dynamicHolidays = Map.of(
            Month.SEPTEMBER, TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)
    );

    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day.equals(DayOfWeek.SATURDAY) || day.equals(DayOfWeek.SUNDAY);
    }

    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }

    public static boolean isHoliday(LocalDate date) {
        if (isObservedHoliday(date) && isWeekend(date)) {
            return false;
        }
        return isObservedHoliday(date) || isClosestWeekdayToObservedHoliday(date) || isDynamicHoliday(date);
    }

    private static boolean isObservedHoliday(LocalDate date) {
        return observedHolidays.stream()
                .anyMatch(h -> h.getMonth() == date.getMonth() && h.getDayOfMonth() == date.getDayOfMonth());
    }

    private static boolean isDynamicHoliday(LocalDate date) {
        TemporalAdjuster adjuster = dynamicHolidays.get(date.getMonth());

        if (adjuster != null) {
            LocalDate holiday = date.with(adjuster);
            return date.getMonth() == holiday.getMonth() && date.getDayOfMonth() == holiday.getDayOfMonth();
        }
        return false;
    }

    private static boolean isClosestWeekdayToObservedHoliday(LocalDate date) {
        return isFridayBeforeSaturdayHoliday(date) || isMondayAfterSundayHoliday(date);
    }

    private static boolean isFridayBeforeSaturdayHoliday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.FRIDAY && isObservedHoliday(date.plusDays(1));
    }

    private static boolean isMondayAfterSundayHoliday(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.MONDAY && isObservedHoliday(date.minusDays(1));
    }
}

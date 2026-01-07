package com.automation.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Utility class for calendar and date-related operations.
 * Provides methods to get formatted month names for current and previous months.
 */
@UtilityClass
public class CalendarUtilities {

    /**
     * Gets the current month name in full lowercase format (e.g., "november")
     *
     * @return Current month name in full lowercase
     */
    public String getCurrentMonth() {
        return LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase();
    }

    /**
     * Gets the previous month name in full lowercase format (e.g., "october")
     *
     * @return Previous month name in full lowercase
     */
    public String getPreviousMonth() {
        return LocalDate.now().minusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase();
    }

    /**
     * Gets the current month as a three-letter uppercase abbreviation (e.g., "NOV")
     *
     * @return Current month as three-letter uppercase abbreviation
     */
    public String getCurrentMonthThreeLetterCaps() {
        return LocalDate.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
    }

    /**
     * Gets the previous month as a three-letter uppercase abbreviation (e.g., "OCT")
     *
     * @return Previous month as three-letter uppercase abbreviation
     */
    public String getPreviousMonthThreeLetterCaps() {
        return LocalDate.now().minusMonths(1).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
    }
}
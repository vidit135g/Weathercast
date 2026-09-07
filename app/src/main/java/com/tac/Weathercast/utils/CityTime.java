package com.tac.Weathercast.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Holds the UTC offset of the currently shown city (from the OWM "timezone"
 * field) so clock times — sunrise, sunset, hourly slots — display in the
 * city's local time rather than the phone's.
 */
public final class CityTime {

    private static volatile int offsetSec = Integer.MIN_VALUE;

    private CityTime() {}

    public static void setOffsetSeconds(int seconds) { offsetSec = seconds; }
    public static boolean isSet() { return offsetSec != Integer.MIN_VALUE; }

    public static TimeZone zone() {
        return isSet() ? new SimpleTimeZone(offsetSec * 1000, "city") : TimeZone.getDefault();
    }

    public static SimpleDateFormat format(String pattern) {
        SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.getDefault());
        f.setTimeZone(zone());
        return f;
    }

    public static String hm(Date d) { return d == null ? "--:--" : format("HH:mm").format(d); }

    public static Calendar calendar(Date d) {
        Calendar c = Calendar.getInstance(zone());
        if (d != null) c.setTime(d);
        return c;
    }

    public static int hourOfDay(Date d) { return calendar(d).get(Calendar.HOUR_OF_DAY); }
}

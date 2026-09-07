package com.tac.Weathercast.utils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * A clear-physics estimate of the current UV index from the sun's elevation and
 * the cloud cover — the standalone OWM UV endpoint was retired, and this tracks
 * the real daily curve (0 at night, peak at solar noon, cut by cloud).
 */
public final class UvEstimate {

    private UvEstimate() {}

    /** @return UV index, 0–12, for right now at this location and sky. */
    public static double now(double lat, double lon, int owmId) {
        return at(System.currentTimeMillis(), lat, lon, owmId);
    }

    public static double at(long utcMillis, double lat, double lon, int owmId) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(utcMillis);
        int doy = c.get(Calendar.DAY_OF_YEAR);
        double utcHours = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60.0;

        double declRad = Math.toRadians(-23.45 * Math.cos(Math.toRadians(360.0 / 365.0 * (doy + 10))));
        double solarTime = utcHours + lon / 15.0;                 // ~local solar time
        double hourAngle = Math.toRadians(15.0 * (solarTime - 12.0));
        double latRad = Math.toRadians(lat);

        double sinElev = Math.sin(latRad) * Math.sin(declRad)
                + Math.cos(latRad) * Math.cos(declRad) * Math.cos(hourAngle);
        if (sinElev <= 0.02) return 0;                             // sun at/below horizon

        double clearSky = 12.0 * Math.pow(sinElev, 1.6);           // ~12 overhead in the tropics
        double cloud = cloudFactor(owmId);
        double uv = clearSky * cloud;
        return Math.max(0, Math.min(12, Math.round(uv * 10) / 10.0));
    }

    private static double cloudFactor(int owmId) {
        switch (owmId) {
            case 800: return 1.00;
            case 801: return 0.92;
            case 802: return 0.78;
            case 803: return 0.58;
            case 804: return 0.42;
        }
        int g = owmId / 100;
        if (g == 2) return 0.25;   // thunderstorm
        if (g == 3 || g == 5) return 0.32;   // drizzle / rain
        if (g == 6) return 0.55;   // snow
        if (g == 7) return 0.5;    // haze / fog / mist
        return 0.7;
    }
}

package com.tac.Weathercast.utils;

import com.tac.Weathercast.models.Weather;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * "Plan your day" — finds the most pleasant outdoor window in the next ~15 hours
 * and the golden-hour times. Uses only the 3-hourly forecast the app already has.
 */
public final class DayPlanner {

    public static final class Plan {
        public final String windowRange;   // "14:00 – 17:00"  (null if none good)
        public final String windowWhy;     // "mild, dry, light breeze"
        public final String goldenAm;      // "06:04 – 06:54"
        public final String goldenPm;      // "17:32 – 18:22"

        Plan(String r, String w, String ga, String gp) {
            windowRange = r; windowWhy = w; goldenAm = ga; goldenPm = gp;
        }
    }

    private DayPlanner() {}

    /** Score a single slot 0..100 for "nice to be outside". */
    private static int score(double tempC, double windMs, double rainMm, double uv, int owmId) {
        int s = 100;
        s -= (int) Math.min(45, Math.abs(tempC - 20) * 3.2);      // comfort around 20°C
        s -= (int) Math.min(40, rainMm * 22);                      // any rain hurts
        if (owmId >= 200 && owmId < 600) s -= 25;                  // wet weather codes
        s -= (int) Math.min(25, Math.max(0, windMs - 4) * 4);     // breezy penalty
        if (uv >= 8) s -= 12;
        return Math.max(0, s);
    }

    public static Plan build(List<Weather> hourly, Date sunrise, Date sunset) {
        SimpleDateFormat hm = CityTime.format("HH:mm");
        String ga = null, gp = null;
        if (sunrise != null) ga = hm.format(sunrise) + " – " + hm.format(new Date(sunrise.getTime() + 50 * 60000L));
        if (sunset != null)  gp = hm.format(new Date(sunset.getTime() - 50 * 60000L)) + " – " + hm.format(sunset);

        String range = null, why = null;
        try {
            long now = System.currentTimeMillis();
            int bestScore = -1, bestI = -1;
            double bestT = 0, bestW = 0; boolean bestDry = true;
            for (int i = 0; i < hourly.size() && i < 6; i++) {
                Weather w = hourly.get(i);
                if (w.getDate().getTime() < now - 3600_000L) continue;
                int h = CityTime.hourOfDay(w.getDate());
                if (h < 6 || h > 21) continue;                     // daytime only (city local)
                double t = Double.parseDouble(w.getTemperature()) - 273.15;
                double wind = num(w.getWind(), 0);
                double rain = num(w.getRain(), 0);
                double uv = w.getUvIndex();
                int id; try { id = Integer.parseInt(w.getId()); } catch (Exception e) { id = 800; }
                int sc = score(t, wind, rain, uv, id);
                if (sc > bestScore) {
                    bestScore = sc; bestI = i; bestT = t; bestW = wind; bestDry = rain < 0.1 && !(id >= 200 && id < 600);
                }
            }
            if (bestI >= 0 && bestScore >= 45) {
                Date start = hourly.get(bestI).getDate();
                Date end = new Date(start.getTime() + 3 * 3600_000L);
                range = hm.format(start) + " – " + hm.format(end);
                String temp = Math.round(bestT) + "°";
                why = (bestT >= 24 ? "warm" : bestT >= 15 ? "mild" : "cool") + ", "
                        + (bestDry ? "dry" : "some rain") + ", "
                        + (bestW < 4 ? "light breeze" : "breezy") + " · around " + temp;
            }
        } catch (Exception ignored) {}

        return new Plan(range, why, ga, gp);
    }

    private static double num(String s, double d) {
        try { return Double.parseDouble(s); } catch (Exception e) { return d; }
    }
}

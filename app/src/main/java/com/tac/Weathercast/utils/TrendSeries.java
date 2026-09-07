package com.tac.Weathercast.utils;

import android.content.SharedPreferences;

import com.tac.Weathercast.models.Weather;

import java.util.List;

/** One extracted forecast metric over the whole 5-day window, with stats and a
 *  plain-English reading. Feeds both the Trends cards and their detail screens. */
public final class TrendSeries {

    public enum Type { TEMP, RAIN, PRESSURE, WIND, HUMIDITY }

    public final Type type;
    public String title, unit;
    public float[] values = new float[0];
    public long[] times = new long[0];
    public float min, max, avg, sum, first, last, now;
    public int minIdx, maxIdx, nowIdx;
    public String reading = "";
    public String trendWord = "steady";     // "rising" / "falling" / "steady"
    public boolean bars;                     // render as bars (rain) vs line

    private TrendSeries(Type t) { type = t; }

    private static double d(String s, double dflt) {
        try { return Double.parseDouble(s); } catch (Exception e) { return dflt; }
    }

    public static TrendSeries of(Type type, List<Weather> data, SharedPreferences sp) {
        TrendSeries s = new TrendSeries(type);
        if (data == null || data.isEmpty()) return s;

        int n = data.size();
        float[] v = new float[n]; long[] tm = new long[n];
        String tu = sp.getString("unit", "°C");
        String pu = sp.getString("pressureUnit", "hPa");
        String su = sp.getString("speedUnit", "m/s");

        for (int i = 0; i < n; i++) {
            Weather w = data.get(i);
            tm[i] = w.getDate() != null ? w.getDate().getTime() : 0;
            float val;
            switch (type) {
                case TEMP:
                    val = safe(UnitConvertor.convertTemperature((float) d(w.getTemperature(), 288.15), sp), v, i); break;
                case RAIN:
                    val = (float) Math.max(0, d(w.getRain(), 0)); break;
                case PRESSURE:
                    val = safe((float) UnitConvertor.convertPressure((float) d(w.getPressure(), 1013), sp), v, i); break;
                case WIND:
                    val = safe((float) UnitConvertor.convertWind(d(w.getWind(), 0), sp), v, i); break;
                default: // HUMIDITY
                    val = (float) d(w.getHumidity(), 50); break;
            }
            v[i] = val;
        }
        s.values = v; s.times = tm;
        s.first = v[0]; s.last = v[n - 1];
        long nowMs = System.currentTimeMillis();
        s.nowIdx = 0;
        for (int i = 0; i < n; i++) { if (tm[i] >= nowMs - 3600_000L) { s.nowIdx = i; break; } }
        s.now = v[s.nowIdx];
        s.min = Float.MAX_VALUE; s.max = -Float.MAX_VALUE;
        double total = 0;
        for (int i = 0; i < n; i++) {
            total += v[i];
            if (v[i] < s.min) { s.min = v[i]; s.minIdx = i; }
            if (v[i] > s.max) { s.max = v[i]; s.maxIdx = i; }
        }
        s.sum = (float) total;
        s.avg = (float) (total / n);

        switch (type) {
            case TEMP:      s.title = "Temperature"; s.unit = tu; break;
            case RAIN:      s.title = "Rainfall";    s.unit = "mm"; s.bars = true; break;
            case PRESSURE:  s.title = "Pressure";    s.unit = pu; break;
            case WIND:      s.title = "Wind speed";  s.unit = su; break;
            default:        s.title = "Humidity";    s.unit = "%"; break;
        }

        if (type == Type.RAIN) {
            float soon = 0;
            for (int i = s.nowIdx; i < Math.min(n, s.nowIdx + 8); i++) soon += v[i];
            s.trendWord = soon > 1f ? "more coming" : (s.now > 0.1f ? "easing" : "dry ahead");
        } else {
            float delta = s.last - s.first;
            float span = Math.max(0.001f, s.max - s.min);
            boolean flat = Math.abs(delta) < span * 0.18f;
            s.trendWord = flat ? "steady" : (delta > 0 ? "rising" : "falling");
        }

        s.reading = reading(s, dayName(s.times[s.maxIdx]), dayName(s.times[s.minIdx]));
        return s;
    }

    private static float safe(float x, float[] prev, int i) {
        if (Float.isNaN(x) || Float.isInfinite(x)) return i > 0 ? prev[i - 1] : 0f;
        return x;
    }

    private static String dayName(long ms) {
        if (ms == 0) return "";
        return CityTime.format("EEEE").format(new java.util.Date(ms));
    }

    private static String reading(TrendSeries s, String peakDay, String lowDay) {
        int mn = Math.round(s.min), mx = Math.round(s.max), av = Math.round(s.avg);
        switch (s.type) {
            case TEMP:
                if ("steady".equals(s.trendWord))
                    return "Temperatures hold near " + av + s.unit + " across the week, between "
                            + mn + " and " + mx + s.unit + ".";
                return ("rising".equals(s.trendWord) ? "Warming up" : "Cooling down")
                        + " through the week — from " + Math.round(s.first) + s.unit + " now to a "
                        + ("rising".equals(s.trendWord) ? "peak of " + mx : "low of " + mn) + s.unit
                        + (peakDay.isEmpty() ? "" : " around " + peakDay) + ".";
            case RAIN:
                if (s.sum < 0.5f) return "Dry — no meaningful rain in the 5-day forecast.";
                return "About " + fmt(s.sum) + " mm of rain expected over five days, heaviest "
                        + (peakDay.isEmpty() ? "mid-week" : "on " + peakDay) + ".";
            case PRESSURE:
                if ("steady".equals(s.trendWord))
                    return "Pressure stays around " + av + " " + s.unit + " — settled, little change expected.";
                return "rising".equals(s.trendWord)
                        ? "Pressure climbs from " + Math.round(s.first) + " to " + mx + " " + s.unit
                          + " — conditions should settle and clear."
                        : "Pressure falls from " + Math.round(s.first) + " to " + mn + " " + s.unit
                          + " — unsettled, wetter weather more likely.";
            case WIND:
                return "Winds run " + mn + "–" + mx + " " + s.unit + ", strongest "
                        + (peakDay.isEmpty() ? "later in the week" : "on " + peakDay) + ".";
            default:
                if (mx >= 80) return "Humid — up to " + mx + "%, muggiest " + (peakDay.isEmpty() ? "mid-week" : "on " + peakDay) + ".";
                if (mn <= 30) return "Dry air — down to " + mn + "%. Keep hydrated.";
                return "Humidity sits between " + mn + " and " + mx + "%, comfortable most of the week.";
        }
    }

    private static String fmt(float x) { return new java.text.DecimalFormat("0.#").format(x); }
}

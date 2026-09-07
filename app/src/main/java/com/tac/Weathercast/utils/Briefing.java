package com.tac.Weathercast.utils;

import com.tac.Weathercast.models.Weather;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Turns the raw forecast into a set of plain-English "briefing" insights —
 * pressure tendency, rain timing, temperature swing, wind outlook, UV window,
 * comfort, a today-vs-tomorrow comparison and a moon reading.
 * Pure logic, no Android / network dependencies beyond the Weather model.
 */
public final class Briefing {

    public static final class Insight {
        public final String key;       // "pressure", "rain", "temp", "wind", "uv", "comfort"
        public final String headline;
        public final String detail;
        public Insight(String k, String h, String d) { key = k; headline = h; detail = d; }
    }

    public static final class Compare {
        public String label = "Tomorrow";
        public double todayHi, todayLo, tmrwHi, tmrwLo;
        public double todayRain, tmrwRain;
        public String delta = "";          // "3° warmer, drier"
    }

    public static final class Moon {
        public String phase = "";
        public int illumination;           // %
        public int emojiIndex;             // 0..7
        public boolean waxing = true;
        public String nextFull = "";
    }

    public static final class Result {
        public final List<Insight> insights = new ArrayList<>();
        public Compare compare;
        public Moon moon;
        public String narrative = "";      // one-line day-ahead summary
        public long daylightMin, daylightDeltaMin;
    }

    private Briefing() {}

    private static double d(String s, double dflt) {
        try { return Double.parseDouble(s); } catch (Exception e) { return dflt; }
    }
    private static double c(String kelvin) { return d(kelvin, 273.15 + 15) - 273.15; }

    /**
     * @param today     the current conditions
     * @param todayHrs  today's 3-hourly slots (may be empty)
     * @param daily     one entry per upcoming day (index 0 == today or tomorrow)
     */
    public static Result build(Weather today, List<Weather> todayHrs, List<Weather> daily) {
        Result r = new Result();
        if (today == null) return r;

        double nowC = c(today.getTemperature());
        double hum = d(today.getHumidity(), 50);
        double windMs = d(today.getWind(), 0);
        int owm; try { owm = Integer.parseInt(today.getId()); } catch (Exception e) { owm = 800; }

        // ---- daylight + delta ----
        try {
            long rise = today.getSunrise().getTime(), set = today.getSunset().getTime();
            r.daylightMin = Math.max(0, (set - rise) / 60000L);
            // rough: each day near equinox changes a few minutes; approximate from daily list unavailable,
            // so estimate via a small model is skipped — delta stays 0 unless we can compute it later.
        } catch (Exception ignored) {}

        // ---- pressure tendency ----
        Double pStart = firstPressure(todayHrs), pEnd = lastPressure(todayHrs);
        if (pStart == null) pStart = d(today.getPressure(), 1013);
        if (pEnd != null && pStart != null) {
            double dp = pEnd - pStart;
            if (dp <= -3)
                r.insights.add(new Insight("pressure", "Pressure is falling",
                        "A drop of " + Math.round(Math.abs(dp)) + " hPa — unsettled or wetter weather is likely within a day."));
            else if (dp >= 3)
                r.insights.add(new Insight("pressure", "Pressure is rising",
                        "Up " + Math.round(dp) + " hPa — skies should settle and clear."));
            else
                r.insights.add(new Insight("pressure", "Pressure is steady",
                        "Little change — expect more of the same for now."));
        }

        // ---- rain timing (today's 3-hourly) ----
        String rainWhen = null;
        for (Weather w : safe(todayHrs)) {
            int id; try { id = Integer.parseInt(w.getId()); } catch (Exception e) { continue; }
            if (id >= 200 && id < 700) {
                Calendar cal = Calendar.getInstance(); cal.setTime(w.getDate());
                rainWhen = String.format("%02d:00", cal.get(Calendar.HOUR_OF_DAY));
                break;
            }
        }
        boolean wetNow = owm >= 200 && owm < 700;
        if (wetNow)
            r.insights.add(new Insight("rain", "Wet now",
                    rainWhen != null ? "More showers around " + rainWhen + "." : "Showers easing through the day."));
        else if (rainWhen != null)
            r.insights.add(new Insight("rain", "Rain arriving ~" + rainWhen,
                    "Dry until then — plan outdoor time for the morning."));
        else
            r.insights.add(new Insight("rain", "Staying dry",
                    "No rain in the forecast for the rest of today."));

        // ---- temperature swing today ----
        double hi = nowC, lo = nowC; String hiAt = null, loAt = null;
        for (Weather w : safe(todayHrs)) {
            double t = c(w.getTemperature());
            Calendar cal = Calendar.getInstance(); cal.setTime(w.getDate());
            String at = String.format("%02d:00", cal.get(Calendar.HOUR_OF_DAY));
            if (t > hi) { hi = t; hiAt = at; }
            if (t < lo) { lo = t; loAt = at; }
        }
        if (hiAt != null || loAt != null) {
            String s = "";
            if (hiAt != null) s += "Warmest near " + hiAt + " at " + Math.round(hi) + "°";
            if (loAt != null) s += (s.isEmpty() ? "Coolest" : "; coolest") + " near " + loAt + " at " + Math.round(lo) + "°";
            r.insights.add(new Insight("temp", "A " + Math.round(hi - lo) + "° swing today", s + "."));
        }

        // ---- wind outlook ----
        Double wStart = firstWind(todayHrs), wEnd = lastWind(todayHrs);
        if (wStart != null && wEnd != null) {
            double dw = wEnd - wStart;
            if (dw >= 2)
                r.insights.add(new Insight("wind", "Wind picking up",
                        "Rising to about " + Math.round(wEnd) + " m/s later — secure loose items outside."));
            else if (dw <= -2)
                r.insights.add(new Insight("wind", "Wind easing",
                        "Dropping to about " + Math.round(wEnd) + " m/s by evening — calmer later."));
            else if (windMs >= 8)
                r.insights.add(new Insight("wind", "Breezy all day",
                        "Around " + Math.round(windMs) + " m/s, steady."));
        }

        // ---- UV window ----
        double uv = today.getUvIndex();
        if (uv >= 3) {
            try {
                Calendar rs = Calendar.getInstance(); rs.setTime(today.getSunrise());
                Calendar st = Calendar.getInstance(); st.setTime(today.getSunset());
                int h1 = Math.max(rs.get(Calendar.HOUR_OF_DAY) + 2, 9);
                int h2 = Math.min(st.get(Calendar.HOUR_OF_DAY) - 2, 16);
                r.insights.add(new Insight("uv",
                        "UV peaks at " + Math.round(uv) + " (" + UnitConvertor.convertUvIndexToRiskLevel(uv) + ")",
                        "Strongest roughly " + h1 + ":00–" + h2 + ":00 — cover up or seek shade."));
            } catch (Exception ignored) {}
        }

        // ---- comfort ----
        String comfort, cd;
        if (hum >= 78 && nowC >= 22) { comfort = "Muggy"; cd = "High humidity makes " + Math.round(nowC) + "° feel heavier — take it easy."; }
        else if (nowC <= 6) { comfort = "Cold"; cd = "Wrap up — it's " + Math.round(nowC) + "° and the air will bite in the wind."; }
        else if (hum <= 28) { comfort = "Dry air"; cd = "Humidity is only " + Math.round(hum) + "% — keep hydrated, moisturise."; }
        else { comfort = "Comfortable"; cd = "Around " + Math.round(nowC) + "° with easy humidity — a good day to be out."; }
        r.insights.add(new Insight("comfort", comfort, cd));

        // ---- today vs tomorrow ----
        r.compare = compare(todayHrs, daily);

        // ---- moon ----
        r.moon = moon();

        // ---- narrative ----
        r.narrative = narrative(owm, rainWhen, wetNow, Math.round(hi), Math.round(lo));

        return r;
    }

    /** {@code todayHrs} = today's 3-hourly slots, {@code future} = later days' 3-hourly slots. */
    private static Compare compare(List<Weather> todayHrs, List<Weather> future) {
        Compare cmp = new Compare();
        int todayDoy = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        double tHi = -999, tLo = 999, tRain = 0; boolean today = false;
        for (Weather w : safe(todayHrs)) {
            double t = c(w.getTemperature());
            tHi = Math.max(tHi, t); tLo = Math.min(tLo, t); tRain += d(w.getRain(), 0); today = true;
        }
        double mHi = -999, mLo = 999, mRain = 0; boolean tmrw = false;
        for (Weather w : safe(future)) {
            Calendar wc = Calendar.getInstance(); wc.setTime(w.getDate());
            if (wc.get(Calendar.DAY_OF_YEAR) != (todayDoy % 365) + 1 && wc.get(Calendar.DAY_OF_YEAR) != todayDoy + 1) continue;
            double t = c(w.getTemperature());
            mHi = Math.max(mHi, t); mLo = Math.min(mLo, t); mRain += d(w.getRain(), 0); tmrw = true;
        }
        if (!tmrw) { // fall back to the very next slots available
            int taken = 0;
            for (Weather w : safe(future)) {
                double t = c(w.getTemperature());
                mHi = Math.max(mHi, t); mLo = Math.min(mLo, t); mRain += d(w.getRain(), 0);
                if (++taken >= 8) break;
            }
            tmrw = taken > 0;
        }
        if (!today || !tmrw) return cmp;

        cmp.todayHi = tHi; cmp.todayLo = tLo; cmp.todayRain = tRain;
        cmp.tmrwHi = mHi;  cmp.tmrwLo = mLo;  cmp.tmrwRain = mRain;
        int warm = (int) Math.round(mHi - tHi);
        StringBuilder s = new StringBuilder();
        if (warm >= 2) s.append(warm).append("° warmer");
        else if (warm <= -2) s.append(Math.abs(warm)).append("° cooler");
        else s.append("a similar temperature");
        if (mRain > tRain + 0.5) s.append(", wetter");
        else if (mRain < tRain - 0.5) s.append(", drier");
        cmp.delta = "Tomorrow looks " + s + ".";
        return cmp;
    }

    private static final String[] MOON_NAMES = {
        "New moon", "Waxing crescent", "First quarter", "Waxing gibbous",
        "Full moon", "Waning gibbous", "Last quarter", "Waning crescent" };

    private static Moon moon() {
        Moon m = new Moon();
        double synodic = 29.530588853;
        // reference new moon: 2000-01-06 18:14 UTC in Julian days
        double jd = System.currentTimeMillis() / 86400000.0 + 2440587.5;
        double age = (jd - 2451550.1) % synodic;
        if (age < 0) age += synodic;
        double frac = age / synodic;
        m.emojiIndex = (int) Math.floor(frac * 8 + 0.5) % 8;
        m.phase = MOON_NAMES[m.emojiIndex];
        m.waxing = frac < 0.5;
        m.illumination = (int) Math.round((1 - Math.cos(2 * Math.PI * frac)) / 2 * 100);
        double toFull = (0.5 - frac) * synodic;
        if (toFull < 0) toFull += synodic;
        m.nextFull = toFull < 1 ? "Tonight" : Math.round(toFull) + " days";
        return m;
    }

    private static String narrative(int owm, String rainWhen, boolean wetNow, long hi, long lo) {
        if (wetNow) return "Wet start; showers gradually ease. High " + hi + "°, low " + lo + "°.";
        if (rainWhen != null) return "Dry morning, rain moving in around " + rainWhen + ". High " + hi + "°.";
        if (owm == 800) return "Clear through the day. High " + hi + "°, low " + lo + "° overnight.";
        if (owm > 802) return "Cloud holds most of the day. High " + hi + "°, low " + lo + "°.";
        return "Mixed sun and cloud. High " + hi + "°, low " + lo + "°.";
    }

    // --- small helpers over the 3-hourly list ---
    private static List<Weather> safe(List<Weather> l) { return l == null ? new ArrayList<>() : l; }
    private static Double firstPressure(List<Weather> l) { for (Weather w : safe(l)) { double v = d(w.getPressure(), -1); if (v > 0) return v; } return null; }
    private static Double lastPressure(List<Weather> l) { Double v = null; for (Weather w : safe(l)) { double p = d(w.getPressure(), -1); if (p > 0) v = p; } return v; }
    private static Double firstWind(List<Weather> l) { for (Weather w : safe(l)) { double v = d(w.getWind(), -1); if (v >= 0) return v; } return null; }
    private static Double lastWind(List<Weather> l) { Double v = null; for (Weather w : safe(l)) { double p = d(w.getWind(), -1); if (p >= 0) v = p; } return v; }
}

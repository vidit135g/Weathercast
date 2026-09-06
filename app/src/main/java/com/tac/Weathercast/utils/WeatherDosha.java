package com.tac.Weathercast.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Ayurvedic reading of the live weather, ported from the Soma design system's
 * {@code features/weather/weatherDosha.ts}.
 *
 * Weather is read as a set of qualities (guna) that push the three doshas:
 *   cold + dry + windy   -> aggravates VATA
 *   hot + bright + humid  -> aggravates PITTA
 *   cold + damp + still   -> aggravates KAPHA
 */
public final class WeatherDosha {

    public static final class Reading {
        public final String dosha;       // "vata" | "pitta" | "kapha" | null
        public final int glowColor;      // ARGB glow tint
        public final String headline;
        public final String advice;
        public final List<String> qualities;

        Reading(String dosha, int glowColor, String headline, String advice, List<String> qualities) {
            this.dosha = dosha;
            this.glowColor = glowColor;
            this.headline = headline;
            this.advice = advice;
            this.qualities = qualities;
        }
    }

    private WeatherDosha() {}

    /** WMO/OWM condition id -> short label. */
    private static String describe(int owmId) {
        if (owmId >= 200 && owmId < 300) return "Thunderstorm";
        if (owmId >= 300 && owmId < 400) return "Drizzle";
        if (owmId >= 500 && owmId < 600) return "Rain";
        if (owmId >= 600 && owmId < 700) return "Snow";
        if (owmId >= 700 && owmId < 800) return "Haze";
        if (owmId == 800) return "Clear sky";
        if (owmId > 800) return "Clouds";
        return "Unsettled";
    }

    /**
     * @param tempC       air temperature in Celsius
     * @param feelsLikeC  apparent temperature in Celsius
     * @param humidity    relative humidity, %
     * @param windMs      wind speed, m/s
     * @param uvIndex     UV index (may be -1 when unknown)
     * @param owmId       OpenWeatherMap condition id
     */
    public static Reading read(double tempC, double feelsLikeC, double humidity,
                               double windMs, double uvIndex, int owmId) {
        String label = describe(owmId);
        double windKph = windMs * 3.6;
        boolean raining = owmId >= 300 && owmId < 600;
        boolean snowing = owmId >= 600 && owmId < 700;

        List<String> q = new ArrayList<>();
        if (feelsLikeC <= 14) q.add("cold");
        else if (feelsLikeC >= 30) q.add("hot");
        if (humidity <= 38) q.add("dry");
        else if (humidity >= 78 || raining) q.add(raining ? "damp" : "humid");
        if (windKph >= 22) q.add("windy");
        else if (windKph <= 6) q.add("still");
        if (uvIndex >= 7) q.add("bright");

        int vata = 0, pitta = 0, kapha = 0;
        if (q.contains("cold")) { vata += 2; kapha += 1; }
        if (q.contains("dry")) vata += 2;
        if (q.contains("windy")) vata += 2;
        if (q.contains("hot")) pitta += 2;
        if (q.contains("bright")) pitta += 2;
        if (q.contains("humid")) { pitta += 1; kapha += 1; }
        if (q.contains("damp")) kapha += 2;
        if (q.contains("still") && (q.contains("cold") || q.contains("damp"))) kapha += 1;
        if (snowing) { kapha += 1; vata += 1; }

        String top = "vata";
        int topScore = vata, second = Math.min(pitta, kapha);
        if (pitta >= topScore) { top = "pitta"; topScore = pitta; }
        if (kapha >= topScore) { top = "kapha"; topScore = kapha; }
        // second highest
        int[] scores = { vata, pitta, kapha };
        java.util.Arrays.sort(scores);
        second = scores[1];

        String dosha = (topScore >= 3 && topScore - second >= 1) ? top : null;

        String tempTxt = Math.round(tempC) + "°";
        StringBuilder phrase = new StringBuilder();
        for (String x : q) {
            if (phrase.length() > 0) phrase.append(", ");
            phrase.append(x);
        }

        String headline, advice;
        int glow;
        if (dosha == null) {
            headline = label + ", " + tempTxt + " — even and easy";
            advice = "Nothing in the weather is pushing you around today. Keep the routine you know works.";
            glow = 0x333F6349;
        } else if (dosha.equals("vata")) {
            headline = label + ", " + tempTxt + " — a Vata day outside";
            advice = "Warm, oily, grounding food; a slower pace; oil on the skin before you shower.";
            glow = 0x33827DE0;
        } else if (dosha.equals("pitta")) {
            headline = label + ", " + tempTxt + " — Pitta weather";
            advice = "Stay out of the noon sun, favour cooling sweet and bitter tastes, and don't skip water.";
            glow = 0x33D8663D;
        } else {
            headline = label + ", " + tempTxt + " — Kapha in the air";
            advice = "Move first thing, keep meals light and warm, and favour pungent, bitter, astringent tastes.";
            glow = 0x333F6349;
        }

        return new Reading(dosha, glow, headline, advice, q);
    }
}

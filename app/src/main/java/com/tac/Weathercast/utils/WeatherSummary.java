package com.tac.Weathercast.utils;

/**
 * Turns the current conditions into a one-line plain-English summary plus one
 * practical, non-contradictory suggestion — shown in the "At a glance" card.
 * Pure weather logic, no external calls.
 */
public final class WeatherSummary {

    public static final class Glance {
        public final String headline;
        public final String tip;

        Glance(String headline, String tip) {
            this.headline = headline;
            this.tip = tip;
        }
    }

    private WeatherSummary() {}

    private static String feel(double tempC) {
        if (tempC <= 0) return "freezing";
        if (tempC <= 8) return "cold";
        if (tempC <= 15) return "cool";
        if (tempC <= 24) return "mild";
        if (tempC <= 31) return "warm";
        if (tempC <= 37) return "hot";
        return "scorching";
    }

    private static String sky(int owmId, boolean night) {
        if (owmId >= 200 && owmId < 300) return "thunderstorms";
        if (owmId >= 300 && owmId < 400) return "drizzle";
        if (owmId >= 500 && owmId < 600) return "rain";
        if (owmId >= 600 && owmId < 700) return "snow";
        if (owmId >= 700 && owmId < 800) return "haze";
        if (owmId == 800) return night ? "a clear sky" : "clear skies";
        if (owmId == 801 || owmId == 802) return "a few clouds";
        if (owmId > 802) return "an overcast sky";
        return "changeable weather";
    }

    public static Glance of(double tempC, double humidity, double windMs,
                            double uvIndex, int owmId, long daylightMinutes) {
        return of(tempC, humidity, windMs, uvIndex, owmId, daylightMinutes, false);
    }

    /**
     * @param night true when it is currently after sunset / before sunrise — suppresses
     *              any daytime-sun advice so the card never contradicts itself.
     */
    public static Glance of(double tempC, double humidity, double windMs,
                            double uvIndex, int owmId, long daylightMinutes, boolean night) {
        String f = feel(tempC);
        String s = sky(owmId, night);
        boolean wet = owmId >= 200 && owmId < 700;
        boolean snow = owmId >= 600 && owmId < 700;
        boolean storm = owmId >= 200 && owmId < 300;
        boolean windy = windMs >= 8;
        boolean breezy = windMs >= 4 && windMs < 8;
        boolean muggy = humidity >= 78 && tempC >= 22;
        boolean dryAir = humidity <= 28;
        boolean highUv = uvIndex >= 6 && !night;

        StringBuilder h = new StringBuilder();
        h.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
        if (night && owmId == 800) h.append(" and ").append(f);
        else h.append(", ").append(f);
        if (windy) h.append(" and blustery");
        else if (breezy) h.append(" with a steady breeze");
        h.append('.');

        StringBuilder tip = new StringBuilder();
        if (storm) tip.append("Thunder about — head indoors if it closes in.");
        else if (snow) tip.append("Wrap up and mind your footing — it's snowing.");
        else if (wet) tip.append("Take a jacket or umbrella; roads will be slick.");
        else if (highUv && tempC >= 20) tip.append("Strong sun — sunscreen and shade around midday.");
        else if (tempC <= 6) tip.append("Dress in warm layers before heading out.");
        else if (muggy) tip.append("Muggy air — go easy on exertion and keep water handy.");
        else if (windy) tip.append("Secure loose items outdoors and expect wind chill.");
        else if (dryAir) tip.append("Air is dry — moisturise and stay hydrated.");
        else if (night) tip.append("Calm night — comfortable for a walk.");
        else tip.append("Comfortable conditions — a good window to be outside.");

        if (daylightMinutes > 0) {
            long hrs = daylightMinutes / 60, mins = daylightMinutes % 60;
            tip.append(night ? "  Tomorrow brings " : "  ")
               .append(hrs).append("h ").append(mins).append("m of daylight")
               .append(night ? "." : " today.");
        }

        return new Glance(h.toString(), tip.toString());
    }
}

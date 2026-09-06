package com.tac.Weathercast.utils;

/**
 * Turns the current conditions into a one-line plain-English summary plus one
 * practical suggestion — shown in the "At a glance" card on the main screen.
 * Pure weather logic, no external calls.
 */
public final class WeatherSummary {

    public static final class Glance {
        public final String headline;   // e.g. "Hazy and mild — an easy day out."
        public final String tip;        // e.g. "Light layers are enough. Keep water handy in the haze."

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

    private static String sky(int owmId) {
        if (owmId >= 200 && owmId < 300) return "thunderstorms";
        if (owmId >= 300 && owmId < 400) return "drizzle";
        if (owmId >= 500 && owmId < 600) return "rain";
        if (owmId >= 600 && owmId < 700) return "snow";
        if (owmId >= 700 && owmId < 800) return "haze";
        if (owmId == 800) return "clear skies";
        if (owmId == 801 || owmId == 802) return "a few clouds";
        if (owmId > 802) return "an overcast sky";
        return "changeable weather";
    }

    /**
     * @param tempC    air temperature, Celsius
     * @param humidity relative humidity, %
     * @param windMs   wind speed, m/s
     * @param uvIndex  UV index (-1 if unknown)
     * @param owmId    OpenWeatherMap condition id
     * @param daylightMinutes minutes between sunrise and sunset (<=0 if unknown)
     */
    public static Glance of(double tempC, double humidity, double windMs,
                            double uvIndex, int owmId, long daylightMinutes) {
        String f = feel(tempC);
        String s = sky(owmId);
        boolean wet = owmId >= 200 && owmId < 700;
        boolean windy = windMs >= 8;
        boolean breezy = windMs >= 4 && windMs < 8;
        boolean muggy = humidity >= 75 && tempC >= 22;
        boolean dryAir = humidity <= 30;
        boolean highUv = uvIndex >= 6;

        StringBuilder h = new StringBuilder();
        h.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
        h.append(" and ").append(f);
        if (windy) h.append(", and blustery");
        else if (breezy) h.append(", with a steady breeze");
        h.append('.');

        StringBuilder tip = new StringBuilder();
        if (wet) {
            tip.append(owmId >= 600 && owmId < 700
                    ? "Wrap up and mind your footing — it's snowing."
                    : "Take a jacket or umbrella; roads will be slick.");
        } else if (highUv && tempC >= 20) {
            tip.append("Strong sun — sunscreen and shade around midday.");
        } else if (tempC <= 8) {
            tip.append("Dress in warm layers before heading out.");
        } else if (muggy) {
            tip.append("Muggy air — go easy on exertion and keep water handy.");
        } else if (windy) {
            tip.append("Secure loose items outdoors and expect wind chill.");
        } else if (dryAir) {
            tip.append("Air is dry — moisturise and stay hydrated.");
        } else {
            tip.append("Comfortable conditions — a good window to be outside.");
        }

        if (daylightMinutes > 0) {
            long hrs = daylightMinutes / 60, mins = daylightMinutes % 60;
            tip.append("  ").append(hrs).append("h ").append(mins).append("m of daylight today.");
        }

        return new Glance(h.toString(), tip.toString());
    }
}

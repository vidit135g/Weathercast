package com.tac.Weathercast.utils;

import android.graphics.Color;

/**
 * Soma palette. The page stays in the warm cream family (or a midnight dark
 * palette when chosen); the hero is a full elemental gradient block chosen from
 * the sky and the time of day, or locked by the user's Appearance setting.
 */
public final class SomaTheme {

    public static final int ON_ELEMENT = 0xFFF8F3E9;
    public static final int ON_ELEMENT_DIM = 0xC7F8F3E9;

    public final int background, surface, surfaceMuted;
    public final int textPrimary, textSecondary, textMuted;
    public final int border, accent;
    public final int heroFrom, heroTo, heroAccent;
    public final boolean isDark;
    public int[] sky = { 0xFF2E77C9, 0xFF5599DA, 0xFF89BEE8 };   // top → bottom gradient

    private SomaTheme(int bg, int surface, int surfaceMuted, int tp, int ts, int tm,
                      int border, int accent, int heroFrom, int heroTo, int heroAccent,
                      boolean isDark) {
        this.background = bg; this.surface = surface; this.surfaceMuted = surfaceMuted;
        this.textPrimary = tp; this.textSecondary = ts; this.textMuted = tm;
        this.border = border; this.accent = accent;
        this.heroFrom = heroFrom; this.heroTo = heroTo; this.heroAccent = heroAccent;
        this.isDark = isDark;
    }

    // Soma jewel elements: from -> to (light -> deep), accent.
    private static final int[] AIR   = { 0xFF9D98E0, 0xFF5A54A6, 0xFF7B76CE };
    private static final int[] FIRE  = { 0xFFF0A85C, 0xFFC24E2E, 0xFFDC6B3C };
    private static final int[] EARTH = { 0xFF93BE8C, 0xFF436B4E, 0xFF5E8C6A };
    private static final int[] WATER = { 0xFF82CFC0, 0xFF3B8C88, 0xFF4FA79B };
    private static final int[] ETHER = { 0xFFB79FE0, 0xFF463C82, 0xFF8B7BC8 };

    public enum Period { DAWN, MORNING, DAY, AFTERNOON, DUSK, NIGHT }

    public static Period periodFor(int hour) {
        if (hour >= 5 && hour < 8) return Period.DAWN;
        if (hour >= 8 && hour < 11) return Period.MORNING;
        if (hour >= 11 && hour < 16) return Period.DAY;
        if (hour >= 16 && hour < 18) return Period.AFTERNOON;
        if (hour >= 18 && hour < 20) return Period.DUSK;
        return Period.NIGHT;
    }

    /** Hero gradient + accent for the time of day (before any weather nudge). */
    private static int[] periodHero(Period p) {
        switch (p) {
            case DAWN:      return new int[]{ 0xFFF3C7C0, 0xFF8E7FB2, 0xFFC98BA8 };
            case MORNING:   return new int[]{ 0xFFF4DA8E, 0xFF5F9E77, 0xFF6E9E78 };
            case DAY:       return new int[]{ 0xFF7FB6DC, 0xFFE49F5C, 0xFFD8663D };
            case AFTERNOON: return new int[]{ 0xFFF0B36B, 0xFFCE6A44, 0xFFCC6A46 };
            case DUSK:      return new int[]{ 0xFFF4A253, 0xFFB0477E, 0xFFDD6A4A };
            case NIGHT: default: return new int[]{ 0xFF33406E, 0xFF12172E, 0xFFE7B15C };
        }
    }

    private static int[] elementFor(int owmId, boolean isNight) {
        int g = owmId / 100;
        if (g == 2) return ETHER;
        if (g == 3 || g == 5) return WATER;
        if (g == 6) return AIR;
        if (g == 7) return isNight ? ETHER : AIR;
        if (isNight) return ETHER;
        if (owmId == 800 || owmId == 801) return FIRE;
        return EARTH;
    }

    private static int[] lockedElement(String appearance) {
        if (appearance == null) return null;
        switch (appearance) {
            case "fire":  return FIRE;
            case "earth": return EARTH;
            case "air":   return AIR;
            case "water": return WATER;
            case "ether": return ETHER;
            default:      return null;
        }
    }

    /** Apple-style full-screen sky gradient (top → bottom), by weather + hour. */
    public static int[] skyStops(int owmId, int hour, String base) {
        boolean night;
        if ("dark".equals(base)) night = true;
        else if ("light".equals(base)) night = false;
        else night = hour >= 20 || hour < 6;
        Period p = periodFor(hour);
        int g = owmId / 100;

        if (g == 2) return night ? new int[]{ 0xFF1C1F36, 0xFF31344E, 0xFF454864 }
                                 : new int[]{ 0xFF3A3F57, 0xFF565B76, 0xFF70748C };
        if (g == 3 || g == 5) return night ? new int[]{ 0xFF16202B, 0xFF27333F, 0xFF3A4653 }
                                           : new int[]{ 0xFF3B4956, 0xFF54626D, 0xFF6E7B85 };
        if (g == 6) return night ? new int[]{ 0xFF283140, 0xFF3B4655, 0xFF4E5A6A }
                                 : new int[]{ 0xFF6E82A0, 0xFF98AAC0, 0xFFB7C6D6 };
        if (g == 7) return night ? new int[]{ 0xFF2A2D34, 0xFF3E424B, 0xFF52565F }
                                 : new int[]{ 0xFF6F7680, 0xFF8B9199, 0xFFA4A9B0 };
        if (g == 8 && owmId >= 803) return night ? new int[]{ 0xFF1A2230, 0xFF2A3440, 0xFF3B4552 }
                                                 : new int[]{ 0xFF52667C, 0xFF74879C, 0xFF97A8BA };

        if (night) return new int[]{ 0xFF0B1330, 0xFF15224A, 0xFF223059 };
        switch (p) {
            case DAWN:      return new int[]{ 0xFF46578C, 0xFF9C6E92, 0xFFE7A76C };
            case MORNING:   return new int[]{ 0xFF3577C0, 0xFF5F9AD7, 0xFF9CC3E7 };
            case DAY:       return new int[]{ 0xFF2E77C9, 0xFF5599DA, 0xFF89BEE8 };
            case AFTERNOON: return new int[]{ 0xFF357FC3, 0xFF6FA0CE, 0xFFDCB07C };
            case DUSK:      return new int[]{ 0xFF213763, 0xFF7A4A80, 0xFFE28B4F };
            default:        return new int[]{ 0xFF0B1330, 0xFF15224A, 0xFF223059 };
        }
    }

    public static SomaTheme forNow(int hour, int owmId) {
        return forNow(hour, owmId, "auto", "system");
    }

    public static SomaTheme forNow(int hour, int owmId, String accent) {
        return forNow(hour, owmId, accent, "system");
    }

    /**
     * @param accent "auto" | "fire" | "earth" | "air" | "water" | "ether"
     * @param base   "system" (day light / night AMOLED) | "light" | "dark" (AMOLED black)
     */
    public static SomaTheme forNow(int hour, int owmId, String accent, String base) {
        Period p = periodFor(hour);
        boolean isNight = p == Period.NIGHT || p == Period.DAWN;
        if ("dark".equals(accent)) base = "dark";   // back-compat with the old single option
        int[] locked = lockedElement(accent);
        int[] el = locked != null ? locked : elementFor(owmId, isNight);

        boolean dark;
        if ("dark".equals(base)) dark = true;
        else if ("light".equals(base)) dark = false;
        else dark = (hour >= 20 || hour < 6);

        // Apple-style: a full-screen sky gradient with frosted glass cards on top.
        int[] stops = skyStops(owmId, hour, base);
        int[] ph = periodHero(p);
        int heroAcc = locked != null ? el[2] : ph[2];

        SomaTheme t = new SomaTheme(
                stops[1],            // background — mid sky stop
                0x24FFFFFF,          // surface — frosted glass
                0x18FFFFFF,          // surfaceMuted
                0xFFFFFFFF,          // textPrimary
                0xE6FFFFFF,          // textSecondary
                0x9EFFFFFF,          // textMuted
                0x33FFFFFF,          // border — hairline
                0xFFFFFFFF,          // accent — white reads cleanest on the glass
                stops[0], stops[2], heroAcc,
                true);
        t.sky = stops;
        return t;
    }

    public int borderTranslucent() {
        return (border & 0x00FFFFFF) | 0x55000000;
    }

    public static int blend(int a, int b, float t) {
        float inv = 1f - t;
        return Color.argb(255,
                Math.round(Color.red(a) * inv + Color.red(b) * t),
                Math.round(Color.green(a) * inv + Color.green(b) * t),
                Math.round(Color.blue(a) * inv + Color.blue(b) * t));
    }
}

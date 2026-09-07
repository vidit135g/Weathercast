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

        // Hero = time of day, nudged toward the current weather element.
        int[] ph = periodHero(p);
        int heroFrom = locked != null ? el[0] : blend(ph[0], el[0], 0.30f);
        int heroTo   = locked != null ? el[1] : blend(ph[1], el[1], 0.30f);
        int heroAcc  = locked != null ? el[2] : ph[2];

        if (dark) {
            int tintDeep = locked != null ? el[1] : heroTo;
            int tintEdge = locked != null ? el[2] : heroAcc;
            // true-black OLED ground, warmed toward the night / element hue
            int bg = blend(0xFF000000, tintDeep, 0.05f);
            int surface = blend(0xFF141419, tintDeep, 0.10f);
            int surfM = blend(0xFF1E1E25, tintDeep, 0.12f);
            int border = blend(0xFF2B2B36, tintEdge, 0.22f);
            return new SomaTheme(bg, surface, surfM,
                    0xFFF4F5F7, 0xFFC4CBD4, 0xFF8B94A0, border, tintEdge,
                    heroFrom, heroTo, heroAcc, true);
        }

        if (locked != null) {
            // Locked accent + light base: clean near-white + element tint.
            int bg = blend(0xFFFFFFFF, el[0], 0.05f);
            int surfM = blend(0xFFF2F2F5, el[0], 0.08f);
            int border = blend(0xFFE4DED1, el[2], 0.18f);
            return new SomaTheme(bg, 0xFFFFFFFF, surfM,
                    0xFF1F1D1B, 0xFF524C46, 0xFF8F877D, border, el[2],
                    el[0], el[1], el[2], false);
        }

        // Auto + light base: warm paper tuned to the hour.
        int bg, surfMuted;
        switch (p) {
            case DAWN:      bg = 0xFFF4EFF3; surfMuted = 0xFFEAE3EF; break;
            case MORNING:   bg = 0xFFF6F4E8; surfMuted = 0xFFEBE9D5; break;
            case DAY:       bg = 0xFFFBF4E7; surfMuted = 0xFFF3E7CF; break;
            case AFTERNOON: bg = 0xFFF7F1EC; surfMuted = 0xFFEEE2D4; break;
            case DUSK:      bg = 0xFFF9ECDC; surfMuted = 0xFFF1DDC1; break;
            case NIGHT: default: bg = 0xFFF2EFE6; surfMuted = 0xFFE5E2D3; break;
        }
        return new SomaTheme(bg, 0xFFFFFFFF, surfMuted,
                0xFF221E19, 0xFF615A50, 0xFF938979, blend(0xFFE4DED1, heroAcc, 0.12f), heroAcc,
                heroFrom, heroTo, heroAcc, false);
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

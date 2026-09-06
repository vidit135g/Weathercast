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
        if (hour >= 4 && hour < 7) return Period.DAWN;
        if (hour >= 7 && hour < 11) return Period.MORNING;
        if (hour >= 11 && hour < 16) return Period.DAY;
        if (hour >= 16 && hour < 18) return Period.AFTERNOON;
        if (hour >= 18 && hour < 20) return Period.DUSK;
        return Period.NIGHT;
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

        if (dark) {
            int[] de = locked != null ? locked : elementFor(owmId, true);
            // AMOLED-ish black, with a hint of the element so tints read as "black + indigo" etc.
            int bg = blend(0xFF000000, de[1], 0.06f);
            int surface = blend(0xFF14141A, de[1], 0.10f);
            int surfM = blend(0xFF1E1E26, de[1], 0.12f);
            int border = blend(0xFF33333E, de[2], 0.28f);
            return new SomaTheme(bg, surface, surfM,
                    0xFFF6F8FA, 0xFFCDD5DD, 0xFFA7B0BB, border, de[2],
                    de[0], de[1], de[2], true);
        }

        if (locked != null) {
            // Locked accent + light base: clean near-white + element tint.
            int bg = blend(0xFFFFFFFF, el[0], 0.05f);
            int surfM = blend(0xFFF2F2F5, el[0], 0.08f);
            int border = blend(0xFFE3E3EA, el[2], 0.22f);
            return new SomaTheme(bg, 0xFFFFFFFF, surfM,
                    0xFF1B1B22, 0xFF54545E, 0xFF83838E, border, el[2],
                    el[0], el[1], el[2], false);
        }

        int bg, surfMuted, acc;
        switch (p) {
            case DAWN:      bg = 0xFFF3EFF4; surfMuted = 0xFFE9E3F0; acc = 0xFF6E6AB8; break;
            case MORNING:   bg = 0xFFF7F5E9; surfMuted = 0xFFECEAD6; acc = 0xFF3F6349; break;
            case DAY:       bg = 0xFFFDF4E3; surfMuted = 0xFFF6E6CC; acc = 0xFFD8663D; break;
            case AFTERNOON: bg = 0xFFF6F1EE; surfMuted = 0xFFEDE2D6; acc = 0xFFCC6A46; break;
            case DUSK:      bg = 0xFFF8EBDA; surfMuted = 0xFFF1DCC0; acc = 0xFFD8663D; break;
            case NIGHT: default: bg = 0xFFF1EEE5; surfMuted = 0xFFE4E1D2; acc = 0xFFC98A3C; break;
        }
        return new SomaTheme(bg, 0xFFFFFDF6, surfMuted,
                0xFF241E17, 0xFF6B5E4E, 0xFF938573, 0xFFE9DCC4, acc,
                el[0], el[1], el[2], false);
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

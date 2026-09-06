package com.tac.Weathercast.utils;

import android.graphics.Color;

/**
 * Soma palette. The page stays in the warm cream family; the hero is a full
 * elemental gradient block (Soma's {@code components/visuals/elements}) chosen
 * from the sky and the time of day.
 */
public final class SomaTheme {

    /** Near-white text that reads on any element gradient (Soma ON_ELEMENT). */
    public static final int ON_ELEMENT = 0xFFF8F3E9;
    public static final int ON_ELEMENT_DIM = 0xC7F8F3E9;

    // ---- page palette ----
    public final int background;
    public final int surface;
    public final int surfaceMuted;
    public final int textPrimary;
    public final int textSecondary;
    public final int textMuted;
    public final int border;
    public final int accent;

    // ---- hero element gradient ----
    public final int heroFrom;
    public final int heroTo;
    public final int heroAccent;

    private SomaTheme(int bg, int surface, int surfaceMuted, int tp, int ts, int tm,
                      int border, int accent, int heroFrom, int heroTo, int heroAccent) {
        this.background = bg; this.surface = surface; this.surfaceMuted = surfaceMuted;
        this.textPrimary = tp; this.textSecondary = ts; this.textMuted = tm;
        this.border = border; this.accent = accent;
        this.heroFrom = heroFrom; this.heroTo = heroTo; this.heroAccent = heroAccent;
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

    /** Pick the elemental gradient for the hero from the sky + clock. */
    private static int[] elementFor(int owmId, boolean isNight) {
        int g = owmId / 100;
        if (g == 2) return ETHER;                    // thunderstorm
        if (g == 3 || g == 5) return WATER;          // drizzle / rain
        if (g == 6) return AIR;                      // snow
        if (g == 7) return isNight ? ETHER : AIR;    // mist / haze / fog
        if (isNight) return ETHER;                   // clear / clouds at night
        if (owmId == 800 || owmId == 801) return FIRE;   // clear-ish day
        return EARTH;                                // clouds / overcast day
    }

    public static SomaTheme forNow(int hour, int owmId) {
        Period p = periodFor(hour);
        boolean isNight = p == Period.NIGHT || p == Period.DAWN;

        // Warm cream page, shifts subtly by period.
        int bg, surf, surfMuted, tp, ts, tm, border, accent;
        switch (p) {
            case DAWN:      bg = 0xFFF3EFF4; surfMuted = 0xFFE9E3F0; accent = 0xFF6E6AB8; break;
            case MORNING:   bg = 0xFFF7F5E9; surfMuted = 0xFFECEAD6; accent = 0xFF3F6349; break;
            case DAY:       bg = 0xFFFDF4E3; surfMuted = 0xFFF6E6CC; accent = 0xFFD8663D; break;
            case AFTERNOON: bg = 0xFFF6F1EE; surfMuted = 0xFFEDE2D6; accent = 0xFFCC6A46; break;
            case DUSK:      bg = 0xFFF8EBDA; surfMuted = 0xFFF1DCC0; accent = 0xFFD8663D; break;
            case NIGHT: default: bg = 0xFFF1EEE5; surfMuted = 0xFFE4E1D2; accent = 0xFFC98A3C; break;
        }
        surf = 0xFFFFFDF6; tp = 0xFF241E17; ts = 0xFF6B5E4E; tm = 0xFF938573; border = 0xFFE9DCC4;

        int[] el = elementFor(owmId, isNight);
        return new SomaTheme(bg, surf, surfMuted, tp, ts, tm, border, accent,
                el[0], el[1], el[2]);
    }

    public int borderTranslucent() {
        return (border & 0x00FFFFFF) | 0x55000000;
    }

    /** blend a over b */
    public static int blend(int a, int b, float t) {
        float inv = 1f - t;
        return Color.argb(255,
                Math.round(Color.red(a) * inv + Color.red(b) * t),
                Math.round(Color.green(a) * inv + Color.green(b) * t),
                Math.round(Color.blue(a) * inv + Color.blue(b) * t));
    }
}

package com.tac.Weathercast.utils;

import android.graphics.Color;

/**
 * Dynamic palette that tracks the time of day (and nudges for weather),
 * mirroring the Soma design system's six time-of-day themes:
 *
 *   DAWN      04–07  deep warm indigo
 *   MORNING   07–11  warm cream + fresh sage
 *   DAY       11–16  bright warm cream + clear ember
 *   AFTERNOON 16–18  soft warm lilac
 *   DUSK      18–20  amber sunset
 *   NIGHT     20–04  warm midnight teal + saffron
 */
public final class SomaTheme {

    public final int background;
    public final int surface;
    public final int textPrimary;
    public final int textSecondary;
    public final int border;
    public final int accent;
    public final int heroFrom;
    public final int heroTo;
    public final boolean isDark;

    private SomaTheme(int bg, int surface, int tp, int ts, int border, int accent,
                      int heroFrom, int heroTo, boolean isDark) {
        this.background = bg; this.surface = surface; this.textPrimary = tp;
        this.textSecondary = ts; this.border = border; this.accent = accent;
        this.heroFrom = heroFrom; this.heroTo = heroTo; this.isDark = isDark;
    }

    public enum Period { DAWN, MORNING, DAY, AFTERNOON, DUSK, NIGHT }

    public static Period periodFor(int hour) {
        if (hour >= 4 && hour < 7) return Period.DAWN;
        if (hour >= 7 && hour < 11) return Period.MORNING;
        if (hour >= 11 && hour < 16) return Period.DAY;
        if (hour >= 16 && hour < 18) return Period.AFTERNOON;
        if (hour >= 18 && hour < 20) return Period.DUSK;
        return Period.NIGHT;
    }

    /**
     * All periods stay in the warm cream family — Soma's light direction — and
     * only shift accent, warmth and the hero wash so the app quietly tracks the
     * day without ever going dark-and-muddy.
     */
    private static SomaTheme base(Period p) {
        switch (p) {
            case DAWN: return new SomaTheme(
                    0xFFF3EFF4, 0xFFFCFAFC, 0xFF262233, 0xFF6C6577, 0xFFE6DEE8, 0xFF6E6AB8,
                    0xFFFBF6F0, 0xFFEDE4EE, false);
            case MORNING: return new SomaTheme(
                    0xFFF7F4E7, 0xFFFFFEF8, 0xFF20301F, 0xFF5E6B58, 0xFFE4E2CC, 0xFF3F6349,
                    0xFFFEFDF4, 0xFFEEEFD9, false);
            case DAY: return new SomaTheme(
                    0xFFFDF4E4, 0xFFFFFCF4, 0xFF2A2113, 0xFF7A6650, 0xFFEFE0C6, 0xFFD8663D,
                    0xFFFFFDF6, 0xFFFBE7CD, false);
            case AFTERNOON: return new SomaTheme(
                    0xFFF6F2F0, 0xFFFFFCFA, 0xFF2C231D, 0xFF7B6A5C, 0xFFEBE0D6, 0xFFCC6A46,
                    0xFFFFFBF6, 0xFFF3E3D6, false);
            case DUSK: return new SomaTheme(
                    0xFFF7EDDF, 0xFFFFF6EC, 0xFF3A2417, 0xFF7A5237, 0xFFEBD6BE, 0xFFD8663D,
                    0xFFFDEEDB, 0xFFF6D3AC, false);
            case NIGHT: default: return new SomaTheme(
                    0xFFF1EEE6, 0xFFFAF8F2, 0xFF232620, 0xFF6A6C63, 0xFFDEDCCE, 0xFFC98A3C,
                    0xFFF7F4EC, 0xFFECE7DA, false);
        }
    }

    /** Time-of-day palette, dimmed toward overcast when the sky is heavy. */
    public static SomaTheme forNow(int hour, int owmId) {
        SomaTheme t = base(periodFor(hour));
        int group = owmId / 100;
        boolean heavy = group == 2 || group == 3 || group == 5 || group == 6 || group == 7
                || owmId == 803 || owmId == 804;
        if (!heavy) return t;
        // pull background + hero toward a muted grey-blue, keep text/accent
        int mute = t.isDark ? 0xFF141E24 : 0xFFE6E4DE;
        return new SomaTheme(
                blend(t.background, mute, 0.55f),
                blend(t.surface, mute, 0.30f),
                t.textPrimary, t.textSecondary,
                blend(t.border, mute, 0.4f),
                t.accent,
                blend(t.heroFrom, mute, 0.5f),
                blend(t.heroTo, mute, 0.5f),
                t.isDark);
    }

    public int borderTranslucent() {
        return (border & 0x00FFFFFF) | 0x40000000;
    }

    private static int blend(int a, int b, float t) {
        float inv = 1f - t;
        return Color.argb(255,
                Math.round(Color.red(a) * inv + Color.red(b) * t),
                Math.round(Color.green(a) * inv + Color.green(b) * t),
                Math.round(Color.blue(a) * inv + Color.blue(b) * t));
    }
}

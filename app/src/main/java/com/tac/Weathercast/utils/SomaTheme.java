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

    private static SomaTheme base(Period p) {
        switch (p) {
            case DAWN: return new SomaTheme(
                    0xFF15172A, 0xFF20233F, 0xFFF3F2FB, 0xFFCFCDEC, 0xFF3B3F6B, 0xFFB0A6F5,
                    0xFF2A2E52, 0xFF15172A, true);
            case MORNING: return new SomaTheme(
                    0xFFF6F3E6, 0xFFFFFFFA, 0xFF1F2E24, 0xFF4B5A4C, 0xFFDFDFC5, 0xFF2A8259,
                    0xFFFDFCF3, 0xFFEDF0DC, false);
            case DAY: return new SomaTheme(
                    0xFFFFF6E8, 0xFFFFFCF3, 0xFF33220F, 0xFF6A5138, 0xFFF1E1C4, 0xFFE85E2C,
                    0xFFFFFDF6, 0xFFFBE9CE, false);
            case AFTERNOON: return new SomaTheme(
                    0xFFF5F1FA, 0xFFFBF9FE, 0xFF241F34, 0xFF544C74, 0xFFE4DCF1, 0xFF6E6AB8,
                    0xFFFDFBFF, 0xFFEDE6F6, false);
            case DUSK: return new SomaTheme(
                    0xFFF7ECDD, 0xFFFFF7EE, 0xFF3A2417, 0xFF6E4A33, 0xFFEAD6BE, 0xFFD8663D,
                    0xFFFDEFDD, 0xFFF3CFA6, false);
            case NIGHT: default: return new SomaTheme(
                    0xFF111F1E, 0xFF1B2C2A, 0xFFF1F5F2, 0xFFB9C9C4, 0xFF2F423E, 0xFFE0A43A,
                    0xFF1A2E2B, 0xFF111F1E, true);
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

package com.tac.Weathercast.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;

/** Shared motion — a soft, springy "rise and settle" used for entrances across the app. */
public final class Anim {

    private Anim() {}

    /** easeOutExpo-ish — quick out of the gate, long gentle settle. */
    public static PathInterpolator ease() {
        return new PathInterpolator(0.16f, 1f, 0.3f, 1f);
    }

    /** Fade + rise + a hair of scale, staggered across a container's children. */
    public static void enterChildren(ViewGroup group, long startDelay, long stagger) {
        if (group == null) return;
        float d = group.getResources().getDisplayMetrics().density;
        int shown = 0;
        for (int i = 0; i < group.getChildCount(); i++) {
            View c = group.getChildAt(i);
            if (c.getVisibility() != View.VISIBLE || c.getHeight() == 0 && c.getWidth() == 0) {
                // still animate — layout may not be measured yet
            }
            c.setAlpha(0f);
            c.setTranslationY(28f * d);
            c.setScaleX(0.97f);
            c.setScaleY(0.97f);
            c.animate()
                    .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
                    .setStartDelay(startDelay + shown * stagger)
                    .setDuration(560)
                    .setInterpolator(ease())
                    .start();
            shown++;
        }
    }

    public static void enterChildren(ViewGroup group) { enterChildren(group, 40, 52); }

    /** A single view rising into place. */
    public static void enter(View v, long delay) {
        if (v == null) return;
        float d = v.getResources().getDisplayMetrics().density;
        v.setAlpha(0f);
        v.setTranslationY(24f * d);
        v.animate().alpha(1f).translationY(0f)
                .setStartDelay(delay).setDuration(520).setInterpolator(ease()).start();
    }

    /** Gentle scale pulse — good for a tapped icon or a value that just changed. */
    public static void pulse(View v) {
        if (v == null) return;
        v.animate().scaleX(1.14f).scaleY(1.14f).setDuration(130)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f)
                        .setDuration(240).setInterpolator(ease()).start())
                .start();
    }
}

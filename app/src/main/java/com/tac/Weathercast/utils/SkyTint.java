package com.tac.Weathercast.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Recolours a subtree's text for the Apple-style frosted look — everything
 * white, with smaller/label text stepped down in opacity — and adds a faint
 * shadow so copy stays legible on any sky gradient.
 */
public final class SkyTint {

    // High-contrast whites — the sky behind can be a bright daytime blue, so even
    // "muted" copy has to clear WCAG AA. The paired soft shadow does the rest.
    public static final int PRIMARY = 0xFFFFFFFF;
    public static final int SECONDARY = 0xF7FFFFFF;
    public static final int MUTED = 0xD6FFFFFF;

    private SkyTint() {}

    public static void apply(View root) {
        if (root == null) return;
        if (root instanceof TextView) {
            TextView tv = (TextView) root;
            float sp = tv.getTextSize() / tv.getResources().getDisplayMetrics().scaledDensity;
            int c = sp <= 11.5f ? MUTED : (sp <= 13.5f ? SECONDARY : PRIMARY);
            tv.setTextColor(c);
            tv.setHintTextColor(MUTED);
            float d = tv.getResources().getDisplayMetrics().density;
            tv.setShadowLayer(d * 7f, 0f, d * 1.5f, 0x73000000);
        }
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) apply(g.getChildAt(i));
        }
    }
}

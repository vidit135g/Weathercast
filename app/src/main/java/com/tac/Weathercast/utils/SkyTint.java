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

    public static final int PRIMARY = 0xFFFFFFFF;
    public static final int SECONDARY = 0xE6FFFFFF;
    public static final int MUTED = 0x9EFFFFFF;

    private SkyTint() {}

    public static void apply(View root) {
        if (root == null) return;
        if (root instanceof TextView) {
            TextView tv = (TextView) root;
            float sp = tv.getTextSize() / tv.getResources().getDisplayMetrics().scaledDensity;
            int c = sp <= 11.5f ? MUTED : (sp <= 13.5f ? SECONDARY : PRIMARY);
            tv.setTextColor(c);
            tv.setHintTextColor(MUTED);
            tv.setShadowLayer(tv.getResources().getDisplayMetrics().density * 6f, 0f,
                    tv.getResources().getDisplayMetrics().density * 1f, 0x30000000);
        }
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) apply(g.getChildAt(i));
        }
    }
}

package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * The full-screen sky — a clean vertical gradient chosen from the weather and
 * the time of day (Apple-Weather style), with soft top / bottom scrims for
 * legibility and one barely-there drifting light bloom for atmosphere.
 */
public class ElementalFieldView extends View {

    private int[] stops = { 0xFF2E77C9, 0xFF5599DA, 0xFF89BEE8 };
    private int[] prevStops = stops;
    private float fade = 1f;
    private ValueAnimator fadeAnim;
    private float phase = 0f;
    private ValueAnimator anim;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ElementalFieldView(Context c) { super(c); init(); }
    public ElementalFieldView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        anim = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2));
        anim.setDuration(46000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setInterpolator(null);
        anim.addUpdateListener(a -> { phase = (float) a.getAnimatedValue(); invalidate(); });
    }

    /** @param sky three colours, top → bottom — cross-faded from the current sky. */
    public void setSky(int[] sky) {
        if (sky == null || sky.length != 3) return;
        if (java.util.Arrays.equals(sky, stops)) return;
        prevStops = current();
        stops = sky;
        if (fadeAnim != null) fadeAnim.cancel();
        fade = 0f;
        fadeAnim = ValueAnimator.ofFloat(0f, 1f);
        fadeAnim.setDuration(900);
        fadeAnim.addUpdateListener(a -> { fade = (float) a.getAnimatedValue(); invalidate(); });
        fadeAnim.start();
    }

    private int[] current() {
        if (fade >= 1f) return stops;
        return new int[]{ lerp(prevStops[0], stops[0], fade),
                          lerp(prevStops[1], stops[1], fade),
                          lerp(prevStops[2], stops[2], fade) };
    }

    private static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000 | (Math.round(ar + (br - ar) * t) << 16)
                | (Math.round(ag + (bg - ag) * t) << 8) | Math.round(ab + (bb - ab) * t);
    }

    /** Back-compat shim — older callers passed element colours; ignore, sky wins. */
    public void setColors(int background, int elementFrom, int elementTo, boolean isDark) { }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (anim != null && !anim.isStarted()) anim.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (anim != null) anim.cancel();
        super.onDetachedFromWindow();
    }

    private boolean lightSky() {
        int mid = current()[1];
        return (0.299 * ((mid >> 16) & 0xFF) + 0.587 * ((mid >> 8) & 0xFF) + 0.114 * (mid & 0xFF)) > 150;
    }

    @Override
    protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;
        int[] s = current();

        paint.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{ s[0], s[1], s[2] },
                new float[]{ 0f, 0.55f, 1f }, Shader.TileMode.CLAMP));
        cv.drawRect(0, 0, w, h, paint);

        boolean light = lightSky();

        // atmospheric bloom
        float bx = w * (0.7f + 0.16f * (float) Math.sin(phase));
        float by = h * (0.16f + 0.06f * (float) Math.cos(phase * 0.8f));
        float br = Math.max(w, h) * 0.75f;
        int bloom = light ? 0x1EFFFFFF : 0x22FFFFFF;
        paint.setShader(new RadialGradient(bx, by, br,
                new int[]{ bloom, bloom & 0x00FFFFFF }, new float[]{ 0f, 1f }, Shader.TileMode.CLAMP));
        cv.drawRect(0, 0, w, h, paint);

        // full-height legibility scrim — white copy sits on this at every scroll
        // position; darker toward the bottom where the densest content lives, and
        // stronger still on a bright daytime sky.
        int topA    = light ? 0x1F : 0x0F;
        int midA    = light ? 0x40 : 0x28;
        int bottomA = light ? 0x66 : 0x40;
        paint.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{ topA << 24, midA << 24, bottomA << 24 },
                new float[]{ 0f, 0.5f, 1f }, Shader.TileMode.CLAMP));
        cv.drawRect(0, 0, w, h, paint);

        // top scrim — keeps the big temperature readable under the status bar
        paint.setShader(new LinearGradient(0, 0, 0, h * 0.26f,
                new int[]{ light ? 0x4D000000 : 0x38000000, 0x00000000 }, null, Shader.TileMode.CLAMP));
        cv.drawRect(0, 0, w, h * 0.26f, paint);

        // bottom scrim — under the tab bar
        paint.setShader(new LinearGradient(0, h * 0.74f, 0, h,
                new int[]{ 0x00000000, light ? 0x5C000000 : 0x45000000 }, null, Shader.TileMode.CLAMP));
        cv.drawRect(0, h * 0.74f, w, h, paint);
        paint.setShader(null);
    }
}

package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Soma's ambient elemental field — a slow-drifting multi-blob gradient painted
 * behind the whole app. Recolours with the current theme / element.
 */
public class ElementalFieldView extends View {

    private final Paint base = new Paint();
    private final Paint blob = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int bg = 0xFFFBF5EA;
    private int c1 = 0x33D8A24E;
    private int c2 = 0x33827DE0;
    private int c3 = 0x223F6349;
    private boolean dark = false;
    private float phase = 0f;
    private ValueAnimator anim;

    public ElementalFieldView(Context c) { super(c); init(); }
    public ElementalFieldView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        anim = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2));
        anim.setDuration(38000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setInterpolator(null);
        anim.addUpdateListener(a -> { phase = (float) a.getAnimatedValue(); invalidate(); });
    }

    private int deep;

    /** @param element two element stops (light→deep); a stronger wash is derived. */
    public void setColors(int background, int elementFrom, int elementTo, boolean isDark) {
        this.dark = isDark;
        // a restrained ambient tint — present, but never a colour bath
        this.bg = SomaTheme.blend(background, elementFrom, isDark ? 0.10f : 0.055f);
        this.deep = SomaTheme.blend(background, elementTo, isDark ? 0.15f : 0.09f);
        int alphaA = isDark ? 0x3E : 0x2E;
        int alphaB = isDark ? 0x34 : 0x24;
        int alphaC = isDark ? 0x28 : 0x1A;
        this.c1 = (elementFrom & 0x00FFFFFF) | (alphaA << 24);
        this.c2 = (elementTo & 0x00FFFFFF) | (alphaB << 24);
        this.c3 = (SomaTheme.blend(elementFrom, elementTo, 0.5f) & 0x00FFFFFF) | (alphaC << 24);
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (anim != null && !anim.isStarted()) anim.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (anim != null) anim.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;
        base.setShader(new android.graphics.LinearGradient(0, 0, 0, h,
                new int[]{ bg, deep }, null, Shader.TileMode.CLAMP));
        cv.drawRect(0, 0, w, h, base);

        float r = Math.max(w, h) * 1.05f;
        drawBlob(cv, w * (0.20f + 0.14f * (float) Math.sin(phase)),
                h * (0.14f + 0.10f * (float) Math.cos(phase * 0.8f)), r, c1);
        drawBlob(cv, w * (0.86f + 0.10f * (float) Math.sin(phase * 0.7f + 1.5f)),
                h * (0.78f + 0.12f * (float) Math.cos(phase * 0.6f)), r * 1.05f, c2);
        drawBlob(cv, w * (0.55f + 0.18f * (float) Math.sin(phase * 0.5f + 3f)),
                h * (0.5f + 0.16f * (float) Math.cos(phase * 0.9f + 2f)), r * 0.8f, c3);
    }

    private void drawBlob(Canvas cv, float cx, float cy, float radius, int color) {
        blob.setShader(new RadialGradient(cx, cy, radius,
                new int[]{ color, color & 0x00FFFFFF }, new float[]{ 0f, 1f }, Shader.TileMode.CLAMP));
        cv.drawCircle(cx, cy, radius, blob);
    }
}

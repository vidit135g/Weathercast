package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * A Soma-style sun arc: a soft parabola from sunrise to sunset with a glowing
 * marker at the current position of the sun, and the passed portion drawn solid.
 */
public class SunArcView extends View {

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progress = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arc = new Path();

    private float fraction = 0f;          // 0..1 position between sunrise & sunset
    private float anim = 0f;              // animated 0..fraction
    private int accent = 0xFFD8663D;

    public SunArcView(Context c) { super(c); init(); }
    public SunArcView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeCap(Paint.Cap.ROUND);
        track.setStrokeWidth(3f * d);
        track.setColor(0x33000000);
        track.setPathEffect(new android.graphics.DashPathEffect(new float[]{ 2f * d, 5f * d }, 0));

        progress.setStyle(Paint.Style.STROKE);
        progress.setStrokeCap(Paint.Cap.ROUND);
        progress.setStrokeWidth(3.5f * d);

        dot.setColor(Color.WHITE);
        glow.setColor(accent);
    }

    /** @param fraction 0 before sunrise → 1 after sunset. */
    public void setDaylightFraction(float fraction, int accent) {
        this.fraction = Math.max(0f, Math.min(1f, fraction));
        this.accent = accent;
        glow.setColor(accent);
        ValueAnimator va = ValueAnimator.ofFloat(0f, this.fraction);
        va.setDuration(900);
        va.setInterpolator(new DecelerateInterpolator(1.6f));
        va.addUpdateListener(a -> { anim = (float) a.getAnimatedValue(); invalidate(); });
        va.start();
    }

    @Override
    protected void onDraw(Canvas cv) {
        float d = getResources().getDisplayMetrics().density;
        float padX = 10f * d, top = 8f * d, bottom = getHeight() - 6f * d;
        float w = getWidth() - padX * 2f;

        arc.reset();
        arc.moveTo(padX, bottom);
        arc.quadTo(padX + w / 2f, top - (bottom - top) * 0.9f, padX + w, bottom);

        progress.setShader(new LinearGradient(padX, 0, padX + w, 0,
                new int[]{ 0xFFF0A85C, accent }, null, Shader.TileMode.CLAMP));

        cv.drawPath(arc, track);

        // draw the passed portion by clipping to x < marker
        float t = anim;
        float mx = bezierX(padX, padX + w / 2f, padX + w, t);
        float my = bezierY(bottom, top - (bottom - top) * 0.9f, bottom, t);
        cv.save();
        cv.clipRect(0, 0, mx, getHeight());
        cv.drawPath(arc, progress);
        cv.restore();

        if (fraction > 0f && fraction < 1f) {
            glow.setAlpha(60);
            cv.drawCircle(mx, my, 9f * d, glow);
            glow.setAlpha(255);
            cv.drawCircle(mx, my, 5.5f * d, glow);
            cv.drawCircle(mx, my, 2.5f * d, dot);
        }
    }

    private static float bezierX(float p0, float p1, float p2, float t) {
        float u = 1 - t;
        return u * u * p0 + 2 * u * t * p1 + t * t * p2;
    }
    private static float bezierY(float p0, float p1, float p2, float t) {
        float u = 1 - t;
        return u * u * p0 + 2 * u * t * p1 + t * t * p2;
    }
}

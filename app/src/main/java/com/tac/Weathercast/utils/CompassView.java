package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** A small Soma-style wind compass: cardinal ticks + an accent needle at the bearing. */
public class CompassView extends View {

    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hub = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float bearing = Float.NaN;   // degrees the wind blows FROM
    private float anim = 0f;
    private int accent = 0xFFD8663D;

    public CompassView(Context c) { super(c); init(); }
    public CompassView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(1.5f * d);
        ring.setColor(0x33000000);
        tick.setStrokeWidth(2f * d);
        tick.setStrokeCap(Paint.Cap.ROUND);
        tick.setColor(0x55000000);
        needle.setStyle(Paint.Style.FILL);
        hub.setColor(Color.WHITE);
    }

    public void setBearing(Float degrees, int accent) {
        this.accent = accent;
        needle.setColor(accent);
        if (degrees == null) { bearing = Float.NaN; invalidate(); return; }
        this.bearing = degrees;
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(700);
        va.setInterpolator(new DecelerateInterpolator(1.8f));
        va.addUpdateListener(a -> { anim = (float) a.getAnimatedValue(); invalidate(); });
        va.start();
    }

    @Override
    protected void onDraw(Canvas cv) {
        float d = getResources().getDisplayMetrics().density;
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float r = Math.min(cx, cy) - 2f * d;

        cv.drawCircle(cx, cy, r, ring);
        for (int i = 0; i < 4; i++) {
            double a = Math.toRadians(i * 90);
            float sx = cx + (float) Math.sin(a) * (r - 4f * d);
            float sy = cy - (float) Math.cos(a) * (r - 4f * d);
            float ex = cx + (float) Math.sin(a) * r;
            float ey = cy - (float) Math.cos(a) * r;
            cv.drawLine(sx, sy, ex, ey, tick);
        }

        if (Float.isNaN(bearing)) return;

        cv.save();
        cv.rotate(bearing * anim, cx, cy);
        float tip = r - 3f * d;
        android.graphics.Path p = new android.graphics.Path();
        p.moveTo(cx, cy - tip);
        p.lineTo(cx - 4f * d, cy);
        p.lineTo(cx + 4f * d, cy);
        p.close();
        cv.drawPath(p, needle);
        needle.setAlpha(90);
        android.graphics.Path t = new android.graphics.Path();
        t.moveTo(cx, cy + tip * 0.7f);
        t.lineTo(cx - 3f * d, cy);
        t.lineTo(cx + 3f * d, cy);
        t.close();
        cv.drawPath(t, needle);
        needle.setAlpha(255);
        cv.restore();
        cv.drawCircle(cx, cy, 3f * d, hub);
    }
}

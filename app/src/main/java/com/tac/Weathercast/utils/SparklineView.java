package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** A tiny smoothed trend line that draws itself on. */
public class SparklineView extends View {

    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float[] data = null;
    private float progress = 0f;
    private int accent = 0xFFD8663D;

    public SparklineView(Context c) { super(c); init(); }
    public SparklineView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2f * d);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setData(float[] values, int accent) {
        this.data = values;
        this.accent = accent;
        line.setColor(accent);
        dot.setColor(accent);
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(700);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(a -> { progress = (float) a.getAnimatedValue(); invalidate(); });
        va.start();
    }

    @Override
    protected void onDraw(Canvas cv) {
        if (data == null || data.length < 2) return;
        float d = getResources().getDisplayMetrics().density;
        float pad = 3f * d;
        float w = getWidth() - pad * 2f, h = getHeight() - pad * 2f;

        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float v : data) { min = Math.min(min, v); max = Math.max(max, v); }
        float range = max - min;
        if (range < 0.0001f) range = 1f;

        int n = data.length;
        int shown = Math.max(2, Math.round(n * progress));
        Path p = new Path();
        float lastX = 0, lastY = 0;
        for (int i = 0; i < shown; i++) {
            float x = pad + w * i / (n - 1f);
            float y = pad + h * (1f - (data[i] - min) / range);
            if (i == 0) p.moveTo(x, y);
            else {
                float px = pad + w * (i - 1) / (n - 1f);
                float mid = (px + x) / 2f;
                p.cubicTo(mid, lastY, mid, y, x, y);
            }
            lastX = x; lastY = y;
        }
        cv.drawPath(p, line);
        cv.drawCircle(lastX, lastY, 3f * d, dot);
    }
}

package com.tac.Weathercast.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.Date;

/** A premium forecast chart: gradient area or bars, gridlines with value labels,
 *  day dividers, and min / max markers. Theme-aware via setColors(). */
public class TrendChartView extends View {

    private float[] v = new float[0];
    private long[] t = new long[0];
    private int accent = 0xFF5A54A6;
    private int gridColor = 0x22000000, labelColor = 0x99000000;
    private boolean bars;
    private String unit = "";

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private float reveal = 0f;

    public TrendChartView(Context c) { super(c); }
    public TrendChartView(Context c, AttributeSet a) { super(c, a); }

    public void setColors(int accent, int grid, int label) {
        this.accent = accent; this.gridColor = grid; this.labelColor = label; invalidate();
    }

    public void setSeries(TrendSeries s) {
        this.v = s.values; this.t = s.times; this.bars = s.bars; this.unit = s.unit;
        reveal = 0f;
        android.animation.ValueAnimator va = android.animation.ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(1100);
        va.setStartDelay(120);
        va.setInterpolator(Anim.ease());
        va.addUpdateListener(a -> { reveal = (float) a.getAnimatedValue(); invalidate(); });
        va.start();
    }

    @Override
    protected void onDraw(Canvas cv) {
        int n = v.length;
        if (n < 2) return;
        float w = getWidth(), h = getHeight();
        float padL = dp(34), padR = dp(12), padT = dp(14), padB = dp(24);
        float cw = w - padL - padR, ch = h - padT - padB;

        float mn = Float.MAX_VALUE, mx = -Float.MAX_VALUE;
        for (float x : v) { mn = Math.min(mn, x); mx = Math.max(mx, x); }
        if (bars) mn = 0;
        if (mx - mn < 0.001f) mx = mn + 1;
        float pad = (mx - mn) * 0.12f; mx += pad; if (!bars) mn -= pad;

        // gridlines + labels
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(dp(10));
        for (int i = 0; i <= 3; i++) {
            float gy = padT + ch * i / 3f;
            float val = mx - (mx - mn) * i / 3f;
            p.setColor(gridColor);
            p.setStrokeWidth(dp(1));
            cv.drawLine(padL, gy, w - padR, gy, p);
            p.setColor(labelColor);
            cv.drawText(Math.round(val) + "", dp(4), gy + dp(3.5f), p);
        }

        // day dividers + labels
        int prevDay = -1;
        for (int i = 0; i < n; i++) {
            int day = CityTime.calendar(new Date(t[i])).get(java.util.Calendar.DAY_OF_YEAR);
            if (day != prevDay && i > 0) {
                float x = padL + cw * i / (n - 1);
                p.setColor(gridColor);
                cv.drawLine(x, padT, x, padT + ch, p);
                p.setColor(labelColor);
                p.setTextSize(dp(9));
                cv.drawText(CityTime.format("EEE").format(new Date(t[i])).toUpperCase(), x + dp(3), h - dp(8), p);
            }
            prevDay = day;
        }

        int clip = cv.save();
        cv.clipRect(0f, 0f, padL + cw * Math.max(0.001f, reveal) + dp(2), h);

        if (bars) {
            float bw = Math.max(dp(2), cw / n * 0.55f);
            p.setColor(withAlpha(accent, 0xD0));
            for (int i = 0; i < n; i++) {
                float x = padL + cw * i / (n - 1);
                float y = padT + ch * (1 - (v[i] - mn) / (mx - mn));
                if (v[i] <= 0.001f) continue;
                cv.drawRoundRect(x - bw / 2, y, x + bw / 2, padT + ch, dp(2), dp(2), p);
            }
            cv.restoreToCount(clip);
            return;
        }

        // area fill
        path.reset();
        for (int i = 0; i < n; i++) {
            float x = padL + cw * i / (n - 1);
            float y = padT + ch * (1 - (v[i] - mn) / (mx - mn));
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        Path fill = new Path(path);
        fill.lineTo(w - padR, padT + ch);
        fill.lineTo(padL, padT + ch);
        fill.close();
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, padT, 0, padT + ch,
                withAlpha(accent, 0x40), withAlpha(accent, 0x08), Shader.TileMode.CLAMP));
        cv.drawPath(fill, p);
        p.setShader(null);

        // line
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(dp(2.5f));
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(accent);
        cv.drawPath(path, p);

        cv.restoreToCount(clip);

        // min / max dots (drawn after the reveal completes so they don't pop early)
        if (reveal > 0.98f) {
            drawDot(cv, padL, cw, ch, padT, n, mn, mx, indexOf(v, true), true);
            drawDot(cv, padL, cw, ch, padT, n, mn, mx, indexOf(v, false), false);
        }
    }

    private void drawDot(Canvas cv, float padL, float cw, float ch, float padT,
                         int n, float mn, float mx, int idx, boolean isMax) {
        if (idx < 0) return;
        float x = padL + cw * idx / (n - 1);
        float y = padT + ch * (1 - (v[idx] - mn) / (mx - mn));
        p.setStyle(Paint.Style.FILL);
        p.setColor(accent);
        cv.drawCircle(x, y, dp(3.5f), p);
        p.setColor(labelColor);
        p.setTextSize(dp(10));
        String s = Math.round(v[idx]) + unit;
        float tw = p.measureText(s);
        float tx = Math.min(getWidth() - dp(12) - tw, Math.max(padL, x - tw / 2));
        cv.drawText(s, tx, isMax ? y - dp(8) : y + dp(16), p);
    }

    private static int indexOf(float[] a, boolean max) {
        int idx = -1; float best = max ? -Float.MAX_VALUE : Float.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (max ? a[i] > best : a[i] < best) { best = a[i]; idx = i; }
        }
        return idx;
    }

    private static int withAlpha(int color, int a) {
        return (a << 24) | (color & 0x00FFFFFF);
    }
    private float dp(float x) { return x * getResources().getDisplayMetrics().density; }
}

package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** A wide smoothed temperature curve with hour labels, a gradient fill and a "now" marker. */
public class TempCurveView extends View {

    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float[] temps = null;
    private String[] hours = null;
    private int accent = 0xFFD8663D;
    private int textColor = 0x99000000;
    private float progress = 0f;

    public TempCurveView(Context c) { super(c); init(); }
    public TempCurveView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(3f * d);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        label.setTextSize(10f * d);
        label.setTextAlign(Paint.Align.CENTER);
        grid.setStrokeWidth(1f);
    }

    public void setData(float[] temps, String[] hours, int accent, int textColor) {
        this.temps = temps;
        this.hours = hours;
        this.accent = accent;
        this.textColor = textColor;
        line.setColor(accent);
        dot.setColor(accent);
        label.setColor(textColor);
        grid.setColor((textColor & 0x00FFFFFF) | 0x14000000);
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(850);
        va.setInterpolator(new DecelerateInterpolator(1.5f));
        va.addUpdateListener(a -> { progress = (float) a.getAnimatedValue(); invalidate(); });
        va.start();
    }

    @Override
    protected void onDraw(Canvas cv) {
        if (temps == null || temps.length < 2) return;
        float d = getResources().getDisplayMetrics().density;
        float padL = 8f * d, padR = 8f * d, padT = 14f * d, padB = 22f * d;
        float w = getWidth() - padL - padR;
        float h = getHeight() - padT - padB;

        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float t : temps) { min = Math.min(min, t); max = Math.max(max, t); }
        float range = Math.max(1f, max - min);

        int n = temps.length;
        int shown = Math.max(2, Math.round(n * progress));

        Path path = new Path();
        float lastX = 0, lastY = 0;
        float[] xs = new float[n], ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = padL + w * i / (n - 1f);
            ys[i] = padT + h * (1f - (temps[i] - min) / range);
        }
        for (int i = 0; i < shown; i++) {
            if (i == 0) path.moveTo(xs[0], ys[0]);
            else {
                float mx = (xs[i - 1] + xs[i]) / 2f;
                path.cubicTo(mx, ys[i - 1], mx, ys[i], xs[i], ys[i]);
            }
            lastX = xs[i]; lastY = ys[i];
        }

        // gradient fill under the curve
        Path fillPath = new Path(path);
        fillPath.lineTo(lastX, padT + h);
        fillPath.lineTo(xs[0], padT + h);
        fillPath.close();
        fill.setShader(new LinearGradient(0, padT, 0, padT + h,
                new int[]{ (accent & 0x00FFFFFF) | 0x40000000, (accent & 0x00FFFFFF) }, null, Shader.TileMode.CLAMP));
        fill.setAlpha(40);
        cv.drawPath(fillPath, fill);

        cv.drawPath(path, line);

        // labels + points every 3rd
        for (int i = 0; i < n; i += 2) {
            if (i >= shown) break;
            cv.drawCircle(xs[i], ys[i], 2.5f * d, dot);
            String tmp = Math.round(temps[i]) + "°";
            cv.drawText(tmp, xs[i], ys[i] - 8f * d, label);
            if (hours != null && i < hours.length)
                cv.drawText(hours[i], xs[i], getHeight() - 6f * d, label);
        }
    }
}

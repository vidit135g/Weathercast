package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * A lightweight weather ambience layer drawn over the elemental field —
 * falling rain / drifting snow / sun rays / drifting clouds, à la Apple Weather.
 */
public class WeatherFxView extends View {

    public enum Fx { NONE, RAIN, DRIZZLE, SNOW, CLOUDS, SUN, FOG, THUNDER }

    private Fx fx = Fx.NONE;
    private boolean night = false;
    private int accent = 0xFF7B76CE;
    private final Random rnd = new Random();
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator anim;
    private float t = 0f;

    private float[] x, y, v, s;   // particle pools
    private int n = 0;
    private long flashUntil = 0;

    public WeatherFxView(Context c) { super(c); init(); }
    public WeatherFxView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(1000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setInterpolator(null);
        anim.addUpdateListener(a -> { t += 0.016f; step(); invalidate(); });
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void setWeather(int owmId, boolean isNight, int accent) {
        this.night = isNight;
        this.accent = accent;
        Fx next;
        int g = owmId / 100;
        if (g == 2) next = Fx.THUNDER;
        else if (g == 3) next = Fx.DRIZZLE;
        else if (g == 5) next = Fx.RAIN;
        else if (g == 6) next = Fx.SNOW;
        else if (g == 7) next = Fx.FOG;
        else if (owmId == 800) next = isNight ? Fx.NONE : Fx.SUN;
        else if (g == 8) next = Fx.CLOUDS;
        else next = Fx.NONE;
        if (next != fx) { fx = next; seed(); }
        if (fx == Fx.NONE) { if (anim.isStarted()) anim.cancel(); invalidate(); }
        else if (!anim.isStarted()) anim.start();
    }

    private void seed() {
        int w = Math.max(1, getWidth()), h = Math.max(1, getHeight());
        switch (fx) {
            case RAIN: case THUNDER: n = 130; break;
            case DRIZZLE: n = 70; break;
            case SNOW: n = 90; break;
            case CLOUDS: n = 5; break;
            case FOG: n = 6; break;
            default: n = 0;
        }
        x = new float[n]; y = new float[n]; v = new float[n]; s = new float[n];
        for (int i = 0; i < n; i++) {
            x[i] = rnd.nextFloat() * w;
            y[i] = rnd.nextFloat() * h;
            if (fx == Fx.SNOW) { v[i] = 1.2f + rnd.nextFloat() * 1.8f; s[i] = 1.6f + rnd.nextFloat() * 2.6f; }
            else if (fx == Fx.CLOUDS || fx == Fx.FOG) { v[i] = 0.15f + rnd.nextFloat() * 0.35f; s[i] = 0.4f + rnd.nextFloat() * 0.7f; }
            else { v[i] = 9f + rnd.nextFloat() * 9f; s[i] = 8f + rnd.nextFloat() * 14f; }
        }
    }

    private void step() {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0 || n == 0) return;
        for (int i = 0; i < n; i++) {
            switch (fx) {
                case RAIN: case DRIZZLE: case THUNDER:
                    y[i] += v[i] * (fx == Fx.DRIZZLE ? 0.5f : 1f);
                    x[i] += v[i] * 0.28f;
                    if (y[i] > h) { y[i] = -20; x[i] = rnd.nextFloat() * w; }
                    break;
                case SNOW:
                    y[i] += v[i];
                    x[i] += (float) Math.sin((t + i) * 0.8f) * 0.8f;
                    if (y[i] > h) { y[i] = -10; x[i] = rnd.nextFloat() * w; }
                    break;
                case CLOUDS: case FOG:
                    x[i] += v[i];
                    if (x[i] > w + 260) x[i] = -260;
                    break;
            }
        }
        if (fx == Fx.THUNDER && rnd.nextInt(220) == 0) flashUntil = System.currentTimeMillis() + 130;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (fx != Fx.NONE) seed();
    }

    @Override
    protected void onDraw(Canvas cv) {
        int w = getWidth(), h = getHeight();
        if (fx == Fx.NONE || n == 0) {
            if (fx == Fx.SUN) drawSun(cv, w, h);
            return;
        }
        switch (fx) {
            case RAIN: case DRIZZLE: case THUNDER: {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(fx == Fx.DRIZZLE ? 1.6f : 2.2f);
                p.setColor(night ? 0x55AEC2D8 : 0x66849BB0);
                for (int i = 0; i < n; i++)
                    cv.drawLine(x[i], y[i], x[i] - s[i] * 0.28f, y[i] - s[i], p);
                if (fx == Fx.THUNDER && System.currentTimeMillis() < flashUntil)
                    cv.drawColor(0x33FFFFFF);
                break;
            }
            case SNOW: {
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xCCFFFFFF);
                for (int i = 0; i < n; i++) cv.drawCircle(x[i], y[i], s[i], p);
                break;
            }
            case CLOUDS: case FOG: {
                p.setStyle(Paint.Style.FILL);
                p.setColor(fx == Fx.FOG ? (night ? 0x22FFFFFF : 0x30FFFFFF) : (night ? 0x1AFFFFFF : 0x26FFFFFF));
                for (int i = 0; i < n; i++) {
                    float cy = h * (0.12f + 0.6f * (i / (float) Math.max(1, n)));
                    float r = (fx == Fx.FOG ? w * 0.9f : 150f) * s[i];
                    cv.drawCircle(x[i], cy, r, p);
                }
                break;
            }
        }
    }

    private void drawSun(Canvas cv, int w, int h) {
        float cx = w * 0.82f, cy = h * 0.14f;
        p.setStyle(Paint.Style.FILL);
        for (int i = 3; i >= 1; i--) {
            p.setColor((0x10FFE08A) | ((0x18 * i) << 24));
            cv.drawCircle(cx, cy, 60f * i, p);
        }
        p.setColor(0x66FFE08A);
        cv.drawCircle(cx, cy, 46f, p);
    }

    @Override protected void onDetachedFromWindow() { if (anim != null) anim.cancel(); super.onDetachedFromWindow(); }
}

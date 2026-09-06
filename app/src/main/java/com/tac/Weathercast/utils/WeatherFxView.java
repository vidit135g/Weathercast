package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * Full-screen weather ambience drawn behind the content — a drifting cloud bank
 * plus falling rain / drizzle / snow, sun glow or fog, à la Apple Weather.
 * The effect is densest near the top and fades toward the bottom.
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

    // precipitation particles
    private float[] px, py, pv, pl, pa;
    private int pn = 0;

    // cloud bank
    private float[] cx, cy, cs, cv;
    private int cn = 0;
    private int cloudColor = 0x33202A38;

    private long flashUntil = 0;

    public WeatherFxView(Context c) { super(c); init(); }
    public WeatherFxView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(1000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setInterpolator(null);
        anim.addUpdateListener(a -> { t += 0.016f; step(); invalidate(); });
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
        fx = next;
        seed();
        if (fx == Fx.NONE) { if (anim.isStarted()) anim.cancel(); invalidate(); }
        else if (!anim.isStarted()) anim.start();
        else invalidate();
    }

    private void seed() {
        int w = Math.max(1, getWidth()), h = Math.max(1, getHeight());

        boolean wantClouds = fx == Fx.RAIN || fx == Fx.DRIZZLE || fx == Fx.THUNDER
                || fx == Fx.SNOW || fx == Fx.CLOUDS;
        cn = wantClouds ? 6 : 0;
        cx = new float[cn]; cy = new float[cn]; cs = new float[cn]; cv = new float[cn];
        for (int i = 0; i < cn; i++) {
            cx[i] = rnd.nextFloat() * (w + 500) - 250;
            // keep the bank near the top so it never fights the headline text
            cy[i] = h * (-0.04f) + h * 0.20f * (i / (float) Math.max(1, cn - 1))
                    + (rnd.nextFloat() - 0.5f) * h * 0.04f;
            cs[i] = 0.85f + rnd.nextFloat() * 0.85f;
            cv[i] = (0.09f + rnd.nextFloat() * 0.18f) * (rnd.nextBoolean() ? 1f : -1f);
        }
        if (fx == Fx.THUNDER)     cloudColor = night ? 0x59202634 : 0x4A38404E;
        else if (fx == Fx.CLOUDS) cloudColor = night ? 0x3AA9B4C6 : 0x3CFFFFFF;
        else if (fx == Fx.SNOW)   cloudColor = night ? 0x40C2CCDA : 0x44FFFFFF;
        else                      cloudColor = night ? 0x4A2E3644 : 0x4E566270; // rain / drizzle

        switch (fx) {
            case RAIN: case THUNDER: pn = 180; break;
            case DRIZZLE: pn = 150; break;
            case SNOW: pn = 100; break;
            default: pn = 0;
        }
        px = new float[pn]; py = new float[pn]; pv = new float[pn]; pl = new float[pn]; pa = new float[pn];
        for (int i = 0; i < pn; i++) reseed(i, w, h, true);
    }

    private void reseed(int i, int w, int h, boolean anywhere) {
        px[i] = rnd.nextFloat() * (w + 120) - 60;
        py[i] = anywhere ? rnd.nextFloat() * h : (-pl0() - rnd.nextFloat() * h * 0.4f);
        if (fx == Fx.SNOW) {
            pv[i] = 1.3f + rnd.nextFloat() * 2.2f;
            pl[i] = 1.6f + rnd.nextFloat() * 3.0f;
            pa[i] = 0.45f + rnd.nextFloat() * 0.5f;
        } else if (fx == Fx.DRIZZLE) {
            pv[i] = 9f + rnd.nextFloat() * 6f;
            pl[i] = 5f + rnd.nextFloat() * 7f;
            pa[i] = 0.18f + rnd.nextFloat() * 0.28f;
        } else {
            pv[i] = 21f + rnd.nextFloat() * 17f;
            pl[i] = 13f + rnd.nextFloat() * 22f;
            pa[i] = 0.22f + rnd.nextFloat() * 0.40f;
        }
    }

    private float pl0() { return fx == Fx.SNOW ? 4f : 20f; }

    private void step() {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;
        float wind = fx == Fx.DRIZZLE ? 1.3f : (fx == Fx.SNOW ? 0f : 2.6f);
        for (int i = 0; i < pn; i++) {
            py[i] += pv[i];
            if (fx == Fx.SNOW) px[i] += (float) Math.sin(t * 1.3f + i) * 0.9f;
            else px[i] += wind;
            if (py[i] > h + 24) reseed(i, w, h, false);
            if (px[i] > w + 60) px[i] -= (w + 120);
            else if (px[i] < -60) px[i] += (w + 120);
        }
        for (int i = 0; i < cn; i++) {
            cx[i] += cv[i];
            float span = w + 500;
            if (cx[i] > w + 250) cx[i] -= span;
            else if (cx[i] < -250) cx[i] += span;
        }
        if (fx == Fx.THUNDER && rnd.nextInt(260) == 0)
            flashUntil = System.currentTimeMillis() + 120;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (fx != Fx.NONE) seed();
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (fx == Fx.NONE) return;

        for (int i = 0; i < cn; i++) drawCloud(c, cx[i], cy[i], cs[i]);

        if (fx == Fx.SUN) { drawSun(c, w, h); return; }
        if (fx == Fx.FOG) { drawFog(c, w, h); return; }

        if (fx == Fx.SNOW) {
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            for (int i = 0; i < pn; i++) {
                p.setColor(argb(pa[i] * fade(py[i], h), 0xFF, 0xFF, 0xFF));
                c.drawCircle(px[i], py[i], pl[i], p);
            }
            return;
        }

        // rain / drizzle / thunder
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(fx == Fx.DRIZZLE ? 1.9f : 2.7f);
        int r = night ? 0xCC : 0x86, gg = night ? 0xDA : 0xA2, b = night ? 0xE8 : 0xC6;
        float dx = fx == Fx.DRIZZLE ? 1.3f : 2.8f;
        for (int i = 0; i < pn; i++) {
            float a = pa[i] * fade(py[i], h) * (night ? 1f : 1.35f);
            p.setColor(argb(a, r, gg, b));
            c.drawLine(px[i], py[i], px[i] - dx * (pl[i] / 16f), py[i] - pl[i], p);
        }
        if (fx == Fx.THUNDER && System.currentTimeMillis() < flashUntil)
            c.drawColor(night ? 0x30FFFFFF : 0x24FFFFFF);
    }

    private float fade(float y, int h) {
        float f = 1.15f - (y / h) * 0.9f;
        return f < 0.30f ? 0.30f : (f > 1f ? 1f : f);
    }

    private int argb(float a, int r, int g, int b) {
        int ai = (int) (a * 255f);
        ai = ai < 0 ? 0 : (ai > 255 ? 255 : ai);
        return (ai << 24) | (r << 16) | (g << 8) | b;
    }

    private void softBlob(Canvas c, float x, float y, float radius) {
        int edge = cloudColor & 0x00FFFFFF;               // same hue, zero alpha
        p.setShader(new RadialGradient(x, y, Math.max(1f, radius),
                cloudColor, edge, Shader.TileMode.CLAMP));
        c.drawCircle(x, y, radius, p);
    }

    /** A soft, blurred-looking cloud built from overlapping radial-gradient puffs. */
    private void drawCloud(Canvas c, float x, float y, float sc) {
        p.setStyle(Paint.Style.FILL);
        float u = 130f * sc;
        softBlob(c, x,               y,               u * 1.00f);
        softBlob(c, x + u * 0.85f,   y + u * 0.10f,   u * 0.80f);
        softBlob(c, x - u * 0.85f,   y + u * 0.14f,   u * 0.74f);
        softBlob(c, x + u * 0.32f,   y - u * 0.26f,   u * 0.64f);
        softBlob(c, x - u * 0.36f,   y - u * 0.20f,   u * 0.58f);
        p.setShader(null);
    }

    private void drawFog(Canvas c, int w, int h) {
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 5; i++) {
            float yy = h * (0.10f + i * 0.17f);
            float drift = (float) Math.sin(t * 0.35f + i * 1.7f) * (w * 0.12f);
            p.setColor(night ? 0x1CFFFFFF : 0x28FFFFFF);
            c.drawRoundRect(-160 + drift, yy, w + 160 + drift, yy + h * 0.13f, 90, 90, p);
        }
    }

    private void drawSun(Canvas c, int w, int h) {
        float ccx = w * 0.80f, ccy = h * 0.12f;
        p.setStyle(Paint.Style.FILL);
        for (int i = 5; i >= 1; i--) {
            p.setColor(argb(0.05f, 0xFF, 0xE0, 0x8A));
            c.drawCircle(ccx, ccy, 44f * i, p);
        }
        p.setColor(argb(0.40f, 0xFF, 0xE6, 0x9A));
        c.drawCircle(ccx, ccy, 38f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(3f);
        for (int i = 0; i < 12; i++) {
            double ang = Math.PI * 2 * i / 12 + t * 0.12;
            p.setColor(argb(0.13f, 0xFF, 0xE6, 0x9A));
            c.drawLine(ccx + (float) Math.cos(ang) * 58f, ccy + (float) Math.sin(ang) * 58f,
                       ccx + (float) Math.cos(ang) * 92f, ccy + (float) Math.sin(ang) * 92f, p);
        }
    }

    @Override protected void onDetachedFromWindow() { if (anim != null) anim.cancel(); super.onDetachedFromWindow(); }
}

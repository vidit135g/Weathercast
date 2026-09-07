package com.tac.Weathercast.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/** A small moon disc that renders the current lit fraction (crescent → gibbous). */
public class MoonView extends View {

    private float illum = 0.5f;   // 0..1 lit fraction
    private boolean waxing = true;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clip = new Path();

    public MoonView(Context c) { super(c); }
    public MoonView(Context c, AttributeSet a) { super(c, a); }

    /** @param illumPercent 0–100, @param waxing true before full moon. */
    public void setPhase(int illumPercent, boolean waxing) {
        this.illum = Math.max(0f, Math.min(1f, illumPercent / 100f));
        this.waxing = waxing;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas cv) {
        float w = getWidth(), h = getHeight();
        float r = Math.min(w, h) / 2f - 2f;
        float cx = w / 2f, cy = h / 2f;

        // lit disc
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFEDE9DC);
        cv.drawCircle(cx, cy, r, p);

        // dark disc slid in from the un-lit side
        clip.reset();
        clip.addCircle(cx, cy, r, Path.Direction.CW);
        cv.save();
        cv.clipPath(clip);
        float dir = waxing ? -1f : 1f;
        float offset = dir * (1f - illum) * 2f * r;
        p.setColor(0xFF23283A);
        cv.drawCircle(cx + offset, cy, r, p);
        cv.restore();

        // rim
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(0x33FFFFFF);
        cv.drawCircle(cx, cy, r, p);
    }
}

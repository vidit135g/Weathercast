package com.tac.Weathercast.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** Risk-zone UV bar with a marker that animates to the current index (0–11+). */
public class UvBarView extends View {

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint marker = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    private float level = 0f;   // 0..11
    private float anim = 0f;

    public UvBarView(Context c) { super(c); init(); }
    public UvBarView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        marker.setColor(Color.WHITE);
        ring.setColor(0x33000000);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(getResources().getDisplayMetrics().density);
    }

    public void setLevel(double uv) {
        level = (float) Math.max(0, Math.min(11, uv));
        ValueAnimator va = ValueAnimator.ofFloat(0f, level / 11f);
        va.setDuration(800);
        va.setInterpolator(new DecelerateInterpolator(1.7f));
        va.addUpdateListener(a -> { anim = (float) a.getAnimatedValue(); invalidate(); });
        va.start();
    }

    @Override
    protected void onDraw(Canvas cv) {
        float d = getResources().getDisplayMetrics().density;
        float h = 7f * d;
        float y = getHeight() / 2f;
        r.set(0, y - h / 2f, getWidth(), y + h / 2f);
        track.setShader(new LinearGradient(0, 0, getWidth(), 0,
                new int[]{ 0xFF3DDC84, 0xFFF4B400, 0xFFDB4437 }, null, Shader.TileMode.CLAMP));
        cv.drawRoundRect(r, h / 2f, h / 2f, track);

        float mx = Math.max(6f * d, Math.min(getWidth() - 6f * d, getWidth() * anim));
        cv.drawCircle(mx, y, 6.5f * d, marker);
        cv.drawCircle(mx, y, 6.5f * d, ring);
        cv.drawCircle(mx, y, 2.5f * d,
                anim < 0.36f ? paint(0xFF3DDC84) : anim < 0.64f ? paint(0xFFF4B400) : paint(0xFFDB4437));
    }

    private Paint _p;
    private Paint paint(int color) {
        if (_p == null) _p = new Paint(Paint.ANTI_ALIAS_FLAG);
        _p.setColor(color);
        return _p;
    }
}

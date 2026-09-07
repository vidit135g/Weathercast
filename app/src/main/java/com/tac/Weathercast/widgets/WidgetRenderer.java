package com.tac.Weathercast.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import androidx.appcompat.content.res.AppCompatResources;

import com.tac.Weathercast.R;
import com.tac.Weathercast.utils.CityTime;
import com.tac.Weathercast.utils.Formatting;
import com.tac.Weathercast.utils.SomaTheme;
import com.tac.Weathercast.utils.UnitConvertor;
import com.tac.Weathercast.utils.UvEstimate;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Draws a whole widget as one Bitmap — a Soma sky gradient with frosted-glass
 * content, so the home-screen widgets match the app exactly (RemoteViews can't
 * host the custom views the app uses).
 */
public final class WidgetRenderer {

    public static final int SIMPLE = 0, EXTENSIVE = 1, TIME = 2;

    private WidgetRenderer() {}

    private static final class Wx {
        String city = "—", country = "", desc = "No data", temp = "--°";
        int owmId = 800; double windMs, hum, pressureHpa, uv;
        Date sunrise, sunset; boolean night;
    }

    public static Bitmap render(Context ctx, int kind, int wPx, int hPx) {
        wPx = Math.max(160, Math.min(1600, wPx));
        hPx = Math.max(120, Math.min(1600, hPx));
        Bitmap bmp = Bitmap.createBitmap(wPx, hPx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        Wx wx = read(ctx, sp);
        int hour = CityTime.calendar(new Date()).get(Calendar.HOUR_OF_DAY);
        int[] sky = SomaTheme.skyStops(wx.owmId, hour, sp.getString("themeBase", "system"));
        boolean transparent = sp.getBoolean("transparentWidget", false);

        float d = ctx.getResources().getDisplayMetrics().density;
        float radius = 22f * d;
        Path clip = new Path();
        clip.addRoundRect(new RectF(0, 0, wPx, hPx), radius, radius, Path.Direction.CW);
        c.clipPath(clip);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new LinearGradient(0, 0, 0, hPx, new int[]{ sky[0], sky[1], sky[2] },
                new float[]{ 0f, 0.55f, 1f }, Shader.TileMode.CLAMP));
        if (transparent) p.setAlpha(150);
        c.drawRect(0, 0, wPx, hPx, p);
        p.setShader(null);
        // top sheen
        p.setShader(new LinearGradient(0, 0, 0, hPx * 0.4f,
                new int[]{ 0x1AFFFFFF, 0x00FFFFFF }, null, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, wPx, hPx * 0.4f, p);
        p.setShader(null);
        // hairline
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1f * d);
        p.setColor(0x33FFFFFF);
        c.drawRoundRect(new RectF(d, d, wPx - d, hPx - d), radius, radius, p);
        p.setStyle(Paint.Style.FILL);

        Typeface serif = Typeface.createFromAsset(ctx.getAssets(), "fonts/Fraunces-SemiBold.ttf");
        Typeface sans = Typeface.createFromAsset(ctx.getAssets(), "fonts/Poppins-Medium.ttf");

        switch (kind) {
            case TIME:      drawTime(ctx, c, wPx, hPx, d, wx, serif, sans); break;
            case EXTENSIVE: drawExtensive(ctx, c, wPx, hPx, d, wx, sp, serif, sans); break;
            default:        drawSimple(ctx, c, wPx, hPx, d, wx, serif, sans); break;
        }
        return bmp;
    }

    // ---------- layouts ----------

    private static void drawSimple(Context ctx, Canvas c, int w, int h, float d, Wx wx,
                                   Typeface serif, Typeface sans) {
        float pad = 18f * d;
        Paint t = text(serif, 0xFFFFFFFF, 40f * d);
        c.drawText(wx.temp, pad, pad + 34f * d, t);

        Paint city = text(sans, 0xF2FFFFFF, 13f * d);
        c.drawText(ellipsize(wx.city + upperCountry(wx), city, w - pad * 2 - iconSize(h, d)),
                pad, h - pad - 18f * d, city);
        Paint desc = text(sans, 0xB8FFFFFF, 12f * d);
        c.drawText(ellipsize(wx.desc, desc, w - pad * 2 - iconSize(h, d)), pad, h - pad, desc);

        drawGlyph(ctx, c, wx, w - pad - iconSize(h, d), (h - iconSize(h, d)) / 2f, iconSize(h, d));
    }

    private static void drawExtensive(Context ctx, Canvas c, int w, int h, float d, Wx wx,
                                      SharedPreferences sp, Typeface serif, Typeface sans) {
        float pad = 20f * d;
        float ic = Math.min(h * 0.42f, 78f * d);
        drawGlyph(ctx, c, wx, w - pad - ic, pad, ic);

        Paint temp = text(serif, 0xFFFFFFFF, 46f * d);
        c.drawText(wx.temp, pad, pad + 40f * d, temp);
        Paint desc = text(sans, 0xF0FFFFFF, 14f * d);
        c.drawText(ellipsize(wx.desc, desc, w - pad * 2 - ic), pad, pad + 66f * d, desc);
        Paint city = text(sans, 0xB0FFFFFF, 12f * d);
        c.drawText(ellipsize(wx.city + upperCountry(wx), city, w - pad * 2 - ic), pad, pad + 86f * d, city);

        // divider
        Paint line = new Paint();
        line.setColor(0x2EFFFFFF);
        float dy = pad + 104f * d;
        c.drawRect(pad, dy, w - pad, dy + 1f * d, line);

        // stat row
        String unit = "°";
        String[][] stats = {
                { "WIND", new DecimalFormat("0.#").format(
                        UnitConvertor.convertWind(wx.windMs, sp)) + " " + sp.getString("speedUnit", "m/s") },
                { "HUMIDITY", Math.round(wx.hum) + "%" },
                { "UV", new DecimalFormat("0.#").format(wx.uv) },
                { "PRESSURE", Math.round(wx.pressureHpa) + " " + sp.getString("pressureUnit", "hPa") },
        };
        Paint lab = text(sans, 0x8CFFFFFF, 9.5f * d);
        Paint val = text(serif, 0xFFFFFFFF, 15f * d);
        float colW = (w - pad * 2) / stats.length;
        float sy = dy + 22f * d;
        for (int i = 0; i < stats.length; i++) {
            float x = pad + colW * i;
            c.drawText(stats[i][0], x, sy, lab);
            c.drawText(stats[i][1], x, sy + 20f * d, val);
        }

        // sun line
        if (wx.sunrise != null && wx.sunset != null) {
            Paint sun = text(sans, 0xC2FFFFFF, 11f * d);
            c.drawText("↑ " + CityTime.hm(wx.sunrise) + "     ↓ " + CityTime.hm(wx.sunset),
                    pad, sy + 44f * d, sun);
        }
    }

    private static void drawTime(Context ctx, Canvas c, int w, int h, float d, Wx wx,
                                 Typeface serif, Typeface sans) {
        float pad = 20f * d;
        boolean tall = h > w * 0.85f;
        Calendar now = Calendar.getInstance();
        String clock = String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        String date = new java.text.SimpleDateFormat("EEEE d MMM", java.util.Locale.getDefault())
                .format(now.getTime());

        Paint tp = text(serif, 0xFFFFFFFF, Math.min(52f * d, w * 0.28f));
        Paint dp = text(sans, 0xCCFFFFFF, 12.5f * d);

        c.drawText(clock, pad, pad + tp.getTextSize() * 0.82f, tp);
        c.drawText(date, pad, pad + tp.getTextSize() * 0.82f + 20f * d, dp);

        float ic = tall ? Math.min(w * 0.5f, 96f * d) : Math.min(h * 0.55f, 68f * d);
        if (tall) {
            drawGlyph(ctx, c, wx, (w - ic) / 2f, h * 0.46f, ic);
            Paint big = text(serif, 0xFFFFFFFF, 30f * d);
            c.drawText(wx.temp, pad, h - pad, big);
            Paint ds = text(sans, 0xC2FFFFFF, 12f * d);
            c.drawText(ellipsize(wx.desc, ds, w - pad * 2 - big.measureText(wx.temp) - 12f * d),
                    pad + big.measureText(wx.temp) + 12f * d, h - pad - 4f * d, ds);
        } else {
            drawGlyph(ctx, c, wx, w - pad - ic, (h - ic) / 2f, ic);
            Paint big = text(serif, 0xFFFFFFFF, 20f * d);
            c.drawText(wx.temp, w - pad - ic - 8f * d - big.measureText(wx.temp), h - pad, big);
        }
    }

    // ---------- helpers ----------

    private static Wx read(Context ctx, SharedPreferences sp) {
        Wx wx = new Wx();
        String json = sp.getString("lastToday", "");
        if (json.isEmpty()) return wx;
        try {
            JSONObject r = new JSONObject(json);
            JSONObject main = r.getJSONObject("main");
            float k = (float) main.getDouble("temp");
            float t = UnitConvertor.convertTemperature(k, sp);
            wx.temp = Math.round(t) + "°";
            wx.city = r.optString("name", "—");
            JSONObject sys = r.optJSONObject("sys");
            if (sys != null) {
                wx.country = sys.optString("country", "");
                if (sys.has("sunrise")) wx.sunrise = new Date(sys.getLong("sunrise") * 1000L);
                if (sys.has("sunset")) wx.sunset = new Date(sys.getLong("sunset") * 1000L);
            }
            JSONObject w0 = r.getJSONArray("weather").getJSONObject(0);
            String desc = w0.getString("description");
            wx.desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
            wx.owmId = w0.getInt("id");
            wx.hum = main.optDouble("humidity", 0);
            wx.pressureHpa = main.optDouble("pressure", 0);
            JSONObject wind = r.optJSONObject("wind");
            if (wind != null) wx.windMs = wind.optDouble("speed", 0);
            if (r.has("timezone")) CityTime.setOffsetSeconds(r.getInt("timezone"));
            double lat = r.optJSONObject("coord") != null ? r.getJSONObject("coord").optDouble("lat", 0) : 0;
            double lon = r.optJSONObject("coord") != null ? r.getJSONObject("coord").optDouble("lon", 0) : 0;
            wx.uv = UvEstimate.now(lat, lon, wx.owmId);
            long n = System.currentTimeMillis();
            wx.night = wx.sunrise != null && wx.sunset != null
                    && (n < wx.sunrise.getTime() || n > wx.sunset.getTime());
        } catch (Exception ignored) {}
        return wx;
    }

    private static void drawGlyph(Context ctx, Canvas c, Wx wx, float x, float y, float size) {
        try {
            Drawable dr = AppCompatResources.getDrawable(ctx,
                    Formatting.somaIllustration(wx.owmId, wx.night));
            if (dr == null) return;
            dr.setBounds((int) x, (int) y, (int) (x + size), (int) (y + size));
            dr.draw(c);
        } catch (Exception ignored) {}
    }

    private static Paint text(Typeface tf, int color, float size) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(tf);
        p.setColor(color);
        p.setTextSize(size);
        p.setShadowLayer(size * 0.18f, 0, size * 0.03f, 0x40000000);
        return p;
    }

    private static float iconSize(int h, float d) { return Math.min(h * 0.5f, 52f * d); }

    private static String upperCountry(Wx wx) {
        return wx.country == null || wx.country.isEmpty() ? "" : ", " + wx.country;
    }

    private static String ellipsize(String s, Paint p, float maxW) {
        if (p.measureText(s) <= maxW || s.length() < 2) return s;
        while (s.length() > 1 && p.measureText(s + "…") > maxW) s = s.substring(0, s.length() - 1);
        return s + "…";
    }
}

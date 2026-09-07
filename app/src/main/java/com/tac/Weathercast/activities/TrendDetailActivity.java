package com.tac.Weathercast.activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.tac.Weathercast.R;
import com.tac.Weathercast.models.Weather;
import com.tac.Weathercast.utils.CityTime;
import com.tac.Weathercast.utils.ForecastCache;
import com.tac.Weathercast.utils.SomaTheme;
import com.tac.Weathercast.utils.TrendChartView;
import com.tac.Weathercast.utils.TrendSeries;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** A per-metric detail screen: big value, stats, a full chart and an hour list. */
public class TrendDetailActivity extends AppCompatActivity {

    private int accent;
    private Typeface serif;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_trend_detail);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        serif = ResourcesCompat.getFont(this, R.font.fraunces);
        TrendSeries.Type type;
        try { type = TrendSeries.Type.valueOf(getIntent().getStringExtra("type")); }
        catch (Exception e) { type = TrendSeries.Type.TEMP; }

        List<Weather> data = ForecastCache.get();
        findViewById(R.id.back).setOnClickListener(x -> finish());

        int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        accent = SomaTheme.forNow(hr, 800, sp.getString("appearance", "auto")).heroAccent;
        int grid = isDark() ? 0x24FFFFFF : 0x16000000;
        int label = col(R.color.soma_text_muted);

        TrendSeries series = TrendSeries.of(type, data, sp);
        ((TextView) findViewById(R.id.title)).setText(series.title);
        ((TextView) findViewById(R.id.reading)).setText(series.reading);

        String u = series.unit;
        String bigNum = (type == TrendSeries.Type.RAIN || type == TrendSeries.Type.WIND)
                ? fmt(series.now) : String.valueOf(Math.round(series.now));
        ((TextView) findViewById(R.id.bigValue)).setText(bigNum + u);
        String arrow = series.trendWord.equals("rising") ? "↑  "
                : series.trendWord.equals("falling") ? "↓  "
                : series.trendWord.equals("steady") ? "→  " : "";
        ((TextView) findViewById(R.id.trendChip)).setText(arrow + series.trendWord);

        if (type == TrendSeries.Type.RAIN) {
            stat(1, "5-day total", fmt(series.sum) + " mm");
            stat(2, "Wettest slot", fmt(series.max) + " mm");
            stat(3, "Dry slots", drySlots(series) + "/" + series.values.length);
        } else {
            stat(1, "High", fmt(series.max) + u);
            stat(2, "Low", fmt(series.min) + u);
            stat(3, "Average", fmt(series.avg) + u);
        }

        TrendChartView chart = findViewById(R.id.chart);
        chart.setColors(accent, grid, label);
        chart.setSeries(series);

        buildHourList(series);
    }

    private void buildHourList(TrendSeries s) {
        LinearLayout list = findViewById(R.id.hourList);
        long now = System.currentTimeMillis();
        int shown = 0;
        for (int i = 0; i < s.values.length && shown < 24; i++) {
            if (s.times[i] < now - 3600_000L) continue;
            list.addView(hourRow(s, i, shown == 0));
            shown++;
        }
    }

    private View hourRow(TrendSeries s, int i, boolean first) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(11), 0, dp(11));

        TextView time = new TextView(this);
        time.setText(first ? "Now" : CityTime.format("EEE HH:mm").format(new Date(s.times[i])));
        time.setTextColor(col(R.color.soma_text_secondary));
        time.setTextSize(13);
        time.setLayoutParams(new LinearLayout.LayoutParams(dp(94), ViewGroup.LayoutParams.WRAP_CONTENT));

        float base = s.bars ? 0f : s.min;
        float span = Math.max(0.001f, s.max - base);
        float frac = Math.max(0.05f, Math.min(1f, (s.values[i] - base) / span));

        LinearLayout barWrap = new LinearLayout(this);
        barWrap.setOrientation(LinearLayout.HORIZONTAL);
        barWrap.setGravity(Gravity.CENTER_VERTICAL);
        barWrap.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        View bar = new View(this);
        bar.setBackgroundColor(withAlpha(accent, 0x3A));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, dp(8), frac);
        bar.setLayoutParams(blp);
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, dp(1), Math.max(0.001f, 1f - frac)));
        barWrap.addView(bar); barWrap.addView(spacer);

        TextView val = new TextView(this);
        val.setText(fmt(s.values[i]) + s.unit);
        if (serif != null) val.setTypeface(serif);
        val.setTextColor(col(R.color.soma_ink));
        val.setTextSize(14);
        val.setGravity(Gravity.END);
        val.setLayoutParams(new LinearLayout.LayoutParams(dp(62), ViewGroup.LayoutParams.WRAP_CONTENT));

        row.addView(time); row.addView(barWrap); row.addView(val);
        return row;
    }

    private int drySlots(TrendSeries s) {
        int c = 0; for (float x : s.values) if (x < 0.05f) c++; return c;
    }

    private void stat(int n, String label, String value) {
        setText(getResources().getIdentifier("stat" + n + "Label", "id", getPackageName()), label);
        setText(getResources().getIdentifier("stat" + n + "Value", "id", getPackageName()), value);
    }

    private void setText(int id, String s) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(s);
    }

    private boolean isDark() {
        int mode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private static String fmt(float x) {
        return new DecimalFormat(x == Math.rint(x) ? "0" : "0.#").format(x);
    }
    private static int withAlpha(int c, int a) { return (a << 24) | (c & 0x00FFFFFF); }
    private int col(int res) { return ContextCompat.getColor(this, res); }
    private int dp(int v) { return Math.round(getResources().getDisplayMetrics().density * v); }
}

package com.tac.Weathercast.fragments;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.tac.Weathercast.R;
import com.tac.Weathercast.activities.MainActivity;
import com.tac.Weathercast.models.Weather;
import com.tac.Weathercast.utils.Briefing;
import com.tac.Weathercast.utils.MoonView;
import com.tac.Weathercast.utils.UnitConvertor;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

/** The centre nav feature — a computed "briefing": comparison, day-ahead
 *  timeline, standout insights and a sun & moon reading. */
public class BriefingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_briefing, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        MainActivity a = (MainActivity) requireActivity();
        Weather today = a.getTodayWeatherData();
        List<Weather> todayHrs = a.getLongTermTodayWeatherData();
        List<Weather> future = new java.util.ArrayList<>();
        if (a.getLongTermTomorrowWeatherData() != null) future.addAll(a.getLongTermTomorrowWeatherData());
        if (a.getLongTermWeatherData() != null) future.addAll(a.getLongTermWeatherData());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        if (today == null) return;

        text(v, R.id.briefCity, today.getCity());

        Briefing.Result r = Briefing.build(today, todayHrs, future);
        text(v, R.id.briefNarrative, r.narrative);

        // ---- compare ----
        Briefing.Compare cmp = r.compare;
        if (cmp != null && !cmp.delta.isEmpty()) {
            text(v, R.id.cmpTodayTemp, t(cmp.todayHi, sp) + " / " + t(cmp.todayLo, sp));
            text(v, R.id.cmpTmrwTemp, t(cmp.tmrwHi, sp) + " / " + t(cmp.tmrwLo, sp));
            text(v, R.id.cmpTodayRain, cmp.todayRain < 0.2 ? "dry" : fmt(cmp.todayRain) + " mm rain");
            text(v, R.id.cmpTmrwRain, cmp.tmrwRain < 0.2 ? "dry" : fmt(cmp.tmrwRain) + " mm rain");
            text(v, R.id.cmpDelta, cmp.delta);
        } else {
            v.findViewById(R.id.compareCard).setVisibility(View.GONE);
        }

        // ---- day-ahead timeline ----
        buildTimeline(v, todayHrs, future, sp);

        // ---- insights ----
        LinearLayout list = v.findViewById(R.id.insightList);
        for (Briefing.Insight in : r.insights) list.addView(insightRow(in));

        // ---- sun & moon ----
        try {
            long rise = today.getSunrise().getTime(), set = today.getSunset().getTime();
            long mins = Math.max(0, (set - rise) / 60000L);
            text(v, R.id.sunHours, (mins / 60) + "h " + (mins % 60) + "m");
            text(v, R.id.sunTimes, "↑ " + hm(rise) + "    ↓ " + hm(set));
        } catch (Exception e) {
            v.findViewById(R.id.sunHours).setVisibility(View.GONE);
        }
        Briefing.Moon m = r.moon;
        if (m != null) {
            ((MoonView) v.findViewById(R.id.moonView)).setPhase(m.illumination, m.waxing);
            text(v, R.id.moonPhase, m.phase);
            text(v, R.id.moonSub, m.illumination + "% lit · full in " + m.nextFull);
        }
    }

    private void buildTimeline(View v, List<Weather> todayHrs, List<Weather> future, SharedPreferences sp) {
        LinearLayout strip = v.findViewById(R.id.timelineStrip);
        java.util.ArrayList<Weather> slots = new java.util.ArrayList<>();
        if (todayHrs != null) slots.addAll(todayHrs);
        if (future != null) slots.addAll(future);
        long now = System.currentTimeMillis();
        int added = 0;
        for (Weather w : slots) {
            if (w.getDate() == null || w.getDate().getTime() < now - 3600_000L) continue;
            strip.addView(timelineChip(w, sp));
            if (++added >= 10) break;
        }
        if (added == 0) strip.setVisibility(View.GONE);
    }

    private View timelineChip(Weather w, SharedPreferences sp) {
        int pad = dp(6);
        LinearLayout col = new LinearLayout(getContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(dp(12), dp(12), dp(12), dp(12));
        col.setBackgroundResource(R.drawable.hourly_capsule_bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(66), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(8);
        col.setLayoutParams(lp);

        Calendar cal = com.tac.Weathercast.utils.CityTime.calendar(w.getDate());
        TextView hour = new TextView(getContext());
        hour.setText(String.format("%02d:00", cal.get(Calendar.HOUR_OF_DAY)));
        hour.setTextColor(color(R.color.soma_text_muted));
        hour.setTextSize(11);

        View dot = new View(getContext());
        int id; try { id = Integer.parseInt(w.getId()); } catch (Exception e) { id = 800; }
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(8), dp(8));
        dlp.topMargin = dp(8); dlp.bottomMargin = dp(8);
        dot.setLayoutParams(dlp);
        dot.setBackgroundResource(conditionChip(id));

        TextView temp = new TextView(getContext());
        try {
            temp.setText(Math.round(UnitConvertor.convertTemperature(Float.parseFloat(w.getTemperature()), sp)) + "°");
        } catch (Exception e) { temp.setText("--"); }
        temp.setTextColor(color(R.color.soma_ink));
        temp.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        temp.setTextSize(15);

        col.addView(hour); col.addView(dot); col.addView(temp);
        return col;
    }

    private View insightRow(Briefing.Insight in) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setBackgroundResource(R.drawable.bento_tile);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        row.setLayoutParams(lp);
        row.setElevation(dp(2));
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            row.setOutlineSpotShadowColor(color(R.color.soma_shadow));
            row.setOutlineAmbientShadowColor(color(R.color.soma_shadow));
        }

        int[] style = styleFor(in.key);
        ImageView icon = new ImageView(getContext());
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(34), dp(34));
        ilp.rightMargin = dp(14);
        icon.setLayoutParams(ilp);
        icon.setPadding(dp(7), dp(7), dp(7), dp(7));
        icon.setBackgroundResource(style[1]);
        icon.setImageResource(style[0]);
        icon.setColorFilter(color(style[2]));

        LinearLayout txt = new LinearLayout(getContext());
        txt.setOrientation(LinearLayout.VERTICAL);
        txt.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView head = new TextView(getContext());
        head.setText(in.headline);
        head.setTextColor(color(R.color.soma_ink));
        head.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        head.setTextSize(14);
        TextView body = new TextView(getContext());
        body.setText(in.detail);
        body.setTextColor(color(R.color.soma_text_secondary));
        body.setTextSize(12);
        body.setLineSpacing(dp(2), 1f);
        txt.addView(head); txt.addView(body);

        row.addView(icon); row.addView(txt);
        return row;
    }

    private int[] styleFor(String key) {
        switch (key) {
            case "pressure": return new int[]{R.drawable.ic_feather_activity, R.drawable.soma_chip_saffron, R.color.soma_saffron};
            case "rain":     return new int[]{R.drawable.ic_feather_droplet,  R.drawable.soma_chip_indigo,  R.color.soma_indigo};
            case "temp":     return new int[]{R.drawable.ic_feather_sun,      R.drawable.soma_chip_ember,   R.color.soma_ember};
            case "wind":     return new int[]{R.drawable.ic_feather_wind,     R.drawable.soma_chip_forest,  R.color.soma_forest};
            case "uv":       return new int[]{R.drawable.ic_feather_sun,      R.drawable.soma_chip_ember,   R.color.soma_ember};
            default:         return new int[]{R.drawable.ic_feather_clock,    R.drawable.soma_chip_muted,   R.color.soma_text_muted};
        }
    }

    private int conditionChip(int owmId) {
        int g = owmId / 100;
        if (g == 2 || g == 3 || g == 5) return R.drawable.soma_chip_indigo;
        if (g == 6) return R.drawable.soma_chip_muted;
        if (owmId == 800) return R.drawable.soma_chip_ember;
        if (g == 8) return R.drawable.soma_chip_muted;
        return R.drawable.soma_chip_saffron;
    }

    private String t(double c, SharedPreferences sp) {
        return Math.round(UnitConvertor.convertTemperature((float) (c + 273.15), sp)) + "°";
    }
    private static String fmt(double x) { return new DecimalFormat("0.#").format(x); }
    private String hm(long ms) {
        return com.tac.Weathercast.utils.CityTime.hm(new java.util.Date(ms));
    }
    private int dp(int v) { return Math.round(getResources().getDisplayMetrics().density * v); }
    private int color(int res) { return ContextCompat.getColor(requireContext(), res); }
    private void text(View root, int id, String s) {
        TextView tv = root.findViewById(id); if (tv != null) tv.setText(s);
    }
}

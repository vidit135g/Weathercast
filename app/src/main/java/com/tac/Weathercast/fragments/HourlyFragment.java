package com.tac.Weathercast.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tac.Weathercast.R;
import com.tac.Weathercast.activities.MainActivity;
import com.tac.Weathercast.models.Weather;
import com.tac.Weathercast.utils.Formatting;
import com.tac.Weathercast.utils.SomaTheme;
import com.tac.Weathercast.utils.TempCurveView;
import com.tac.Weathercast.utils.UnitConvertor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** The Hourly tab: a temperature curve + hour-by-hour rows for the next ~48h. */
public class HourlyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hourly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        List<Weather> all = ((MainActivity) requireActivity()).getForecastSeries();
        List<Weather> data = new java.util.ArrayList<>();
        long cutoff = System.currentTimeMillis() - 3 * 3600_000L;
        for (Weather w : all) if (w.getDate() != null && w.getDate().getTime() >= cutoff) data.add(w);
        if (data.isEmpty()) data = all;
        if (data.isEmpty()) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());

        int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int accent = SomaTheme.forNow(hr, 800,
                sp.getString("appearance", "auto")).heroAccent;
        int textColor = 0x9EFFFFFF;

        int n = Math.min(16, data.size());
        float[] temps = new float[n];
        String[] hours = new String[n];
        SimpleDateFormat hf = com.tac.Weathercast.utils.CityTime.format("HH");
        for (int i = 0; i < n; i++) {
            Weather w = data.get(i);
            try { temps[i] = UnitConvertor.convertTemperature(Float.parseFloat(w.getTemperature()), sp); }
            catch (Exception e) { temps[i] = i > 0 ? temps[i - 1] : 15f; }
            hours[i] = hf.format(w.getDate());
        }
        TempCurveView curve = v.findViewById(R.id.tempCurve);
        curve.setData(temps, hours, accent, textColor);

        LinearLayout rows = v.findViewById(R.id.hourlyRows);
        SimpleDateFormat tf = com.tac.Weathercast.utils.CityTime.format("EEE HH:mm");
        String speedUnit = sp.getString("speedUnit", "m/s");
        int show = Math.min(20, data.size());
        for (int i = 0; i < show; i++) {
            Weather w = data.get(i);
            View row = LayoutInflater.from(getContext()).inflate(R.layout.hourly_detail_row, rows, false);
            boolean now = i == 0;
            ((TextView) row.findViewById(R.id.hTime)).setText(now ? "Now" : tf.format(w.getDate()));

            int owmId;
            try { owmId = Integer.parseInt(w.getId()); } catch (Exception e) { owmId = 800; }
            int h24 = com.tac.Weathercast.utils.CityTime.hourOfDay(w.getDate());
            ((ImageView) row.findViewById(R.id.hIcon)).setImageResource(
                    Formatting.somaIllustration(owmId, h24 < 6 || h24 >= 20));

            double wind = 0;
            try { wind = UnitConvertor.convertWind(Double.parseDouble(w.getWind()), sp); } catch (Exception ignored) {}
            String rain = "";
            try {
                double r = Double.parseDouble(w.getRain());
                if (r > 0.05) rain = "  ·  " + new DecimalFormat("0.#").format(r) + " mm";
            } catch (Exception ignored) {}
            ((TextView) row.findViewById(R.id.hPrecip)).setText(
                    "Wind " + new DecimalFormat("0.#").format(wind) + " " + speedUnit + rain);

            try {
                float t = UnitConvertor.convertTemperature(Float.parseFloat(w.getTemperature()), sp);
                ((TextView) row.findViewById(R.id.hTemp)).setText(Math.round(t) + "°");
            } catch (Exception e) {
                ((TextView) row.findViewById(R.id.hTemp)).setText("--");
            }

            // precip intensity bar (0..~4mm -> full width)
            final View fill = row.findViewById(R.id.hRainFill);
            float rmm = 0;
            try { rmm = (float) Double.parseDouble(w.getRain()); } catch (Exception ignored) {}
            final float frac = Math.max(0f, Math.min(1f, rmm / 4f));
            final int fi = i;
            row.post(() -> {
                android.view.ViewGroup.LayoutParams lp = fill.getLayoutParams();
                lp.width = 0; fill.setLayoutParams(lp);
                if (frac <= 0f) return;
                fill.animate().setStartDelay(120 + fi * 22L).setDuration(420)
                        .setUpdateListener(a -> {
                            android.view.ViewGroup.LayoutParams l = fill.getLayoutParams();
                            l.width = (int) (((View) fill.getParent()).getWidth() * frac * a.getAnimatedFraction());
                            fill.setLayoutParams(l);
                        }).start();
            });

            row.setAlpha(0f);
            row.animate().alpha(1f).setStartDelay(i * 22L).setDuration(260).start();
            rows.addView(row);
        }
        com.tac.Weathercast.utils.SkyTint.apply(v);
        v.post(() -> com.tac.Weathercast.utils.SkyTint.apply(v));
        v.post(() -> { android.view.View cc = ((android.view.ViewGroup) v).getChildAt(0);
            if (cc instanceof android.view.ViewGroup) com.tac.Weathercast.utils.Anim.enterChildren((android.view.ViewGroup) cc); });
    }

    private int resolveColor(int res) {
        return androidx.core.content.ContextCompat.getColor(requireContext(), res);
    }
}

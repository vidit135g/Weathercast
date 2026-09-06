package com.tac.Weathercast.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.tac.Weathercast.R;
import com.tac.Weathercast.activities.MainActivity;
import com.tac.Weathercast.models.Weather;
import com.tac.Weathercast.utils.SparklineView;
import com.tac.Weathercast.utils.TempCurveView;
import com.tac.Weathercast.utils.UnitConvertor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** The Trends tab — 5-day sparkline cards for temperature, rain, pressure and wind. */
public class TrendsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_trends, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        List<Weather> data = ((MainActivity) requireActivity()).getLongTermWeatherData();
        if (data == null || data.size() < 3) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int n = data.size();

        float[] temp = new float[n], rain = new float[n], press = new float[n], wind = new float[n];
        String[] days = new String[n];
        SimpleDateFormat df = new SimpleDateFormat("EEE", Locale.getDefault());
        for (int i = 0; i < n; i++) {
            Weather w = data.get(i);
            temp[i] = safe(() -> UnitConvertor.convertTemperature(Float.parseFloat(w.getTemperature()), sp), i, temp);
            rain[i] = safe(() -> (float) Double.parseDouble(w.getRain()), i, rain);
            press[i] = safe(() -> (float) UnitConvertor.convertPressure(Float.parseFloat(w.getPressure()), sp), i, press);
            wind[i] = safe(() -> (float) UnitConvertor.convertWind(Double.parseDouble(w.getWind()), sp), i, wind);
            days[i] = df.format(w.getDate());
        }

        int forest = c(R.color.soma_forest), indigo = c(R.color.soma_indigo),
                ember = c(R.color.soma_ember), saffron = c(R.color.soma_saffron),
                muted = c(R.color.soma_text_muted);

        ((TempCurveView) v.findViewById(R.id.curveTemp)).setData(temp, days, ember, muted);
        ((SparklineView) v.findViewById(R.id.sparkRain)).setData(rain, indigo);
        ((SparklineView) v.findViewById(R.id.sparkPressure)).setData(press, saffron);
        ((SparklineView) v.findViewById(R.id.sparkWind)).setData(wind, forest);

        DecimalFormat f0 = new DecimalFormat("0");
        String tu = sp.getString("unit", "°C");
        String pu = sp.getString("pressureUnit", "hPa");
        String su = sp.getString("speedUnit", "m/s");
        set(v, R.id.rangeTemp, f0.format(min(temp)) + tu + "  –  " + f0.format(max(temp)) + tu);
        set(v, R.id.rangeRain, max(rain) < 0.1f ? "None expected" : "up to " + new DecimalFormat("0.#").format(max(rain)) + " mm");
        set(v, R.id.rangePressure, f0.format(min(press)) + "  –  " + f0.format(max(press)) + " " + pu);
        set(v, R.id.rangeWind, f0.format(min(wind)) + "  –  " + f0.format(max(wind)) + " " + su);
    }

    private interface Fn { float get() throws Exception; }
    private static float safe(Fn fn, int i, float[] prev) {
        try { return fn.get(); } catch (Exception e) { return i > 0 ? prev[i - 1] : 0f; }
    }
    private static float min(float[] a) { float m = Float.MAX_VALUE; for (float x : a) m = Math.min(m, x); return m; }
    private static float max(float[] a) { float m = -Float.MAX_VALUE; for (float x : a) m = Math.max(m, x); return m; }
    private int c(int res) { return ContextCompat.getColor(requireContext(), res); }
    private void set(View root, int id, String t) { ((TextView) root.findViewById(id)).setText(t); }
}

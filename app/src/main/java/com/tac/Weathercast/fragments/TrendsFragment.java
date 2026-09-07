package com.tac.Weathercast.fragments;

import android.content.Intent;
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
import com.tac.Weathercast.activities.TrendDetailActivity;
import com.tac.Weathercast.models.Weather;
import com.tac.Weathercast.utils.SparklineView;
import com.tac.Weathercast.utils.TempCurveView;
import com.tac.Weathercast.utils.TrendSeries;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/** The Trends tab — five tappable metric cards, each opening a detail screen. */
public class TrendsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_trends, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        List<Weather> data = ((MainActivity) requireActivity()).getForecastSeries();
        if (data == null || data.size() < 3) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(requireContext());

        int ember = c(R.color.soma_ember), indigo = c(R.color.soma_indigo),
                saffron = c(R.color.soma_saffron), forest = c(R.color.soma_forest),
                mint = c(R.color.soma_mint), muted = 0x9EFFFFFF;

        TrendSeries temp = TrendSeries.of(TrendSeries.Type.TEMP, data, sp);
        TrendSeries rain = TrendSeries.of(TrendSeries.Type.RAIN, data, sp);
        TrendSeries press = TrendSeries.of(TrendSeries.Type.PRESSURE, data, sp);
        TrendSeries wind = TrendSeries.of(TrendSeries.Type.WIND, data, sp);
        TrendSeries hum = TrendSeries.of(TrendSeries.Type.HUMIDITY, data, sp);

        int n = Math.min(temp.values.length, 24);
        String[] days = new String[n];
        java.text.SimpleDateFormat df = com.tac.Weathercast.utils.CityTime.format("EEE");
        for (int i = 0; i < n; i++) days[i] = df.format(new Date(temp.times[i]));
        ((TempCurveView) v.findViewById(R.id.curveTemp)).setData(slice(temp.values, n), days, ember, muted);
        ((SparklineView) v.findViewById(R.id.sparkRain)).setData(rain.values, indigo);
        ((SparklineView) v.findViewById(R.id.sparkPressure)).setData(press.values, saffron);
        ((SparklineView) v.findViewById(R.id.sparkWind)).setData(wind.values, forest);
        ((SparklineView) v.findViewById(R.id.sparkHumidity)).setData(hum.values, mint);

        DecimalFormat f0 = new DecimalFormat("0");
        sub(v, R.id.tTempSub, f0.format(temp.min) + temp.unit + " – " + f0.format(temp.max) + temp.unit + "  ·  " + temp.trendWord);
        sub(v, R.id.tRainSub, rain.sum < 0.5f ? "Dry all week" : f0.format(rain.sum) + " mm over 5 days");
        sub(v, R.id.tPressureSub, f0.format(press.min) + " – " + f0.format(press.max) + " " + press.unit + "  ·  " + press.trendWord);
        sub(v, R.id.tWindSub, f0.format(wind.min) + " – " + f0.format(wind.max) + " " + wind.unit);
        sub(v, R.id.tHumiditySub, f0.format(hum.min) + " – " + f0.format(hum.max) + " %");

        ((TextView) v.findViewById(R.id.trendsHeadline)).setText(headline(temp, rain, press));

        open(v, R.id.cardTemp, TrendSeries.Type.TEMP);
        open(v, R.id.cardRain, TrendSeries.Type.RAIN);
        open(v, R.id.cardPressure, TrendSeries.Type.PRESSURE);
        open(v, R.id.cardWind, TrendSeries.Type.WIND);
        open(v, R.id.cardHumidity, TrendSeries.Type.HUMIDITY);

        com.tac.Weathercast.utils.SkyTint.apply(v);
        v.post(() -> com.tac.Weathercast.utils.SkyTint.apply(v));
    }

    private String headline(TrendSeries temp, TrendSeries rain, TrendSeries press) {
        String a = temp.trendWord.equals("rising") ? "Warming up"
                : temp.trendWord.equals("falling") ? "Cooling down" : "Steady temperatures";
        String b;
        if (rain.sum < 0.5f) b = "and dry through the week";
        else b = "with " + new DecimalFormat("0").format(rain.sum) + " mm of rain to come";
        return a + " " + b + ".";
    }

    private void open(View root, int cardId, TrendSeries.Type type) {
        View card = root.findViewById(cardId);
        if (card == null) return;
        card.setClickable(true);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            card.setForeground(ContextCompat.getDrawable(requireContext(),
                    resolveAttr(android.R.attr.selectableItemBackground)));
        }
        card.setOnClickListener(x -> {
            Intent it = new Intent(requireContext(), TrendDetailActivity.class);
            it.putExtra("type", type.name());
            startActivity(it);
        });
    }

    private int resolveAttr(int attr) {
        android.util.TypedValue tv = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(attr, tv, true);
        return tv.resourceId;
    }

    private static float[] slice(float[] a, int n) {
        float[] o = new float[Math.min(n, a.length)];
        System.arraycopy(a, 0, o, 0, o.length);
        return o;
    }
    private int c(int res) { return ContextCompat.getColor(requireContext(), res); }
    private void sub(View root, int id, String t) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(t);
    }
}

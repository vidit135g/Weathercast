package com.tac.Weathercast.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tac.Weathercast.R;
import com.tac.Weathercast.models.Weather;
import com.tac.Weathercast.utils.Formatting;
import com.tac.Weathercast.utils.UnitConvertor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Horizontal strip of upcoming 3-hourly conditions as Soma capsules. */
public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.VH> {

    private final Context context;
    private final List<Weather> items;

    public HourlyAdapter(Context context, List<Weather> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_capsule, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Weather w = items.get(position);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        Calendar cal = Calendar.getInstance();
        cal.setTime(w.getDate());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        boolean isToday = cal.get(Calendar.DAY_OF_YEAR)
                == Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        boolean now = isToday && Math.abs(hour - Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) < 2;

        h.time.setText(now ? "NOW" : new SimpleDateFormat("HH:mm", Locale.getDefault()).format(w.getDate()));

        int owmId;
        try { owmId = Integer.parseInt(w.getId()); } catch (Exception e) { owmId = 800; }
        h.icon.setImageResource(Formatting.somaIllustration(owmId, hour < 6 || hour >= 20));

        try {
            float t = UnitConvertor.convertTemperature(Float.parseFloat(w.getTemperature()), sp);
            h.temp.setText(new DecimalFormat("0").format(t) + "°");
        } catch (Exception e) {
            h.temp.setText("--");
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView time, temp;
        final ImageView icon;
        VH(View v) {
            super(v);
            time = v.findViewById(R.id.hourTime);
            temp = v.findViewById(R.id.hourTemp);
            icon = v.findViewById(R.id.hourIcon);
        }
    }
}

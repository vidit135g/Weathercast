package com.tac.Weathercast.utils;

import android.content.Context;

import com.tac.Weathercast.R;

public class Formatting {

    private Context context;

    public Formatting(Context context) {
        this.context = context;
    }

    /**
     * Soma-style illustration for a condition. Returns a drawable resource id
     * from the {@code soma_wx_*} vector set.
     */
    public static int somaIllustration(int owmId, boolean isNight) {
        int group = owmId / 100;
        if (owmId == 800) return isNight ? R.drawable.soma_wx_clear_night : R.drawable.soma_wx_clear_day;
        switch (group) {
            case 2: return R.drawable.soma_wx_thunder;
            case 3: return R.drawable.soma_wx_drizzle;
            case 5: return R.drawable.soma_wx_rain;
            case 6: return R.drawable.soma_wx_snow;
            case 7:
                if (owmId == 771 || owmId == 781) return R.drawable.soma_wx_wind; // squall / tornado
                return R.drawable.soma_wx_mist;                                   // mist, haze, fog, dust, smoke, ash
            case 8:
                if (owmId == 801 || owmId == 802)
                    return isNight ? R.drawable.soma_wx_partly_night : R.drawable.soma_wx_partly_day;
                return R.drawable.soma_wx_cloudy;                                 // 803 / 804
            default: return R.drawable.soma_wx_cloudy;
        }
    }

    public String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = context.getString(R.string.weather_sunny);
            } else {
                icon = context.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = context.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = context.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = context.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = context.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = context.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = context.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }
}

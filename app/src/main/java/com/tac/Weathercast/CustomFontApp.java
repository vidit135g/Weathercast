package com.tac.Weathercast;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;


public class CustomFontApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OverrideFonts.setDefaultFont(this, "MONOSPACE", "fonts/Poppins-Regular.ttf");
        OverrideFonts.setDefaultFont(this, "SERIF", "fonts/Fraunces-SemiBold.ttf");
        OverrideFonts.setDefaultFont(this, "SANS_SERIF", "fonts/Poppins-Regular.ttf");
        OverrideFonts.setDefaultFont(this, "DEFAULT", "fonts/Poppins-Regular.ttf");
        applyNightMode(this);
    }

    /** Day/night per the Appearance setting: dark = always, a locked element = never,
     *  auto = dark between 20:00 and 06:00. */
    public static void applyNightMode(android.content.Context ctx) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String base = sp.getString("themeBase", "system");
        if ("dark".equals(sp.getString("appearance", "auto"))) base = "dark";  // legacy
        int mode;
        if ("dark".equals(base)) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if ("light".equals(base)) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            mode = (h >= 20 || h < 6) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}

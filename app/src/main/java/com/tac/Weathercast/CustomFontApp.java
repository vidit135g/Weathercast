package com.tac.Weathercast;

import android.app.Application;


public class CustomFontApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OverrideFonts.setDefaultFont(this, "MONOSPACE", "fonts/google1.ttf");
    }
}

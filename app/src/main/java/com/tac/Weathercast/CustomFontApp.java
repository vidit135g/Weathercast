package com.tac.Weathercast;

import android.app.Application;


public class CustomFontApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OverrideFonts.setDefaultFont(this, "MONOSPACE", "fonts/Poppins-Regular.ttf");
        OverrideFonts.setDefaultFont(this, "SERIF", "fonts/Fraunces-SemiBold.ttf");
        OverrideFonts.setDefaultFont(this, "SANS_SERIF", "fonts/Poppins-Regular.ttf");
        OverrideFonts.setDefaultFont(this, "DEFAULT", "fonts/Poppins-Regular.ttf");
    }
}

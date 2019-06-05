package com.tac.Weathercast.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.tac.Weathercast.R;


public class SplashActivity extends AppCompatActivity {

    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);
    }
    @Override
    protected void onResume() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        super.onResume();

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (prefs.getBoolean("firstrun", true)) {
                // Do first run stuff here then set 'firstrun' as false
                // start  DataActivity because its your app first run
                // using the following line to edit/commit prefs
                prefs.edit().putBoolean("firstrun", false).commit();
                startActivity(new Intent(this , FirstActivity.class));
                finish();
            }
            else {
                startActivity(new Intent(this , MainActivity.class));
                finish();
            }
        }, 600);


    }
}


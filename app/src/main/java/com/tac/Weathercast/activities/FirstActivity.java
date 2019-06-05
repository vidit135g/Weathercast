package com.tac.Weathercast.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.tac.Weathercast.R;


public class FirstActivity extends AppCompatActivity {

    AppCompatButton btn;
    TextView prgtext;
    ImageView piximage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);


        btn=findViewById(R.id.pixelbutton);
        prgtext=findViewById(R.id.progresstext);
        piximage=findViewById(R.id.pixelimage);
        TextView intro=findViewById(R.id.intro);
        TextView hi=findViewById(R.id.hello);

        final Animation an = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fadein);
        final Animation fade = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fade);
        final Animation fadef = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fadefirst);
        final Animation fadebut = AnimationUtils.loadAnimation(getBaseContext(),R.anim.fadebutton);
        hi.startAnimation(fadef);
        new Handler().postDelayed(() -> {
        }, 4000);
        piximage.startAnimation(an);
        new Handler().postDelayed(() -> {
        }, 2000);
        intro.startAnimation(fade);
        btn.startAnimation(fadebut);


        btn.setOnClickListener(v -> {

                Intent i = new Intent(FirstActivity.this, LoadingActivity.class);
                startActivity(i);
                finish();
        });

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
    }
}


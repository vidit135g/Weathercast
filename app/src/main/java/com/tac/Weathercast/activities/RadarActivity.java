package com.tac.Weathercast.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.tac.Weathercast.fragments.RadarFragment;

/** Full-screen radar, opened from the preview card on the Today screen.
 *  Radar is a secondary feature now — no longer in the bottom nav. */
public class RadarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        FrameLayout root = new FrameLayout(this);
        int hostId = View.generateViewId();
        root.setId(hostId);
        setContentView(root);
        if (s == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(hostId, new RadarFragment())
                    .commit();
        }
    }
}

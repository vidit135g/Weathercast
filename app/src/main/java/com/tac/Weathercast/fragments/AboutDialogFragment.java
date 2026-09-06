package com.tac.Weathercast.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;

import com.tac.Weathercast.BuildConfig;
import com.tac.Weathercast.R;


public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(R.layout.aboutlayout)
                .create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        alertDialog.show();

        TextView ver = alertDialog.findViewById(R.id.aboutVersion);
        if (ver != null) ver.setText("Version " + BuildConfig.VERSION_NAME);

        Button btn = alertDialog.findViewById(R.id.pixelbutton);
        btn.setOnClickListener(v -> alertDialog.dismiss());
        return alertDialog;
    }
}

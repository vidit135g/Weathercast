package com.tac.Weathercast.fragments;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;

import com.tac.Weathercast.R;


public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(R.layout.aboutlayout)
                .create();

        alertDialog.show();

        Button btn=alertDialog.findViewById(R.id.pixelbutton);
        btn.setOnClickListener(v -> alertDialog.dismiss());
        return alertDialog;
    }
}

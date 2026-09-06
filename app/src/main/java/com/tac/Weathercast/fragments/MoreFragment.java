package com.tac.Weathercast.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tac.Weathercast.R;
import com.tac.Weathercast.activities.MainActivity;

/** The More tab — location, settings, share, about. */
public class MoreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_more, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        MainActivity a = (MainActivity) requireActivity();
        v.findViewById(R.id.moreUpdate).setOnClickListener(x -> a.onUpdateClick());
        v.findViewById(R.id.moreSettings).setOnClickListener(x -> a.onSettingsClick());
        v.findViewById(R.id.moreShare).setOnClickListener(x -> a.onShareClick());
        v.findViewById(R.id.moreAbout).setOnClickListener(x -> a.onAboutClick());
    }
}

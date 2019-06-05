package com.tac.Weathercast.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tac.Weathercast.R;


public class ShowRoundDialogFragment extends RoundedBottomSheet {


    TextView graph, locdetect, settings, about,share,refresh;
    private CheckRefreshClickListener mCheckGraphListener;
    private CheckRefreshClickListener mCheckAutoDetectListener;
    private CheckRefreshClickListener mCheckSettingsListener;
    private CheckRefreshClickListener mCheckAboutListener;
    private CheckRefreshClickListener mCheckShareListener;
    private CheckRefreshClickListener mCheckRefresh;
    public static ShowRoundDialogFragment newInstance() {
        return new ShowRoundDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mCheckGraphListener = (CheckRefreshClickListener) context;
        mCheckSettingsListener = (CheckRefreshClickListener) context;
        mCheckAboutListener = (CheckRefreshClickListener) context;
        mCheckShareListener = (CheckRefreshClickListener) context;
        mCheckAutoDetectListener=(CheckRefreshClickListener)context;
        mCheckRefresh=(CheckRefreshClickListener)context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_show_round_dialog, container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        graph = getView().findViewById(R.id.graph);
        locdetect = getView().findViewById(R.id.locationup);
        settings = getView().findViewById(R.id.settings);
        about = getView().findViewById(R.id.about);
        share = getView().findViewById(R.id.share);
        refresh=getView().findViewById(R.id.refresh);
        graph.setOnClickListener(v -> mCheckGraphListener.onGraphClick());
        locdetect.setOnClickListener(v -> mCheckAutoDetectListener.onUpdateClick());
        settings.setOnClickListener(v -> mCheckSettingsListener.onSettingsClick());
        about.setOnClickListener(v -> mCheckAboutListener.onAboutClick());
        share.setOnClickListener(v -> mCheckShareListener.onShareClick());
        refresh.setOnClickListener(v->mCheckRefresh.onRefresh());
        super.onViewCreated(view, savedInstanceState);

    }
}


interface CheckRefreshClickListener {
    void onGraphClick();
    void onUpdateClick();
    void onShareClick();
    void onSettingsClick();
    void onAboutClick();
    void onRefresh();
}
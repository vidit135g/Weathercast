package com.tac.Weathercast.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebViewAssetLoader;

import com.tac.Weathercast.R;
import com.tac.Weathercast.activities.MainActivity;
import com.tac.Weathercast.models.Weather;

/** Radar tab — animated precipitation (RainViewer) on a dark Leaflet map.
 *  Served through WebViewAssetLoader so the page has an https origin and
 *  can fetch the RainViewer API without CORS/file:// restrictions. */
public class RadarFragment extends Fragment {

    private WebView web;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        return inflater.inflate(R.layout.fragment_radar, c, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        web = v.findViewById(R.id.radarWeb);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setAllowContentAccess(true);
        web.setBackgroundColor(0xFF12161B);
        if (0 != (requireContext().getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        web.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public boolean onConsoleMessage(android.webkit.ConsoleMessage m) {
                android.util.Log.d("RadarWeb", m.message() + " @" + m.lineNumber());
                return true;
            }
        });

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(requireContext()))
                .build();
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                return loader.shouldInterceptRequest(req.getUrl());
            }
        });

        android.content.SharedPreferences sp =
                android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        double lat = sp.getFloat("latitude", 0f);
        double lon = sp.getFloat("longitude", 0f);
        try {
            Weather w = ((MainActivity) requireActivity()).getTodayWeatherData();
            if (lat == 0 && lon == 0 && (w.getLat() != 0 || w.getLon() != 0)) {
                lat = w.getLat(); lon = w.getLon();
            }
        } catch (Exception ignored) {}

        String key = sp.getString("apiKey", getString(R.string.apiKey)).replace("\"", "").trim();
        int hr = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        int accent = com.tac.Weathercast.utils.SomaTheme.forNow(hr, 800,
                sp.getString("appearance", "auto")).heroAccent & 0x00FFFFFF;

        web.loadUrl("https://appassets.androidplatform.net/assets/radar.html?lat=" + lat + "&lon=" + lon
                + "&key=" + key + "&accent=" + String.format("%06X", accent));
    }

    @Override
    public void onDestroyView() {
        if (web != null) { web.loadUrl("about:blank"); web.destroy(); web = null; }
        super.onDestroyView();
    }
}

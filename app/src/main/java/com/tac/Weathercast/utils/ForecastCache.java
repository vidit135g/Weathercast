package com.tac.Weathercast.utils;

import com.tac.Weathercast.models.Weather;

import java.util.ArrayList;
import java.util.List;

/** A lightweight in-memory handoff of the current forecast series so secondary
 *  activities (Trend detail) can read it without a binder to MainActivity. */
public final class ForecastCache {

    private static volatile List<Weather> series = new ArrayList<>();

    private ForecastCache() {}

    public static void set(List<Weather> s) {
        series = s != null ? new ArrayList<>(s) : new ArrayList<>();
    }

    public static List<Weather> get() { return series; }
}

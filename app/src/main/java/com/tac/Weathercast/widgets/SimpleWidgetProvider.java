package com.tac.Weathercast.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.tac.Weathercast.R;

public class SimpleWidgetProvider extends AbstractWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            renderWidget(context, appWidgetManager, id, R.layout.simple_widget, WidgetRenderer.SIMPLE);
        }
        scheduleNextUpdate(context);
    }
}

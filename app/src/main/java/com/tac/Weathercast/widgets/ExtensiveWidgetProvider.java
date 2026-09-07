package com.tac.Weathercast.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;

import com.tac.Weathercast.R;

public class ExtensiveWidgetProvider extends AbstractWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            renderWidget(context, appWidgetManager, id, R.layout.extensive_widget, WidgetRenderer.EXTENSIVE);
        }
        scheduleNextUpdate(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager mgr, int id, Bundle opts) {
        renderWidget(context, mgr, id, R.layout.extensive_widget, WidgetRenderer.EXTENSIVE);
    }
}

package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.WidgetRemoteViewsService;
import barqsoft.footballscores.service.myFetchService;

/**
 * Created by Scott on 10/19/2015.
 */
public class FootballScoresWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Code gist from http://developer.android.com/guide/topics/appwidgets/index.html#MetaData
        final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];


            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            Intent  serviceIntent = new Intent(context, WidgetRemoteViewsService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            Intent activityIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_football_scores);
            rv.setOnClickPendingIntent(R.id.widget_container, pendingActivityIntent);

            rv.setRemoteAdapter(appWidgetId, R.id.widget_list, serviceIntent);
            //rv.setEmptyView(R.id.widget_list, R.id.empty_widget);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //context.startService(new Intent(context, myFetchService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, myFetchService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

    }


}

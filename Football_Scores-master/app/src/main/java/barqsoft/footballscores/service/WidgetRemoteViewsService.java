package barqsoft.footballscores.service;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ScoresProvider;
import barqsoft.footballscores.Utilies;

/**
 * Created by Scott on 10/21/2015.
 */
public class WidgetRemoteViewsService extends RemoteViewsService {

    public static final String LEAGUE_COL = "league";
    public static final String DATE_COL = "date";
    public static final String TIME_COL = "time";
    public static final String HOME_COL = "home";
    public static final String AWAY_COL = "away";
    public static final String HOME_GOALS_COL = "home_goals";
    public static final String AWAY_GOALS_COL = "away_goals";
    public static final String MATCH_ID = "match_id";
    public static final String MATCH_DAY = "match_day";

    private static final String [] WIDGET_PROJECITON = {
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY
    };


    static final int COL_ID = 0;
    static final int COL_LEAGUE = 1;
    static final int COL_DATE = 2;
    static final int COL_TIME = 3;
    static final int COL_HOME_NAME = 4;
    static final int COL_AWAY_NAME = 5;
    static final int COL_HOME_GOALS = 6;
    static final int COL_AWAY_GOALS = 7;
    static final int COL_MATCH_ID = 8;
    static final int COL_MATCH_DAY = 9;


    private static final String WIDGET_SELECTION = DatabaseContract.scores_table.DATE_COL + ">=?";

    private static final String WIDGET_SORT = DatabaseContract.scores_table.DATE_COL + " ASC";


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FootballScoresWidgetRemoteViewFactory(this.getApplicationContext(), intent);
    }

    class FootballScoresWidgetRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory{
        //Code gist taken from https://android.googlesource.com/platform/development/+/master/samples/WeatherListWidget/src/com/example/android/weatherlistwidget/WeatherWidgetService.java

        private Cursor mCursor = null;
        private Context mContext;
        private Intent mIntent;
        private int mAppWidgetID;

        public FootballScoresWidgetRemoteViewFactory(Context context, Intent intent) {
            this.mContext = context;
            this.mIntent = intent;
            this.mAppWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            //Data loaded in onDataSetChanged() which is the next call
            //Do Nothing
        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null){
                mCursor.close();
            }

            //Necessary to fix permissions issue, solution from http://stackoverflow.com/questions/13187284/android-permission-denial-in-widget-remoteviewsfactory-for-content
            final long identityToken = Binder.clearCallingIdentity();

            Date currentDate = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String [] dateSelectionArg = new String[1];
            dateSelectionArg[0] = dateFormat.format(currentDate);

            try{
                mCursor = mContext.getContentResolver().query(
                        DatabaseContract.scores_table.buildScoreWithDate(),
                        WIDGET_PROJECITON,
                        WIDGET_SELECTION,
                        dateSelectionArg,
                        WIDGET_SORT);
            }finally{
                Binder.restoreCallingIdentity(identityToken);
            }

        }


        @Override
        public void onDestroy() {
            if (mCursor != null){
                mCursor.close();
            }
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }
            RemoteViews views = getLoadingView();

            views.setTextViewText(R.id.widget_home_team_name, mCursor.getString(COL_HOME_NAME));
            views.setTextViewText(R.id.widget_home_team_score, mCursor.getInt(COL_HOME_GOALS)+"");
            views.setTextViewText(R.id.widget_game_time, mCursor.getString(COL_DATE) );
            views.setTextViewText(R.id.widget_away_team_name, mCursor.getString(COL_AWAY_NAME));
            int awayGoals = mCursor.getInt(COL_AWAY_GOALS);
            if (awayGoals > 0){
                views.setTextViewText(R.id.widget_away_team_score, "None");
            }else{
                views.setTextViewText(R.id.widget_away_team_score, mCursor.getInt(COL_AWAY_GOALS) + "");
            }


            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, mIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            return views;

        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_list_item);
        }

        @Override
        public int getViewTypeCount() {
            //Only 1 type of view
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}

package barqsoft.footballscores.widget;

/**
 * Created by shetty on 26/01/16.
 */

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.squareup.picasso.Picasso;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.ViewHolder;
import barqsoft.footballscores.utils.Utils;

@SuppressLint("NewApi")
public class WidgetDataProvider implements RemoteViewsFactory {


    private Cursor cursor;
    List mCollections = new ArrayList();
    Context mContext = null;

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    private static final String TAG = WidgetDataProvider.class.getName();
    public double detail_match_id = 0;
    public static final int COL_HOME_TEAM_ID = 10;
    public static final int COL_AWAY_TEAM_ID = 11;
    private static ComponentName COMPONENT_NAME ;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;

    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG,"getViewAt:"+position);
        cursor.moveToPosition(position);

        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                R.layout.scores_list_item);
        //ViewHolder mHolder = new ViewHolder(mView);

            //mCollections.add("ListView item " + i);
            //final ViewHolder mHolder = (ViewHolder) mView.getTag();
        mView.setTextViewText(R.id.home_name, cursor.getString(COL_HOME));
        //home_name.setText(cursor.getString(COL_HOME));
        mView.setTextViewText(R.id.away_name, cursor.getString(COL_AWAY));
        //mHolder.away_name.setText(cursor.getString(COL_AWAY));
        mView.setTextViewText(R.id.data_textview, cursor.getString(COL_MATCHTIME));
        //mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mView.setTextViewText(R.id.data_textview,cursor.getString(COL_MATCHTIME));
        mView.setTextViewText(R.id.score_textview, Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));

        //mHolder.score.setText(Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        String league_id = cursor.getString(COL_LEAGUE);
        String league_name = Utils.retrieveLeagueName(league_id,mContext);
        mView.setTextViewText(R.id.league_textview,league_name);

        String home_id = cursor.getString(COL_HOME_TEAM_ID);
        String away_id = cursor.getString(COL_AWAY_TEAM_ID);



        String home_url = Utils.retrieveCrestFromRealm(home_id,mContext);
        String away_url = Utils.retrieveCrestFromRealm(away_id,mContext);

            /*Log.d(TAG, "home_crest:" + home_url + " away_crest:" + away_url);
            if(home_url!=null) {
                if (!home_url.contains("svg")) {
                    Picasso.with(mContext).load(new File(home_url)).resize(60, 60).into(mHolder.home_crest);
                } else {
                    //loadSVGImage(mHolder.home_crest, home_url);
                    //renderSVGFile(mHolder.home_crest, home_url);
                    //if(!isScrolling) {

                        imageDownloader.download(home_url, (ImageView) mHolder.home_crest);
                    //}
                }
            }

            if(away_url !=null) {
                if (!away_url.contains("svg")) {
                    Picasso.with(mContext).load(new File(away_url)).resize(60, 60).into(mHolder.away_crest);
                } else {
                    //loadSVGImage(mHolder.away_crest, away_url);
                    //renderSVGFile(mHolder.away_crest, away_url);
                    //if(!isScrolling) {
                        imageDownloader.download(away_url, (ImageView) mHolder.away_crest);
                    //}

                }
            }
            //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
            //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));*/

        //mView.setTextViewText(android.R.id.text1, mCollections.get(position).toString());

        //mView.setTextColor(android.R.id.text1, Color.BLACK);

        return mView;
    }

    @Override
    public int getViewTypeCount() {
        Log.d(TAG,"getViewTypeCount:"+cursor.getCount());
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    private void initData() {
        //mCollections.clear();
        Log.d(TAG,"initData");
        final ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = DatabaseContract.scores_table.buildScoreWithDate(); // Get all entries
        Date date = new Date(System.currentTimeMillis());//+((i-2)*86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String []dates = new String[1];
        dates[0] = mformat.format(date);
        cursor =
                contentResolver.query(uri, null, null, dates, null);


    }

    @Override
    public void onDestroy() {
        cursor = null;
    }

}

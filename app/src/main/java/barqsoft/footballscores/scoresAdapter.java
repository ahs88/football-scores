package barqsoft.footballscores;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGExternalFileResolver;
import com.caverock.androidsvg.SVGParser;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.internal.Util;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import barqsoft.footballscores.imagedownloader.ImageDownloader;
import barqsoft.footballscores.realm.League;
import barqsoft.footballscores.realm.Team;
import barqsoft.footballscores.utils.Utils;
import io.realm.Realm;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class scoresAdapter extends CursorAdapter implements MainScreenFragment.ListViewSrollListener
{
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    private static final String TAG = scoresAdapter.class.getName();
    public double detail_match_id = 0;
    public static final int COL_HOME_TEAM_ID = 10;
    public static final int COL_AWAY_TEAM_ID = 11;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    private Context mContext;
    private DisplayImageOptions displayImageOptions;
    private ImageDownloader imageDownloader;
    private boolean isScrolling;
    private MainScreenFragment mainScreenFragment;

    public scoresAdapter(Context context,Cursor cursor,int flags,MainScreenFragment mainScreenFragment)
    {
        super(context, cursor, flags);
        mContext = context;
        this.mainScreenFragment = (MainScreenFragment)mainScreenFragment;
        mainScreenFragment.setScrollinListener(this);
        imageDownloader = new ImageDownloader(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        mHolder.home_name.setText(cursor.getString(COL_HOME));
        mHolder.away_name.setText(cursor.getString(COL_AWAY));
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utilies.getScores(cursor.getInt(COL_HOME_GOALS),cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);
        //mHolder.home_crest.setImageResource(Utilies.getTeamCrestByTeamName(
        //        cursor.getString(COL_HOME)));
        //mHolder.away_crest.setImageResource(Utilies.getTeamCrestByTeamName(
        //        cursor.getString(COL_AWAY)
        //));

        String home_id = cursor.getString(COL_HOME_TEAM_ID);
        String away_id = cursor.getString(COL_AWAY_TEAM_ID);

        String league_id = cursor.getString(COL_LEAGUE);
        String league_name = Utils.retrieveLeagueName(league_id,mContext);

        String home_url = Utils.retrieveCrestFromRealm(home_id, mContext);
        String away_url = Utils.retrieveCrestFromRealm(away_id,mContext);

       // Log.d(TAG, "home_crest:"+home_url+" away_crest:"+away_url);
        if(home_url!=null) {
            if (!home_url.contains("svg")) {
                Picasso.with(mContext).load(new File(home_url)).resize(60, 60).into(mHolder.home_crest);
            } else {
                //loadSVGImage(mHolder.home_crest, home_url);
                //renderSVGFile(mHolder.home_crest, home_url);
                if(!isScrolling) {
                    imageDownloader.download(home_url, (ImageView) mHolder.home_crest);
                }
            }
        }

        if(away_url !=null) {
            if (!away_url.contains("svg")) {
                Picasso.with(mContext).load(new File(away_url)).resize(60, 60).into(mHolder.away_crest);
            } else {
                //loadSVGImage(mHolder.away_crest, away_url);
                //renderSVGFile(mHolder.away_crest, away_url);
                if(!isScrolling) {
                    imageDownloader.download(away_url, (ImageView) mHolder.away_crest);
                }

            }
        }
        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if(mHolder.match_id == detail_match_id)
        {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilies.getMatchDay(cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(league_name);
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.home_name.getText()+" "
                    +mHolder.score.getText()+" "+mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            container.removeAllViews();
        }

    }


    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

    @Override
    public void scrolling() {
        isScrolling = true;
    }

    @Override
    public void scrollingStopped() {
        isScrolling = false;
        notifyDataSetChanged();
    }


    public class SvgDecoder implements ImageDecoder {
        ImageView imageView;
        String localUrl;
        public SvgDecoder(ImageView imageview,String localUrl){
            this.imageView = imageview;
            this.localUrl = localUrl;
        }

        @Override
        public Bitmap decode(ImageDecodingInfo imageDecodingInfo) throws IOException {
            InputStream is = imageDecodingInfo.getDownloader().getStream(
                    imageDecodingInfo.getImageUri(),   imageDecodingInfo.getExtraForDownloader());

            SVG svg = null;

            try {
                svg = SVG.getFromInputStream(new FileInputStream(new File(localUrl)));
            } /*catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/catch(Exception nfe){
                Log.d(TAG,"exception retrieving svg");
                nfe.printStackTrace();
            }

            Picture picture = svg.renderToPicture();
            PictureDrawable pictureDrawable = new PictureDrawable(picture);
            // I think the problem is in this call parameters (getWidth/getHeight)
            // but what must I write instead ?
            Bitmap image = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(),
                    pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas imageCanvas = new Canvas(image);

            svg.renderToCanvas(imageCanvas);

            return image;
        }
    }



}

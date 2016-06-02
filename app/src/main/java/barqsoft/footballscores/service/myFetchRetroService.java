package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.security.auth.callback.Callback;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.datamodel.team_data.Teams;
import barqsoft.footballscores.network.ApiError;
import barqsoft.footballscores.realm.Season;
import barqsoft.footballscores.realm.Team;
import barqsoft.footballscores.datamodel.fixture.Fixture;
import barqsoft.footballscores.datamodel.fixture.MatchFixtures;

import barqsoft.footballscores.network.FetchInterface;
import barqsoft.footballscores.network.RetroUtil;
import barqsoft.footballscores.shared.AppSharedPreference;
import barqsoft.footballscores.utils.Utils;
import io.realm.Realm;
import io.realm.RealmQuery;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class myFetchRetroService extends IntentService
{
    public static final String LOG_TAG = "myFetchService";
    private static final String TAG = myFetchRetroService.class.getName();
    private FetchInterface fmcService;
    private MatchFixtures fixtures;
    public static final String TEAM_URL = "http://api.football-data.org/v1/teams/";
    public static final String SEASON_LINK = "http://api.football-data.org/v1/soccerseasons/";
    public static final String FIXTURE_LINK = "http://api.football-data.org/v1/fixtures/";
    private static List<String> listOfPreferredLeagues;
    private HashMap<String,Fixture> fixturesMap = new HashMap<>();
    private AppSharedPreference appSharedPreference ;
    private ApiError apiError;
    private ApiError error;
    public myFetchRetroService() {
        super(TAG);
    }
    /*public myFetchRetroService()
    {
        super("myFetchService");
    }*/



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent");
        appSharedPreference = new AppSharedPreference(this);
        apiError = new ApiError();
        if (fmcService==null) {
            fmcService = RetroUtil.getInstance(true).create(FetchInterface.class);
        }
        loadPreferredListOfLeagues();

        loadFixtures("n2");
        loadFixtures("p2");

        Log.d(TAG,"apiError.getErrorCode():"+apiError.getErrorCode());
        appSharedPreference.setError(apiError.getErrorCode());
    }

    private void loadFixtures(String param) {

        MatchFixtures fixtures = requestFixtures(param);
        if(fixtures!=null) {
            loadData(fixtures);
        }
        else
        {
            //handle appropriately

        }
    }


    private MatchFixtures requestFixtures(String paramVal) {
        Log.d(LOG_TAG, "requestFixtures");
        MatchFixtures fixtures = null;
        Response<MatchFixtures> response = null;
        try {
            response = fmcService.retrieveFixtures(paramVal).execute();
            fixtures = response.body();
        } catch (IOException e) {

            Log.d(TAG," failed to create object :"+e.getMessage());
            appSharedPreference.setError(ApiError.NO_INTERNET);
            e.printStackTrace();
        }

        if(response != null && !response.isSuccess() && response.errorBody() != null){
            // handle carsResponse.errorBody()
            Log.d(TAG, "response.errorBody:" + response.errorBody() + " responseCode:" + response.code());
            loadApiError(response);
        }

        return fixtures;

    }




    public void downloadCrestImageIntoSD(String url, final String name, final String extension){

        fmcService.downloadCrestImage(url).enqueue(new retrofit.Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                try {
                    if (response.isSuccess()) {
                        InputStream inputStream = response.body().byteStream();
                        String filePath = Utils.writeIntoSD(name + extension, inputStream, getApplicationContext());
                        Realm realm = Realm.getInstance(getApplicationContext());
                        Team team = realm.where(Team.class).equalTo("id", name).findFirst();
                        realm.beginTransaction();
                        team.setLocalUrl(filePath);
                        realm.commitTransaction();
                        Log.d(TAG, "committed local url:" + filePath);
                    } else {


                        loadApiError(response);
                    }

                } catch (IOException e) {
                    Log.d(TAG, "failed to download crestImage ");
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d(TAG, "onFailure");
            }


        });
    }









    public void loadData(MatchFixtures fixtures){
        //Match data
        Log.d(LOG_TAG,"loadData fixture:"+fixtures);
        String league = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;
        String home_team_id = "";
        String away_team_id = "";
        String home_crest_url = "";
        String away_crest_url = "";
        String []dates = new String[1];

        List<Fixture> fixturesList = fixtures.getFixtures();
        Vector<ContentValues> values = new Vector <ContentValues> (fixturesList.size());


        final ContentResolver contentResolver = getApplicationContext().getContentResolver();


        Uri uri = DatabaseContract.scores_table.buildScoreWithDate(); // Get all entries
        Date date = new Date(System.currentTimeMillis());//+((i-2)*86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        dates[0] = mformat.format(date);

        Cursor c =
         contentResolver.query(uri, null, null, dates, null);



        createEntryMap(fixturesList);
        Log.d(TAG, "fixture size before processing:" + fixturesList.size());
        //remove from retrieved list if already exists in local db

        Uri uri_id = DatabaseContract.scores_table.buildScoreWithId();
        fixturesList = removeExistingEntries(c,fixturesList,contentResolver,uri_id);
        Log.d(TAG,"fixture size to write into db:"+fixturesList.size());

        for(int i=0;i<fixturesList.size();i++) {


            //Log.d(TAG,"fixture size to write into db:"+fixturesList.size());
            Fixture fixture = fixturesList.get(i);

            league = fixture.getLinks().getSoccerseason().getHref().replace(SEASON_LINK,"");
            if(listOfPreferredLeagues.contains(league)) {
                Log.d(TAG,"preferred league:"+league);
                mDate = fixture.getDate();
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0, mDate.indexOf("T"));
                SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    Date parseddate = match_date.parse(mDate + mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0, mDate.indexOf(":"));

                } catch (Exception e) {
                    Log.d(LOG_TAG, "error here!");
                    Log.e(LOG_TAG, e.getMessage());
                }
                Home = fixture.getHomeTeamName();
                Away = fixture.getAwayTeamName();
                match_id = fixture.getLinks().getSelf().getHref().replace(FIXTURE_LINK, "");
                Home_goals = String.valueOf(fixture.getResult().getGoalsHomeTeam());
                Away_goals = String.valueOf(fixture.getResult().getGoalsAwayTeam());
                match_day = String.valueOf(fixture.getMatchday());
                away_team_id = String.valueOf(fixture.getLinks().getAwayTeam().getHref().replace(TEAM_URL, ""));
                home_team_id = String.valueOf(fixture.getLinks().getHomeTeam().getHref().replace(TEAM_URL, ""));


                //insert into content values to write into db
                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                match_values.put(DatabaseContract.scores_table.AWAY_TEAM_ID, away_team_id);
                match_values.put(DatabaseContract.scores_table.HOME_TEAM_ID, home_team_id);
                match_values.put(DatabaseContract.scores_table.LEAGUE_COL, league);
                //requestSoccerSeason();

                Log.v(TAG, match_id);
                Log.v(TAG, mDate);
                Log.v(TAG, mTime);
                Log.v(TAG, Home);
                Log.v(TAG, Away);
                Log.v(TAG, Home_goals);
                Log.v(TAG, Away_goals);
                Log.v(TAG, home_crest_url);
                Log.v(TAG, away_crest_url);
                values.add(match_values);
            }
            
        }
        insertIntoDb(values);

        
        
        
    }



    private void writeTeamDetailsIntoRealm(String teamId, String crest_url, int i,int noOfTeams,String season_id){
        Realm realm = Realm.getInstance(getApplicationContext());

        Team team = realm.where(Team.class).equalTo("id", teamId).findFirst();
        if(team == null) {
            realm.beginTransaction();
            team = realm.createObject(Team.class);
            String url = team.getCrestUrl();
            team.setId(teamId);
            team.setCrestUrl(crest_url);
            realm.commitTransaction();


            //download crest url
            String ext = null;
            if(crest_url.contains("svg"))
                ext = ".svg";
            else
                ext = ".png";
            downloadCrestImageIntoSD(crest_url, teamId, ext);

            if(i == noOfTeams-1){
                realm.beginTransaction();
                Season season = realm.createObject(Season.class);
                season.setSeasonId(season_id);
                season.setDataLoaded(true);
                realm.commitTransaction();
                Log.d(TAG," loading crest completed for id :"+season_id);
            }

        }
    }



    private void createEntryMap(List<Fixture> fixturesList) {
        for(int i=0;i<fixturesList.size();i++){
            Fixture fixture = fixturesList.get(i);
            String fixtureId = fixture.getLinks().getSelf().getHref().replace(FIXTURE_LINK, "");
            fixturesMap.put(fixtureId,fixture);
        }
    }

    private List<Fixture> removeExistingEntries(Cursor c,List<Fixture> fixturesList,ContentResolver contentResolver,Uri uri) {
        Log.d(TAG,"removeExistingEntries count:"+c.getCount());
        while(c.moveToNext()){
            String match_id = c.getString(c.getColumnIndex(DatabaseContract.scores_table.MATCH_ID));
            String away_goals = c.getString(c.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));
            String home_goals = c.getString(c.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));

            Fixture fixture = fixturesMap.get(match_id);
            if(fixture!=null){

                if(away_goals.equals(String.valueOf(fixture.getResult().getGoalsAwayTeam())) && home_goals.equals(String.valueOf(fixture.getResult().getGoalsHomeTeam())) ){
                    Log.d(TAG, "removeExistingEntries to ignore insert :" + match_id);
                    fixturesList.remove(fixture);
                }
                else
                {
                    Log.d(TAG,"updating etries:"+match_id);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseContract.scores_table.HOME_GOALS_COL, home_goals);
                    contentValues.put(DatabaseContract.scores_table.AWAY_GOALS_COL, away_goals);
                    String []args = new String[]{match_id};
                    contentResolver.update(uri,contentValues,null,args);
                }

            }
        }
        return fixturesList;
    }

    private void loadPreferredListOfLeagues(){
        String arr[] = {"398","399","401","403","405"};
        listOfPreferredLeagues = Arrays.asList(arr);
        for(int i =0;i<arr.length;i++){
            requestTeamData(arr[i]);
        }

    }


    private void requestTeamData(final String id) {
            //donot request if already loaded
            if(checIfSeasonTeamsCrestLoaded(id)){
                Log.d(TAG,"already loaded crest");
                return;
            };
            //teamData = fmcService.retrieveTeamData(id).execute().body();
            fmcService.retrieveTeamData(id).enqueue(new retrofit.Callback<Teams>(){
                @Override
                public void onResponse(Response<Teams> response, Retrofit retrofit) {
                    if(response.isSuccess()) {
                        Teams teams = response.body();
                        if (teams != null) {
                            List<barqsoft.footballscores.datamodel.team_data.Team> teamList = teams.getTeams();
                            for (int i = 0; i < teamList.size(); i++) {
                                barqsoft.footballscores.datamodel.team_data.Team team = teamList.get(i);
                                String crestUrl = team.getCrestUrl();
                                String team_name = team.getName();
                                String team_id = team.getLinks().getSelf().getHref().replace(TEAM_URL, "");
                                Log.d(TAG, "name:" + team_name + " crestUrl:" + crestUrl + " team_id:" + team_id);
                                writeTeamDetailsIntoRealm(team_id, crestUrl, i, teamList.size(), id);
                            }

                        }
                    }
                    else
                    {
                        loadApiError(response);
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.d(TAG, "onFailure");
                }


            });
    }

    private boolean checIfSeasonTeamsCrestLoaded(String season_id) {
        Realm realm = Realm.getInstance(getApplicationContext());
        Season season = realm.where(Season.class).equalTo("seasonId", season_id).findFirst();
        if(season!=null){
            return season.isDataLoaded();
        }

        return false;
    }


    public void insertIntoDb(Vector<ContentValues> values){
        int inserted_data = 0;
        ContentValues[] insert_data = new ContentValues[values.size()];
        values.toArray(insert_data);
        inserted_data = getApplicationContext().getContentResolver().bulkInsert(
                DatabaseContract.BASE_CONTENT_URI,insert_data);
    }



    private void loadApiError(Response response) {
        try {
            Log.d(TAG, response.errorBody().string() + " response error code:" + response.code());
            apiError.setErrorCode(response.code());
            apiError.setMessage(response.errorBody().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}


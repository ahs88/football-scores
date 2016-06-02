package barqsoft.footballscores.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import barqsoft.footballscores.realm.League;
import barqsoft.footballscores.realm.Team;
import io.realm.Realm;

/**
 * Created by shetty on 06/02/16.
 */
public class Utils {

    public static String writeIntoSD(String fileName,InputStream in,Context context){
        File fileUrl = null;
        try {
            File root = context.getExternalFilesDir(null);
            fileUrl = new File(root, fileName);
            FileOutputStream f = new FileOutputStream(fileUrl);
            byte[] buffer = new byte[in.available()];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
        } catch (Exception e) {
            Log.d("writeIntoSD failed", e.getMessage());
            return null;
        }

        return fileUrl.getAbsolutePath();

    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String retrieveLeagueName(String league_id,Context mContext) {
        Realm realm = Realm.getInstance(mContext);
        League league = realm.where(League.class).equalTo("id",league_id).findFirst();
        return league.getLeagueName();
    }


    public static String retrieveCrestFromRealm(String team_id,Context mContext) {
        Realm realm = Realm.getInstance(mContext);
        Team team1 = realm.where(Team.class).equalTo("id", team_id).findFirst();
        if(team1!=null){
            return team1.getLocalUrl();
        }
        else{
            return null;
        }
    }

    public static String retrievePreferredLeaguesString(Context mContext) {
        Realm realm = Realm.getInstance(mContext);
        List<League> leagues = realm.where(League.class).findAll();
        String preferredleagues = "";
        for (int i =0; i< leagues.size();i++){
            League league = leagues.get(i);
            if(leagues.size()!=0 && i == leagues.size()-1){
                preferredleagues += "and "+ league.getLeagueName();
            }
            else if(leagues.size()!=1 && i == leagues.size()-2) {
                preferredleagues += league.getLeagueName()+" ";
            }
            else {
                preferredleagues += league.getLeagueName() + ",";
            }
        }
        return preferredleagues;
    }


    public static void overwriteSVGFileAsRealmLocalUrl(String localUrl,String newLocalUrl,Context mContext){
        Log.d("Utils","localUrl:"+localUrl+" newlocalUrl:"+newLocalUrl);
        Realm realm = Realm.getInstance(mContext);
        Team team = realm.where(Team.class).equalTo("localUrl",localUrl).findFirst();
        if(team!=null){
            realm.beginTransaction();
            team.setLocalUrl(newLocalUrl);
            realm.copyToRealmOrUpdate(team);
            realm.commitTransaction();
        }
    }




}

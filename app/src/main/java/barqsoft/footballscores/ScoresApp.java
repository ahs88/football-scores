package barqsoft.footballscores;

import android.app.Application;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import barqsoft.footballscores.realm.League;
import io.realm.Realm;

/**
 * Created by shetty on 08/02/16.
 */
public class ScoresApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        loadLeagues();
    }

    private void loadLeagues() {

      //  String arr[] = {"394","396","398","399","401","404"};
        HashMap<String,String> hashMap = new HashMap();
        hashMap.put("398",getApplicationContext().getString(R.string.premierleague));
        hashMap.put("399",getApplicationContext().getString(R.string.primeradivison));
        hashMap.put("401",getApplicationContext().getString(R.string.seriaa));
        hashMap.put("403",getApplicationContext().getString(R.string.bundesliga));
        hashMap.put("405",getApplicationContext().getString(R.string.champions_league));

        createRealmEntry(hashMap);



    }

    private void createRealmEntry(HashMap<String,String> hashMap) {
        for(Map.Entry<String,String> entry : hashMap.entrySet()){
            Log.d("APP", "entry key:" + entry.getKey() + " value:" + entry.getValue());
            Realm realm = Realm.getInstance(this);
            realm.beginTransaction();
            League league = realm.where(League.class).equalTo("id",entry.getKey()).findFirst();
            if(league == null) {
                league = realm.createObject(League.class);
                league.setId(entry.getKey());
                league.setLeagueName(entry.getValue());

            }
            realm.commitTransaction();
        }

    }
}

package barqsoft.footballscores.shared;

import android.content.Context;
import android.content.SharedPreferences;

import barqsoft.footballscores.network.ApiError;

/**
 * Created by shetty on 15/02/16.
 */
public class AppSharedPreference {

    private static final String USER_PREFERENCE = "USER_PREFERENCE";
    public static final String ERROR_CODE = "ERROR_CODE";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    Context mContext;
    public AppSharedPreference(Context context){
        mContext = context;
        sharedPreferences  = mContext.getSharedPreferences(USER_PREFERENCE,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public SharedPreferences getSharedPrefence(){
        return sharedPreferences;
    }


    public void setError(int n){
        editor.putInt(ERROR_CODE,n);
        editor.commit();
    }

    public int getError(){
        if(sharedPreferences.contains(ERROR_CODE)){
            return sharedPreferences.getInt(ERROR_CODE,0);
        }
        return 0;
    }

}

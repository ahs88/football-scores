package barqsoft.footballscores.network;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;


/**
 * Created by akshath on 11/30/2015.
 */
public class RetroUtil implements HttpRequestInterceptor{

    private static final String BASE_URL = "http://api.football-data.org";
    public static Retrofit retrofit;
    public static boolean currentStatus;

    public static Retrofit getInstance(boolean converter) {

        if(retrofit == null || currentStatus != converter) {
            OkHttpClient client = new OkHttpClient();
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.interceptors().add(interceptor);
            if(converter) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            else
            {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            currentStatus = converter;
        }
        return retrofit;

    }

    public static String retrieveValueFromJson(String info,String valueTag) {
        String data="";
        try {
            JSONObject jsonObject = new JSONObject(info);
            data = (String)jsonObject.get(valueTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;

    }

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {

    }
}

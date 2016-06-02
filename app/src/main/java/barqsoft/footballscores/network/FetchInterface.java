package barqsoft.footballscores.network;

import com.squareup.okhttp.ResponseBody;

import java.lang.ref.Reference;

import barqsoft.footballscores.datamodel.fixture.MatchFixtures;


import barqsoft.footballscores.datamodel.team_data.Teams;
import retrofit.Call;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Url;

/**
 * Created by akshath on 11/30/2015.
 */
public interface FetchInterface {
    public static final String API_KEY = "819cb458b6824745bfde77b90bb27cdc";
    @Headers("X-Auth-Token:"+API_KEY)
    @GET("v1/fixtures")
    Call<MatchFixtures> retrieveFixtures(@Query("timeFrame") String timeFrame);

    @Headers("X-Auth-Token:"+API_KEY)
    @GET("v1/soccerseasons/{id}/teams")
    Call<Teams> retrieveTeamData(@Path(value = "id") String path);

    @GET
    Call<ResponseBody> downloadCrestImage(@Url String url);


/*

    @FormUrlEncoded
    @POST("android/availablejobs.php")
    Call<List<RelevantAllJobs>> retrieveAllJobs(@Field("empid") String name);

    @FormUrlEncoded
    @POST("android/viewjob.php")
    Call<JobDescription> retrieveJobDescription(@Field("empid") String empid, @Field("eventid") String eventid);

   // http://flashbackmemo.in/android/acceptedjob.php
    @FormUrlEncoded
    @POST("android/acceptedjob.php")
    Call<List<info.androidhive.slidingmenu.jobs.datamodel.AcceptedJobs>> retrieveAcceptedJobs(@Field("empid") String empid);


    @FormUrlEncoded
    @POST("android/acceptjob.php")
    Call<ResponseBody> acceptJob(@Field("empid") String emp_id, @Field("eventid") String eventId, @Field("splreqans") String splreqans);
*/

}



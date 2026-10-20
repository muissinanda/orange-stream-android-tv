package com.orangestream.tv.api;

import com.google.gson.JsonObject;
import com.orangestream.tv.model.SportsMatch;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SportsApiService {

    @GET("api/streamed/matches/all")
    Call<List<SportsMatch>> getAllMatches();

    @GET("api/v1/sports/streams")
    Call<JsonObject> getMatchStreams(@Query("source") String source, @Query("id") String id);

    @GET("api/v1/sports/resolve")
    Call<JsonObject> resolveStreamM3u8(
            @Query("source") String source,
            @Query("id") String id,
            @Query("stream") String streamNo,
            @Query("url") String embedUrl
    );
}

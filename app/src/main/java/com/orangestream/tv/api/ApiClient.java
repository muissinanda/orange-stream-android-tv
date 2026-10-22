package com.orangestream.tv.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    public static final String TMDB_API_KEY = "df42d80eb5e5a88706ab6e992ba050dd";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String SPORTS_BASE_URL = "https://film.boyblade.my.id/";

    private static Retrofit tmdbRetrofit = null;
    private static Retrofit sportsRetrofit = null;

    private static OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();
    }

    public static TmdBApiService getTmdBService() {
        if (tmdbRetrofit == null) {
            tmdbRetrofit = new Retrofit.Builder()
                    .baseUrl(TMDB_BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return tmdbRetrofit.create(TmdBApiService.class);
    }

    public static SportsApiService getSportsService() {
        if (sportsRetrofit == null) {
            sportsRetrofit = new Retrofit.Builder()
                    .baseUrl(SPORTS_BASE_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return sportsRetrofit.create(SportsApiService.class);
    }
}

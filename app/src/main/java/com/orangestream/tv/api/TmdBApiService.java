package com.orangestream.tv.api;

import com.orangestream.tv.model.Movie;
import com.orangestream.tv.model.MovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdBApiService {

    @GET("trending/all/day")
    Call<MovieResponse> getTrending(@Query("api_key") String apiKey, @Query("language") String lang);

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("language") String lang);

    @GET("tv/popular")
    Call<MovieResponse> getPopularSeries(@Query("api_key") String apiKey, @Query("language") String lang);

    @GET("discover/movie")
    Call<MovieResponse> getMoviesByGenre(
            @Query("api_key") String apiKey,
            @Query("with_genres") String genreId,
            @Query("language") String lang,
            @Query("sort_by") String sortBy
    );

    @GET("discover/tv")
    Call<MovieResponse> getTvByGenre(
            @Query("api_key") String apiKey,
            @Query("with_genres") String genreId,
            @Query("language") String lang,
            @Query("sort_by") String sortBy
    );

    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(@Path("movie_id") long movieId, @Query("api_key") String apiKey, @Query("language") String lang);

    @GET("tv/{tv_id}")
    Call<Movie> getTvDetails(@Path("tv_id") long tvId, @Query("api_key") String apiKey, @Query("language") String lang);
}

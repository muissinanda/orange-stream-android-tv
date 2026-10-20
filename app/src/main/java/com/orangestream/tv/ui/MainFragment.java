package com.orangestream.tv.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.orangestream.tv.R;
import com.orangestream.tv.api.ApiClient;
import com.orangestream.tv.model.Movie;
import com.orangestream.tv.model.MovieResponse;
import com.orangestream.tv.model.SportsMatch;
import com.orangestream.tv.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends BrowseSupportFragment {

    private ArrayObjectAdapter mRowsAdapter;
    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mBackgroundRunnable;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prepareBackgroundManager();
        setupUIElements();
        loadRows();
        setupEventListeners();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = ContextCompat.getDrawable(getActivity(), R.drawable.banner);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setTitle("ORANGE STREAM TV");
        setBadgeDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.logo));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.primary_orange));
        setSearchAffordanceColor(Color.parseColor("#F97316"));
    }

    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        // Row 0: Trending
        ArrayObjectAdapter trendingAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerTrending = new HeaderItem(0, "🔥 Sedang Tren (Trending Hari Ini)");
        mRowsAdapter.add(new ListRow(headerTrending, trendingAdapter));
        fetchTrending(trendingAdapter);

        // Row 1: Live Sports
        ArrayObjectAdapter sportsAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerSports = new HeaderItem(1, "⚽ Siaran Langsung Olahraga (Live Sports)");
        mRowsAdapter.add(new ListRow(headerSports, sportsAdapter));
        fetchLiveSports(sportsAdapter);

        // Row 2: 24/7 Saluran Olahraga
        ArrayObjectAdapter channelsAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerChannels = new HeaderItem(2, "⚡ Saluran TV Olahraga 24/7 Non-Stop");
        mRowsAdapter.add(new ListRow(headerChannels, channelsAdapter));
        load247SportsChannels(channelsAdapter);

        // Row 3: Popular Movies
        ArrayObjectAdapter moviesAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerMovies = new HeaderItem(3, "🎬 Film Bioskop Terpopuler");
        mRowsAdapter.add(new ListRow(headerMovies, moviesAdapter));
        fetchPopularMovies(moviesAdapter);

        // Row 4: Popular Series
        ArrayObjectAdapter seriesAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerSeries = new HeaderItem(4, "📺 Serial TV Pilihan");
        mRowsAdapter.add(new ListRow(headerSeries, seriesAdapter));
        fetchPopularSeries(seriesAdapter);

        // Row 5: Action Movies
        ArrayObjectAdapter actionAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerAction = new HeaderItem(5, "💥 Film Aksi Blockbuster");
        mRowsAdapter.add(new ListRow(headerAction, actionAdapter));
        fetchMoviesByGenre("28", actionAdapter);

        // Row 6: Anime
        ArrayObjectAdapter animeAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerAnime = new HeaderItem(6, "🎌 Anime & Animasi Populer");
        mRowsAdapter.add(new ListRow(headerAnime, animeAdapter));
        fetchMoviesByGenre("16", animeAdapter);

        // Row 7: Korean Drama
        ArrayObjectAdapter kdramaAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerKdrama = new HeaderItem(7, "🇰🇷 Drama Korea (K-Drama)");
        mRowsAdapter.add(new ListRow(headerKdrama, kdramaAdapter));
        fetchKdrama(kdramaAdapter);

        setAdapter(mRowsAdapter);
    }

    private void fetchTrending(ArrayObjectAdapter adapter) {
        ApiClient.getTmdBService().getTrending(ApiClient.TMDB_API_KEY, "id-ID").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Movie m : response.body().getResults()) {
                        adapter.add(m);
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void fetchPopularMovies(ArrayObjectAdapter adapter) {
        ApiClient.getTmdBService().getPopularMovies(ApiClient.TMDB_API_KEY, "id-ID").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Movie m : response.body().getResults()) {
                        m.setSport(false);
                        adapter.add(m);
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void fetchPopularSeries(ArrayObjectAdapter adapter) {
        ApiClient.getTmdBService().getPopularSeries(ApiClient.TMDB_API_KEY, "id-ID").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Movie m : response.body().getResults()) {
                        m.setSport(false);
                        adapter.add(m);
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void fetchMoviesByGenre(String genreId, ArrayObjectAdapter adapter) {
        ApiClient.getTmdBService().getMoviesByGenre(ApiClient.TMDB_API_KEY, genreId, "id-ID", "popularity.desc").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Movie m : response.body().getResults()) {
                        adapter.add(m);
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void fetchKdrama(ArrayObjectAdapter adapter) {
        ApiClient.getTmdBService().getTvByGenre(ApiClient.TMDB_API_KEY, "18", "id-ID", "popularity.desc").enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Movie m : response.body().getResults()) {
                        adapter.add(m);
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void fetchLiveSports(ArrayObjectAdapter adapter) {
        ApiClient.getSportsService().getAllMatches().enqueue(new Callback<List<SportsMatch>>() {
            @Override
            public void onResponse(Call<List<SportsMatch>> call, Response<List<SportsMatch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (SportsMatch match : response.body()) {
                        Movie sportItem = new Movie();
                        sportItem.setId(match.getId().hashCode());
                        sportItem.setSport(true);
                        sportItem.setCategory(match.getCategory());
                        sportItem.setStreamUrl("https://embed.st/embed/" + (match.getCategory() != null ? match.getCategory() : "live") + "/" + match.getId() + "/1");
                        adapter.add(sportItem);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<SportsMatch>> call, Throwable t) {}
        });
    }

    private void load247SportsChannels(ArrayObjectAdapter adapter) {
        String[][] channels = {
                {"Sky Sports Golf 24/7", "golf", "https://embed.st/embed/golf/skygolf/1", "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?w=800"},
                {"Tennis Channel HD", "tennis", "https://embed.st/embed/tennis/tennisch/1", "https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800"},
                {"Rally TV Worldwide", "motor-sports", "https://embed.st/embed/motor-sports/rallytv/1", "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800"},
                {"NHL Network HD", "hockey", "https://embed.st/embed/hockey/nhlnet/1", "https://images.unsplash.com/photo-1580748141549-71748dbe0bdc?w=800"},
                {"NFL Network Live", "american-football", "https://embed.st/embed/american-football/nflnet/1", "https://images.unsplash.com/photo-1587280501635-68a0e82cd5ff?w=800"},
                {"Fox League 502", "rugby", "https://embed.st/embed/rugby/fox502/1", "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800"},
                {"Willow Cricket HD", "cricket", "https://embed.st/embed/cricket/willow/1", "https://images.unsplash.com/photo-1531415074868-036b1c5d53ec?w=800"}
        };

        for (String[] ch : channels) {
            Movie chItem = new Movie();
            chItem.setId(ch[0].hashCode());
            chItem.setSport(true);
            chItem.setCategory(ch[1]);
            chItem.setStreamUrl(ch[2]);
            adapter.add(chItem);
        }
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                if (movie.isSport() || movie.getStreamUrl() != null) {
                    // Direct Play for Sports
                    Intent intent = new Intent(getActivity(), PlaybackActivity.class);
                    intent.putExtra("movie", movie);
                    startActivity(intent);
                } else {
                    // Open Details for Movies / Series
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra("movie", movie);
                    startActivity(intent);
                }
            }
        });

        setOnItemViewSelectedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (item instanceof Movie) {
                updateBackgroundDebounced(((Movie) item).getBackdropUrl());
            }
        });
    }

    private void updateBackgroundDebounced(String uri) {
        if (mBackgroundRunnable != null) {
            mHandler.removeCallbacks(mBackgroundRunnable);
        }
        mBackgroundRunnable = () -> updateBackground(uri);
        mHandler.postDelayed(mBackgroundRunnable, 300);
    }

    private void updateBackground(String uri) {
        if (getActivity() == null) return;

        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .into(new CustomTarget<Drawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        mBackgroundManager.setDrawable(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundRunnable != null) {
            mHandler.removeCallbacks(mBackgroundRunnable);
        }
    }
}

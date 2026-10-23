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

        // Row 0: Live Sports Matches
        ArrayObjectAdapter sportsAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerSports = new HeaderItem(0, "🔴 Siaran Langsung & Pertandingan Hari Ini");
        mRowsAdapter.add(new ListRow(headerSports, sportsAdapter));

        // Row 1: 24/7 Saluran TV Olahraga
        ArrayObjectAdapter channelsAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerChannels = new HeaderItem(1, "⚡ Saluran TV Olahraga 24/7 Non-Stop");
        mRowsAdapter.add(new ListRow(headerChannels, channelsAdapter));

        // Row 2: Football
        ArrayObjectAdapter footballAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerFootball = new HeaderItem(2, "⚽ Sepak Bola (Football)");
        mRowsAdapter.add(new ListRow(headerFootball, footballAdapter));

        // Row 3: Basketball
        ArrayObjectAdapter basketballAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerBasketball = new HeaderItem(3, "🏀 Basket (NBA & Liga Dunia)");
        mRowsAdapter.add(new ListRow(headerBasketball, basketballAdapter));

        // Row 4: Motorsport
        ArrayObjectAdapter motorAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerMotor = new HeaderItem(4, "🏎️ Motor Sports (MotoGP / Formula 1)");
        mRowsAdapter.add(new ListRow(headerMotor, motorAdapter));

        // Row 5: Combat Sports
        ArrayObjectAdapter fightAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerFight = new HeaderItem(5, "🥊 Pertarungan (UFC, Tinju & MMA)");
        mRowsAdapter.add(new ListRow(headerFight, fightAdapter));

        // Row 6: Tennis & Other Sports
        ArrayObjectAdapter otherAdapter = new ArrayObjectAdapter(cardPresenter);
        HeaderItem headerOther = new HeaderItem(6, "🎾 Tenis & Olahraga Pilihan Lainnya");
        mRowsAdapter.add(new ListRow(headerOther, otherAdapter));

        fetchLiveSportsAndChannels(sportsAdapter, channelsAdapter, footballAdapter, basketballAdapter, motorAdapter, fightAdapter, otherAdapter);

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
        ApiClient.getTmdBService().getTvByLanguage(ApiClient.TMDB_API_KEY, "ko", "id-ID", "popularity.desc").enqueue(new Callback<MovieResponse>() {
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

    private void fetchLiveSportsAndChannels(
            ArrayObjectAdapter sportsAdapter,
            ArrayObjectAdapter channelsAdapter,
            ArrayObjectAdapter footballAdapter,
            ArrayObjectAdapter basketballAdapter,
            ArrayObjectAdapter motorAdapter,
            ArrayObjectAdapter fightAdapter,
            ArrayObjectAdapter otherAdapter
    ) {
        ApiClient.getSportsService().getAllMatches().enqueue(new Callback<List<SportsMatch>>() {
            @Override
            public void onResponse(Call<List<SportsMatch>> call, Response<List<SportsMatch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (SportsMatch match : response.body()) {
                        Movie sportItem = new Movie();
                        sportItem.setId(match.getId() != null ? match.getId().hashCode() : System.currentTimeMillis());
                        sportItem.setTitle(match.getTitle() != null ? match.getTitle() : "Siaran Olahraga");
                        sportItem.setSport(true);
                        sportItem.setCategory(match.getCategory());
                        sportItem.setPosterPath(match.getPoster());
                        sportItem.setBackdropPath(match.getPoster());
                        sportItem.setMatchDate(match.getDate());

                        String streamUrl = "https://embed.st/embed/" + (match.getCategory() != null ? match.getCategory() : "live") + "/" + match.getId() + "/1";
                        if (match.getSources() != null && !match.getSources().isEmpty()) {
                            SportsMatch.SourceItem src = match.getSources().get(0);
                            streamUrl = "https://embed.st/embed/" + src.getSource() + "/" + src.getId() + "/1";
                        }
                        sportItem.setStreamUrl(streamUrl);

                        String cat = match.getCategory() != null ? match.getCategory().toLowerCase() : "";

                        if (match.getDate() <= 0) {
                            channelsAdapter.add(sportItem);
                        } else {
                            sportsAdapter.add(sportItem);
                        }

                        if (cat.contains("football") || cat.contains("soccer")) {
                            footballAdapter.add(sportItem);
                        } else if (cat.contains("basketball")) {
                            basketballAdapter.add(sportItem);
                        } else if (cat.contains("motor") || cat.contains("f1") || cat.contains("nascar") || cat.contains("rally")) {
                            motorAdapter.add(sportItem);
                        } else if (cat.contains("fight") || cat.contains("ufc") || cat.contains("boxing") || cat.contains("mma")) {
                            fightAdapter.add(sportItem);
                        } else {
                            otherAdapter.add(sportItem);
                        }
                    }
                }
                
                // Fallback 24/7 channels if list was empty
                if (channelsAdapter.size() == 0) {
                    loadFallback247Channels(channelsAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<SportsMatch>> call, Throwable t) {
                loadFallback247Channels(channelsAdapter);
            }
        });
    }

    private void loadFallback247Channels(ArrayObjectAdapter adapter) {
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
            chItem.setTitle(ch[0]);
            chItem.setSport(true);
            chItem.setCategory(ch[1]);
            chItem.setStreamUrl(ch[2]);
            chItem.setPosterPath(ch[3]);
            chItem.setBackdropPath(ch[3]);
            chItem.setMatchDate(0);
            chItem.setCustomSubtitle("⚡ Saluran TV 24/7 Non-Stop");
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

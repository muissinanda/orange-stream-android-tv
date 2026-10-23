package com.orangestream.tv.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.DetailsSupportFragment;
import androidx.leanback.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.orangestream.tv.R;
import com.orangestream.tv.model.Movie;
import com.orangestream.tv.presenter.DetailsDescriptionPresenter;

public class VideoDetailsFragment extends DetailsSupportFragment {

    private static final int ACTION_PLAY_VIDLINK = 1;
    private static final int ACTION_PLAY_AUTOEMBED = 2;
    private static final int ACTION_PLAY_MULTIEMBED = 3;
    private static final int ACTION_PLAY_TWOEMBED = 4;
    private static final int ACTION_PLAY_VIDSRC = 5;

    private Movie mSelectedMovie;
    private ArrayObjectAdapter mAdapter;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedMovie = (Movie) getActivity().getIntent().getSerializableExtra("movie");
        if (mSelectedMovie == null) {
            getActivity().finish();
            return;
        }

        prepareBackgroundManager();
        setupDetailsOverviewRow();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        Glide.with(getActivity())
                .load(mSelectedMovie.getBackdropUrl())
                .centerCrop()
                .into(new CustomTarget<Drawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        mBackgroundManager.setDrawable(resource);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void setupDetailsOverviewRow() {
        FullWidthDetailsOverviewRowPresenter detailsPresenter = new FullWidthDetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.bg_dark));
        detailsPresenter.setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_FULL);

        detailsPresenter.setOnActionClickedListener(action -> {
            String server = "vidlink";
            if (action.getId() == ACTION_PLAY_AUTOEMBED) server = "autoembed";
            else if (action.getId() == ACTION_PLAY_MULTIEMBED) server = "multiembed";
            else if (action.getId() == ACTION_PLAY_TWOEMBED) server = "twoembed";
            else if (action.getId() == ACTION_PLAY_VIDSRC) server = "vidsrc";

            Intent intent = new Intent(getActivity(), PlaybackActivity.class);
            intent.putExtra("movie", mSelectedMovie);
            intent.putExtra("server", server);
            startActivity(intent);
        });

        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);

        mAdapter = new ArrayObjectAdapter(presenterSelector);
        DetailsOverviewRow detailsRow = new DetailsOverviewRow(mSelectedMovie);

        // Action Buttons (Remote D-Pad Friendly)
        SparseArrayObjectAdapter actionAdapter = new SparseArrayObjectAdapter();
        actionAdapter.set(ACTION_PLAY_VIDLINK, new Action(ACTION_PLAY_VIDLINK, "▶ PUTAR SEKARANG", "VidLink Pro (Super HD)"));
        actionAdapter.set(ACTION_PLAY_AUTOEMBED, new Action(ACTION_PLAY_AUTOEMBED, "SERVER 2", "AutoEmbed Fast"));
        actionAdapter.set(ACTION_PLAY_MULTIEMBED, new Action(ACTION_PLAY_MULTIEMBED, "SERVER 3", "MultiEmbed Global"));
        actionAdapter.set(ACTION_PLAY_TWOEMBED, new Action(ACTION_PLAY_TWOEMBED, "SERVER 4", "2Embed"));
        actionAdapter.set(ACTION_PLAY_VIDSRC, new Action(ACTION_PLAY_VIDSRC, "SERVER 5", "VidSrc Premium"));
        detailsRow.setActionsAdapter(actionAdapter);

        // Load Poster
        Glide.with(getActivity())
                .asBitmap()
                .load(mSelectedMovie.getPosterUrl())
                .into(new CustomTarget<Bitmap>(280, 420) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        detailsRow.setImageBitmap(getActivity(), resource);
                        mAdapter.notifyArrayItemRangeChanged(0, 1);
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });

        mAdapter.add(detailsRow);
        setAdapter(mAdapter);
    }
}

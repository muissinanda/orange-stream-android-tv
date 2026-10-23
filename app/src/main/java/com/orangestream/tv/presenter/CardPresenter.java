package com.orangestream.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orangestream.tv.R;
import com.orangestream.tv.model.Movie;

public class CardPresenter extends Presenter {

    private static final int POSTER_WIDTH_DP = 160;
    private static final int POSTER_HEIGHT_DP = 240;
    private static final int SPORTS_WIDTH_DP = 280;
    private static final int SPORTS_HEIGHT_DP = 158;

    private int sSelectedBackgroundColor = -1;
    private int sDefaultBackgroundColor = -1;

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        sDefaultBackgroundColor = ContextCompat.getColor(context, R.color.bg_dark);
        sSelectedBackgroundColor = ContextCompat.getColor(context, R.color.primary_orange);

        ImageCardView cardView = new ImageCardView(context) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        view.setBackgroundColor(color);
        view.setInfoAreaBackgroundColor(selected ? Color.parseColor("#1F1812") : Color.parseColor("#12141C"));
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (!(item instanceof Movie)) return;
        Movie movie = (Movie) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        Context context = cardView.getContext();

        cardView.setTitleText(movie.getDisplayTitle());
        if (movie.isSport()) {
            cardView.setContentText(movie.getSportTimeFormatted());
        } else {
            cardView.setContentText(movie.getRatingString() + " • " + movie.getYear());
        }

        int width = dpToPx(context, movie.isSport() ? SPORTS_WIDTH_DP : POSTER_WIDTH_DP);
        int height = dpToPx(context, movie.isSport() ? SPORTS_HEIGHT_DP : POSTER_HEIGHT_DP);
        cardView.setMainImageDimensions(width, height);

        String imageUrl = movie.isSport() ? movie.getBackdropUrl() : movie.getPosterUrl();

        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.banner)
                .error(R.drawable.banner)
                .into(cardView.getMainImageView());
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}

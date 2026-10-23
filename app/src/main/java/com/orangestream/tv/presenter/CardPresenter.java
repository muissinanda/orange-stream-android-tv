package com.orangestream.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
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

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();

        ImageCardView cardView = new ImageCardView(context) {
            @Override
            public void setSelected(boolean selected) {
                updateCardVisualState(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dpToPx(context, 12));
                }
            });
            cardView.setClipToOutline(true);
        }

        updateCardVisualState(cardView, false);
        return new ViewHolder(cardView);
    }

    private void updateCardVisualState(ImageCardView cardView, boolean selected) {
        Context ctx = cardView.getContext();
        int cornerRadius = dpToPx(ctx, 12);

        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setCornerRadius(cornerRadius);

        if (selected) {
            borderDrawable.setColor(Color.parseColor("#1C1610"));
            borderDrawable.setStroke(dpToPx(ctx, 3), ContextCompat.getColor(ctx, R.color.primary_orange));
            cardView.setInfoAreaBackgroundColor(Color.parseColor("#261C13"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardView.setElevation(dpToPx(ctx, 10));
                cardView.setTranslationZ(dpToPx(ctx, 4));
            }
        } else {
            borderDrawable.setColor(Color.parseColor("#0E121B"));
            borderDrawable.setStroke(dpToPx(ctx, 1), Color.parseColor("#1B2232"));
            cardView.setInfoAreaBackgroundColor(Color.parseColor("#0F1420"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardView.setElevation(dpToPx(ctx, 2));
                cardView.setTranslationZ(0);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cardView.setBackground(borderDrawable);
        } else {
            cardView.setBackgroundDrawable(borderDrawable);
        }
    }

    private CharSequence formatSportSubtitle(String raw) {
        if (raw == null) return "";
        SpannableStringBuilder ssb = new SpannableStringBuilder(raw);

        if (raw.startsWith("🔴 SEDANG LIVE")) {
            int end = raw.indexOf("•");
            if (end == -1) end = Math.min(raw.length(), 14);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FF4D4D")), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < raw.length()) {
                ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#E2E8F0")), end, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else if (raw.contains("24/7")) {
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#F59E0B")), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (raw.startsWith("Hari ini")) {
            int end = raw.indexOf(",");
            if (end == -1) end = Math.min(raw.length(), 8);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#10B981")), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < raw.length()) {
                ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#E2E8F0")), end, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else if (raw.startsWith("Besok")) {
            int end = raw.indexOf(",");
            if (end == -1) end = Math.min(raw.length(), 5);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < raw.length()) {
                ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#E2E8F0")), end, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#CBD5E1")), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (!(item instanceof Movie)) return;
        Movie movie = (Movie) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        Context context = cardView.getContext();

        cardView.setTitleText(movie.getDisplayTitle());
        if (movie.isSport()) {
            cardView.setContentText(formatSportSubtitle(movie.getSportTimeFormatted()));
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
package com.orangestream.tv.presenter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.leanback.widget.Presenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orangestream.tv.R;
import com.orangestream.tv.model.Movie;

public class CardPresenter extends Presenter {

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static class SportViewHolder extends Presenter.ViewHolder {
        public final View cardRoot;
        public final CardView cardContainer;
        public final ImageView ivPoster;
        public final TextView tvLiveBadge;
        public final TextView tvCategoryBadge;
        public final View infoArea;
        public final TextView tvTitle;
        public final TextView tvSubtitle;
        public final View focusBorder;

        public SportViewHolder(View view) {
            super(view);
            cardRoot = view.findViewById(R.id.cardRoot);
            cardContainer = view.findViewById(R.id.cardContainer);
            ivPoster = view.findViewById(R.id.ivPoster);
            tvLiveBadge = view.findViewById(R.id.tvLiveBadge);
            tvCategoryBadge = view.findViewById(R.id.tvCategoryBadge);
            infoArea = view.findViewById(R.id.infoArea);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvSubtitle = view.findViewById(R.id.tvSubtitle);
            focusBorder = view.findViewById(R.id.focusBorder);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_sport_card, parent, false);
        SportViewHolder vh = new SportViewHolder(view);

        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                vh.focusBorder.setVisibility(View.VISIBLE);
                vh.cardContainer.setCardElevation(dpToPx(context, 12));
                vh.infoArea.setBackgroundColor(Color.parseColor("#201812"));
                vh.cardRoot.animate().scaleX(1.05f).scaleY(1.05f).setDuration(160).start();
                vh.tvTitle.setSelected(true);
            } else {
                vh.focusBorder.setVisibility(View.GONE);
                vh.cardContainer.setCardElevation(dpToPx(context, 3));
                vh.infoArea.setBackgroundColor(Color.parseColor("#121724"));
                vh.cardRoot.animate().scaleX(1.0f).scaleY(1.0f).setDuration(160).start();
                vh.tvTitle.setSelected(false);
            }
        });

        return vh;
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
                ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#F1F5F9")), end, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    private String formatCategory(String cat) {
        if (cat == null || cat.trim().isEmpty()) return "SPORTS";
        String lower = cat.toLowerCase();
        if (lower.contains("football") || lower.contains("soccer")) return "⚽ FOOTBALL";
        if (lower.contains("basketball")) return "🏀 NBA";
        if (lower.contains("motor") || lower.contains("f1") || lower.contains("motogp")) return "🏎️ MOTOR";
        if (lower.contains("fight") || lower.contains("ufc") || lower.contains("boxing")) return "🥊 COMBAT";
        if (lower.contains("tennis")) return "🎾 TENNIS";
        if (lower.contains("hockey")) return "🏒 HOCKEY";
        if (lower.contains("baseball")) return "⚾ BASEBALL";
        if (lower.contains("golf")) return "⛳ GOLF";
        if (lower.contains("cricket")) return "🏏 CRICKET";
        if (lower.contains("rugby")) return "🏉 RUGBY";
        return cat.toUpperCase();
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (!(item instanceof Movie) || !(viewHolder instanceof SportViewHolder)) return;
        Movie movie = (Movie) item;
        SportViewHolder vh = (SportViewHolder) viewHolder;
        Context context = vh.view.getContext();

        vh.tvTitle.setText(movie.getDisplayTitle());

        String subtitleText = movie.isSport() ? movie.getSportTimeFormatted() : (movie.getRatingString() + " • " + movie.getYear());
        vh.tvSubtitle.setText(formatSportSubtitle(subtitleText));

        // Live badge
        boolean isLive = subtitleText != null && (subtitleText.contains("SEDANG LIVE") || subtitleText.contains("LIVE"));
        vh.tvLiveBadge.setVisibility(isLive ? View.VISIBLE : View.GONE);

        // Category badge
        vh.tvCategoryBadge.setText(formatCategory(movie.getCategory()));

        // Poster image
        String imageUrl = movie.isSport() ? movie.getBackdropUrl() : movie.getPosterUrl();

        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.bg_sport_placeholder)
                .error(R.drawable.bg_sport_placeholder)
                .into(vh.ivPoster);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        if (viewHolder instanceof SportViewHolder) {
            SportViewHolder vh = (SportViewHolder) viewHolder;
            Glide.with(vh.view.getContext()).clear(vh.ivPoster);
        }
    }
}
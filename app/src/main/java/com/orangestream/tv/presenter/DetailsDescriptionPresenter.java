package com.orangestream.tv.presenter;

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;
import com.orangestream.tv.model.Movie;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        if (!(item instanceof Movie)) return;
        Movie movie = (Movie) item;

        viewHolder.getTitle().setText(movie.getDisplayTitle());
        viewHolder.getSubtitle().setText("⭐ " + movie.getRatingString() + "  |  " + movie.getYear() + "  |  " + (movie.isSport() ? "LIVE SPORTS HD" : movie.getMediaType().toUpperCase()));
        viewHolder.getBody().setText(movie.getOverview());
    }
}

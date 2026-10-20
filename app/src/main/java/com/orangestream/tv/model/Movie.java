package com.orangestream.tv.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Movie implements Serializable {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("name")
    private String name;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("first_air_date")
    private String firstAirDate;

    @SerializedName("media_type")
    private String mediaType;

    // Custom fields for Sports and direct streams
    private boolean isSport = false;
    private String streamUrl;
    private String category;

    public Movie() {}

    public Movie(long id, String title, String overview, String posterPath, String backdropPath, double rating, String date, String mediaType) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.voteAverage = rating;
        this.releaseDate = date;
        this.mediaType = mediaType;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDisplayTitle() {
        if (title != null && !title.trim().isEmpty()) return title;
        if (name != null && !name.trim().isEmpty()) return name;
        return "Tayangan";
    }

    public String getOverview() {
        if (overview != null && !overview.trim().isEmpty()) return overview;
        return "Tidak ada sinopsis tersedia untuk tayangan ini.";
    }

    public String getPosterUrl() {
        if (posterPath == null || posterPath.trim().isEmpty()) {
            return "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80";
        }
        if (posterPath.startsWith("http")) return posterPath;
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    public String getBackdropUrl() {
        if (backdropPath == null || backdropPath.trim().isEmpty()) {
            return getPosterUrl();
        }
        if (backdropPath.startsWith("http")) return backdropPath;
        return "https://image.tmdb.org/t/p/original" + backdropPath;
    }

    public String getYear() {
        if (releaseDate != null && releaseDate.length() >= 4) return releaseDate.substring(0, 4);
        if (firstAirDate != null && firstAirDate.length() >= 4) return firstAirDate.substring(0, 4);
        return "2024";
    }

    public String getRatingString() {
        if (isSport) return "⚡ LIVE";
        if (voteAverage > 0) return String.format("%.1f", voteAverage);
        return "7.5";
    }

    public String getMediaType() {
        if (mediaType != null) return mediaType;
        return name != null ? "tv" : "movie";
    }

    public boolean isSport() { return isSport; }
    public void setSport(boolean sport) { isSport = sport; }

    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

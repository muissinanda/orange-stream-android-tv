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
    private long matchDate = 0;
    private String customSubtitle = null;

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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getFirstAirDate() { return firstAirDate; }
    public void setFirstAirDate(String firstAirDate) { this.firstAirDate = firstAirDate; }

    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

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
        if (posterPath.startsWith("http://") || posterPath.startsWith("https://")) {
            return posterPath;
        }
        if (posterPath.startsWith("/api/images/")) {
            return "https://film.boyblade.my.id/api/streamed/images/" + posterPath.substring(12);
        }
        if (posterPath.startsWith("/api/streamed/")) {
            return "https://film.boyblade.my.id" + posterPath;
        }
        if (posterPath.startsWith("/")) {
            return "https://image.tmdb.org/t/p/w500" + posterPath;
        }
        return "https://image.tmdb.org/t/p/w500/" + posterPath;
    }

    public String getBackdropUrl() {
        if (backdropPath == null || backdropPath.trim().isEmpty()) {
            return getPosterUrl();
        }
        if (backdropPath.startsWith("http://") || backdropPath.startsWith("https://")) {
            return backdropPath;
        }
        if (backdropPath.startsWith("/api/images/")) {
            return "https://film.boyblade.my.id/api/streamed/images/" + backdropPath.substring(12);
        }
        if (backdropPath.startsWith("/api/streamed/")) {
            return "https://film.boyblade.my.id" + backdropPath;
        }
        if (backdropPath.startsWith("/")) {
            return "https://image.tmdb.org/t/p/original" + backdropPath;
        }
        return "https://image.tmdb.org/t/p/original/" + backdropPath;
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

    public long getMatchDate() { return matchDate; }
    public void setMatchDate(long matchDate) { this.matchDate = matchDate; }

    public String getCustomSubtitle() { return customSubtitle; }
    public void setCustomSubtitle(String customSubtitle) { this.customSubtitle = customSubtitle; }

    public String getSportTimeFormatted() {
        if (customSubtitle != null && !customSubtitle.trim().isEmpty()) {
            return customSubtitle;
        }
        if (matchDate <= 0) {
            return "⚡ Saluran TV 24/7";
        }

        long now = System.currentTimeMillis();
        java.util.Date matchD = new java.util.Date(matchDate);
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        String timeStr = timeFormat.format(matchD);

        // Check if currently live: from 15 minutes before scheduled kickoff until 3.5 hours (210 mins) after
        if (now >= matchDate - (15 * 60 * 1000L) && now <= matchDate + (210 * 60 * 1000L)) {
            return "🔴 SEDANG LIVE • " + timeStr + " WIB";
        }

        // If finished
        if (now > matchDate + (210 * 60 * 1000L)) {
            java.text.SimpleDateFormat endedFormat = new java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault());
            return "Selesai • " + endedFormat.format(matchD) + " WIB";
        }

        // Check if Today or Tomorrow in local device calendar
        java.util.Calendar calNow = java.util.Calendar.getInstance();
        java.util.Calendar calMatch = java.util.Calendar.getInstance();
        calMatch.setTime(matchD);

        int dayDiff = calMatch.get(java.util.Calendar.DAY_OF_YEAR) - calNow.get(java.util.Calendar.DAY_OF_YEAR);
        int yearDiff = calMatch.get(java.util.Calendar.YEAR) - calNow.get(java.util.Calendar.YEAR);

        if (yearDiff == 0 && dayDiff == 0) {
            return "Hari ini, " + timeStr + " WIB";
        } else if (yearDiff == 0 && dayDiff == 1) {
            return "Besok, " + timeStr + " WIB";
        } else {
            java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault());
            return dayFormat.format(matchD) + " WIB";
        }
    }
}

package com.orangestream.tv.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class SportsMatch implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("category")
    private String category;

    @SerializedName("date")
    private long date;

    @SerializedName("poster")
    private String poster;

    @SerializedName("sources")
    private List<SourceItem> sources;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public long getDate() { return date; }
    public String getPoster() { return poster; }
    public List<SourceItem> getSources() { return sources; }

    public static class SourceItem implements Serializable {
        @SerializedName("source")
        private String source;

        @SerializedName("id")
        private String id;

        public String getSource() { return source; }
        public String getId() { return id; }
    }
}

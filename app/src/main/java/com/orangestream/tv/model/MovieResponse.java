package com.orangestream.tv.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieResponse {

    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<Movie> results;

    public int getPage() { return page; }
    public List<Movie> getResults() { return results; }
}

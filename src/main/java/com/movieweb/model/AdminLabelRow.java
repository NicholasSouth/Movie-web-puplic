package com.movieweb.model;

/* One genre or tag in the admin Genres and Tags page, with how many live movies use it. */
public class AdminLabelRow {
    private int id;
    private String name;
    private int movieCount;

    public AdminLabelRow() {
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getMovieCount() {
        return movieCount;
    }
    public void setMovieCount(int movieCount) {
        this.movieCount = movieCount;
    }
}

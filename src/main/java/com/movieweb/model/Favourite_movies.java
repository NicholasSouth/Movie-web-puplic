package com.movieweb.model;

public class Favourite_movies {
	private int user_id;
    private int movie_id;
    private String added_at;
    //
    public Favourite_movies() {
    }
    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    public int getMovie_id() {
        return movie_id;
    }
    public void setMovie_id(int movie_id) {
        this.movie_id = movie_id;
    }
    public String getAdded_at() {
        return added_at;
    }
    public void setAdded_at(String added_at) {
        this.added_at = added_at;
    }
}

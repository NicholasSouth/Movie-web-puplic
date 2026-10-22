package com.movieweb.model;

import java.sql.Timestamp;

public class Ratings {
    private int rating_id;
    private int user_id;
    private int movie_id;
    private int rating;
    private Timestamp created_at;

    //
    public Ratings() {
    }
    
    public Ratings(
            int rating_id,
            int user_id,
            int movie_id,
            int rating,
            Timestamp created_at) {
        this.rating_id = rating_id;
        this.user_id = user_id;
        this.movie_id = movie_id;
        this.rating = rating;
        this.created_at = created_at;
    }
    public int getRatingId() {
        return rating_id;
    }
    public void setRatingId(int rating_id) {
        this.rating_id = rating_id;
    }
    public int getUserId() {
        return user_id;
    }
    public void setUserId(int user_id) {
        this.user_id = user_id;
    }
    public int getMovieId() {
        return movie_id;
    }
    public void setMovieId(int movie_id) {
        this.movie_id = movie_id;
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public Timestamp getCreatedAt() {
        return created_at;
    }
    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }
}
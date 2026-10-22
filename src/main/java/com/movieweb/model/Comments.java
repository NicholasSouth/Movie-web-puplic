package com.movieweb.model;

import java.sql.Timestamp;

public class Comments {
    private int comment_id;
    private int user_id;
    private int movie_id;
    private String comment_text;
    private Timestamp created_at;

    //
    public Comments() {
    }

    public Comments(
            int comment_id,
            int user_id,
            int movie_id,
            String comment_text,
            Timestamp created_at) {
        this.comment_id = comment_id;
        this.user_id = user_id;
        this.movie_id = movie_id;
        this.comment_text = comment_text;
        this.created_at = created_at;
    }
    public int getCommentId() {
        return comment_id;
    }
    public void setCommentId(int comment_id) {
        this.comment_id = comment_id;
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
    public String getCommentText() {
        return comment_text;
    }
    public void setCommentText(String comment_text) {
        this.comment_text = comment_text;
    }
    public Timestamp getCreatedAt() {
        return created_at;
    }
    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }
}
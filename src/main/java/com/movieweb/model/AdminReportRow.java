package com.movieweb.model;

import java.sql.Timestamp;

/* A reported comment, with its report totals. */
public class AdminReportRow {
    private int commentId;
    private String commentText;
    private Timestamp commentCreatedAt;
    private int movieId;
    private String movieName;
    private int authorId;
    private String authorUsername;
    private boolean authorActive;
    private boolean authorDeleted;
    private int reportCount;
    private Timestamp firstReportedAt;
    private Timestamp lastReportedAt;

    public AdminReportRow() {
    }

    public int getCommentId() {
        return commentId;
    }
    public void setCommentId(int v) {
        this.commentId = v;
    }

    public String getCommentText() {
        return commentText;
    }
    public void setCommentText(String v) {
        this.commentText = v;
    }

    public Timestamp getCommentCreatedAt() {
        return commentCreatedAt;
    }
    public void setCommentCreatedAt(Timestamp v) {
        this.commentCreatedAt = v;
    }

    public int getMovieId() {
        return movieId;
    }
    public void setMovieId(int v) {
        this.movieId = v;
    }

    public String getMovieName() {
        return movieName;
    }
    public void setMovieName(String v) {
        this.movieName = v;
    }

    public int getAuthorId() {
        return authorId;
    }
    public void setAuthorId(int v) {
        this.authorId = v;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }
    public void setAuthorUsername(String v) {
        this.authorUsername = v;
    }

    public boolean isAuthorActive() {
        return authorActive;
    }
    public void setAuthorActive(boolean v) {
        this.authorActive = v;
    }

    public boolean isAuthorDeleted() {
        return authorDeleted;
    }
    public void setAuthorDeleted(boolean v) {
        this.authorDeleted = v;
    }

    public int getReportCount() {
        return reportCount;
    }
    public void setReportCount(int v) {
        this.reportCount = v;
    }

    public Timestamp getFirstReportedAt() {
        return firstReportedAt;
    }
    public void setFirstReportedAt(Timestamp v) {
        this.firstReportedAt = v;
    }

    public Timestamp getLastReportedAt() {
        return lastReportedAt;
    }
    public void setLastReportedAt(Timestamp v) {
        this.lastReportedAt = v;
    }
}
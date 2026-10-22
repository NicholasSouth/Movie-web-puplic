package com.movieweb.model;

import java.sql.Timestamp;

/* One movie in the admin movie list (filled by AdminMoviesDAO.list, shown by admin_movies.jsp). */
public class AdminMovieRow {
    private int movieId;
    private String name;
    private String ageRating;
    private int durationMinute;
    private Timestamp availableFrom;
    private Timestamp availableUntil; // null = always available
    private String posterPath;
    private boolean active;

    public AdminMovieRow() {
    }

    public int getMovieId() {
        return movieId;
    }
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAgeRating() {
        return ageRating;
    }
    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public int getDurationMinute() {
        return durationMinute;
    }
    public void setDurationMinute(int durationMinute) {
        this.durationMinute = durationMinute;
    }

    public Timestamp getAvailableFrom() {
        return availableFrom;
    }
    public void setAvailableFrom(Timestamp availableFrom) {
        this.availableFrom = availableFrom;
    }

    public Timestamp getAvailableUntil() {
        return availableUntil;
    }
    public void setAvailableUntil(Timestamp availableUntil) {
        this.availableUntil = availableUntil;
    }

    public String getPosterPath() {
        return posterPath;
    }
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    /* Same rules as the customer pages (MoviesDAO): NOW SHOWING, COMING SOON, ENDED, or HIDDEN when switched off. */
    public String getState() {
        if (!active) {
            return "HIDDEN";
        }
        long now = System.currentTimeMillis();
        if (availableFrom != null && availableFrom.getTime() > now) {
            return "COMING SOON";
        }
        if (availableUntil != null && availableUntil.getTime() + 24L * 60L * 60L * 1000L <= now) {
            return "ENDED";
        }
        return "NOW SHOWING";
    }
}

package com.movieweb.model;

import java.time.LocalDate;

/** What a manager typed into the "Request a movie" form. Only the name is always present. */
public class MovieRequestData {
    private String name;
    private LocalDate releaseDate;      // optional
    private Integer durationMinutes;    // optional
    private String ageRating;           // optional: G, PG, PG-13, R, NC-17
    private String genres;              // optional
    private String directors;           // optional
    private String authors;             // optional
    private String actors;              // optional
    private String synopsis;            // optional
    private String trailerLink;         // optional
    private String posterLink;          // optional
    private String note;                // optional, message to the administrator

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public String getDirectors() { return directors; }
    public void setDirectors(String directors) { this.directors = directors; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }
    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
    public String getTrailerLink() { return trailerLink; }
    public void setTrailerLink(String trailerLink) { this.trailerLink = trailerLink; }
    public String getPosterLink() { return posterLink; }
    public void setPosterLink(String posterLink) { this.posterLink = posterLink; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

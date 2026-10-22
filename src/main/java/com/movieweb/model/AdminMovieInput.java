package com.movieweb.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/* Everything the admin types in the Add/Edit Movie form, already validated by AdminMoviesServlet.
 * Handed to AdminMoviesDAO.create / update, which write the movie and its cast, genres and tags in one
 * transaction. AdminMoviesDAO.getById also fills one of these to prefill the edit form.
 */
public class AdminMovieInput {
    public int movieId;             // 0 when adding; the movie being edited otherwise
    public String name;
    public String description;      // may be empty
    public String ageRating;        // may be empty
    public int durationMinute;
    public Timestamp availableFrom;
    public Timestamp availableUntil; // null = always available
    public String posterPath;
    public String trailerLink;      // may be empty
    public boolean active;
    public List<Integer> genreIds = new ArrayList<>();
    public List<Integer> tagIds = new ArrayList<>();
    public List<String> actors = new ArrayList<>();
    public List<String> directors = new ArrayList<>();
    public List<String> authors = new ArrayList<>();
}

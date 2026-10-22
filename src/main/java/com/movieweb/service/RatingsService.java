package com.movieweb.service;

import java.sql.SQLException;
import java.util.Objects;

import com.movieweb.DAO.RatingsDAO;
import com.movieweb.model.Ratings;

public class RatingsService {
    private final RatingsDAO ratingsDAO;
    public RatingsService() {
        this(new RatingsDAO());
    }
    public RatingsService(RatingsDAO ratingsDAO) {
        this.ratingsDAO = Objects.requireNonNull(ratingsDAO);
    }

    /* Returns the user's rating for a movie. */
    public Ratings getRating(int user_id, int movie_id)
            throws SQLException {
        requirePositive(user_id, "User ID");
        requirePositive(movie_id, "Movie ID");
        return ratingsDAO.getRating(user_id, movie_id);
    }

    /* Adds a new rating or updates the user's existing rating. */
    public boolean setRating(int user_id, int movie_id, int rating)
            throws SQLException {
        requirePositive(user_id, "User ID");
        requirePositive(movie_id, "Movie ID");
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 1 and 10.");
        }
        Ratings existingRating = ratingsDAO.getRating(user_id, movie_id);
        boolean success;
        if (existingRating == null) {
            Ratings newRating = new Ratings();
            newRating.setUserId(user_id);
            newRating.setMovieId(movie_id);
            newRating.setRating(rating);
            success = ratingsDAO.addRating(newRating);
        } 
        else {
            success = ratingsDAO.updateRating(
                    user_id,
                    movie_id,
                    rating
            );
        }
        if (!success) {
            return false;
        }
        updateMovieAverageRating(movie_id);
        return true;
    }

    /* Recalculates and updates Movies.avg_rating. */
    public boolean updateMovieAverageRating(int movie_id)
            throws SQLException {
        requirePositive(movie_id, "Movie ID");
        double avg_rating = ratingsDAO.getAverageRating(movie_id);
        return ratingsDAO.updateMovieAverageRating(movie_id, avg_rating);
    }
    private static void requirePositive(int value, String label) {
        if (value < 1) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
    }
}
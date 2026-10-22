package com.movieweb.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.movieweb.DAO.Movie_actorsDAO;
import com.movieweb.DAO.Movie_directorsDAO;
import com.movieweb.DAO.Movie_authorsDAO;

import com.movieweb.model.Actors;
import com.movieweb.model.Directors;
import com.movieweb.model.Authors;

public class MovieCastService {
    private final Movie_actorsDAO movieActorsDAO;
    private final Movie_directorsDAO movieDirectorsDAO;
    private final Movie_authorsDAO movieAuthorsDAO;
    public MovieCastService() {
        this(
            new Movie_actorsDAO(),
            new Movie_directorsDAO(),
            new Movie_authorsDAO()
        );
    }
    public MovieCastService(
            Movie_actorsDAO movieActorsDAO,
            Movie_directorsDAO movieDirectorsDAO,
            Movie_authorsDAO movieAuthorsDAO) {
        this.movieActorsDAO = Objects.requireNonNull(movieActorsDAO);
        this.movieDirectorsDAO = Objects.requireNonNull(movieDirectorsDAO);
        this.movieAuthorsDAO = Objects.requireNonNull(movieAuthorsDAO);
    }
    public List<Actors> getActorsByMovieId(int movieId)
            throws SQLException {
        requirePositive(movieId, "Movie ID");
        return movieActorsDAO.getActorsByMovieId(movieId);
    }
    public List<Directors> getDirectorsByMovieId(int movieId)
            throws SQLException {
        requirePositive(movieId, "Movie ID");
        return movieDirectorsDAO.getDirectorsByMovieId(movieId);
    }
    public List<Authors> getAuthorsByMovieId(int movieId)
            throws SQLException {
        requirePositive(movieId, "Movie ID");
        return movieAuthorsDAO.getAuthorsByMovieId(movieId);
    }
    private static void requirePositive(int value, String label) {
        if (value < 1) {
            throw new IllegalArgumentException(
                label + " must be positive."
            );
        }
    }
}
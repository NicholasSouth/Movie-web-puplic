package com.movieweb.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.movieweb.DAO.MoviesDAO;
import com.movieweb.model.Movies;

/*Business entry point shared by catalogue, details, and favourite controllers.*/
public class MoviesService {
    private final MoviesDAO moviesDAO;
    public MoviesService() {
        this(new MoviesDAO());
    }
    public MoviesService(MoviesDAO moviesDAO) {
        this.moviesDAO = Objects.requireNonNull(moviesDAO);
    }

    /*Returns filtered and paginated movies.*/
    public List<Movies> getCatalog(Movies filter) throws SQLException {
        return moviesDAO.searchAndFilterMovies(Objects.requireNonNull(filter));
    }

    /*Returns movies currently showing.*/
    public List<Movies> getNowShowingMovies() throws SQLException {
        return moviesDAO.getNowShowingMovies();
    }

    /*Returns upcoming movies.*/
    public List<Movies> getComingSoonMovies() throws SQLException {
        return moviesDAO.getComingSoonMovies();
    }

    /*Returns popular movies.*/
    public List<Movies> getPopularMovies() throws SQLException {
        return moviesDAO.getPopularMovies();
    }

    /*Returns movie details.*/
    public Movies getDetails(int movieId, Integer userId)
            throws SQLException {
        requirePositive(movieId, "Movie ID");
        if (userId != null) {
            requirePositive(userId, "User ID");
        }
        return moviesDAO.getMovieById(movieId);
    }

    /*Handles adding or removing a movie from user favorites.*/
    public boolean setFavourite(int userId, int movieId, boolean add)
            throws SQLException {
        requirePositive(userId, "User ID");
        requirePositive(movieId, "Movie ID");
        return moviesDAO.setFavourite(userId, movieId, add);
    }

    private static void requirePositive(int value, String label) {
        if (value < 1) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
    }
}
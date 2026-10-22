package com.movieweb.service;

import java.util.List;

import com.movieweb.DAO.Favourite_moviesDAO;
import com.movieweb.model.Movies;

public class FavouriteMoviesService 
{
    private Favourite_moviesDAO favouriteMoviesDAO;

    public FavouriteMoviesService()
    {
        favouriteMoviesDAO = new Favourite_moviesDAO();
    }
    public List<Movies> getFavouriteMovies(
            int userId)
    {
        return favouriteMoviesDAO.getFavouriteMovies(
                userId);
    }
    public boolean addFavourite(
            int userId,
            int movieId)
    {
        if (favouriteMoviesDAO.isFavourite(
                userId,
                movieId))
        {
            return false;
        }
        return favouriteMoviesDAO.addFavourite(
                userId,
                movieId);
    }
    public boolean removeFavourite(
            int userId,
            int movieId)
    {
        return favouriteMoviesDAO.removeFavourite(
                userId,
                movieId);
    }
    public boolean isFavourite(
            int userId,
            int movieId)
    {
        return favouriteMoviesDAO.isFavourite(
                userId,
                movieId);
    }
}
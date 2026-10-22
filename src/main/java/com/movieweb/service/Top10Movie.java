package com.movieweb.service;

import java.util.List;

import com.movieweb.DAO.MoviesDAO;
import com.movieweb.model.Movies;

public class Top10Movie {
    private MoviesDAO moviesDAO;
    public Top10Movie() {
        moviesDAO = new MoviesDAO();
    }
    public List<Movies> getTop10Movies() {
        return moviesDAO.getTop10Movies();
    }
}
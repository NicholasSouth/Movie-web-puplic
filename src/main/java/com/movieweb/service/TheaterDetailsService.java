package com.movieweb.service;

import java.util.List;

import com.movieweb.DAO.MoviesDAO;
import com.movieweb.DAO.RoomsDAO;
import com.movieweb.DAO.TheatersDAO;
import com.movieweb.model.Movies;
import com.movieweb.model.Rooms;
import com.movieweb.model.Theaters;

import com.movieweb.DAO.ShowtimesDAO;
import com.movieweb.model.Showtimes;
import java.time.LocalDate;

public class TheaterDetailsService {
    private TheatersDAO theatersDAO;
    private RoomsDAO roomsDAO;
    private MoviesDAO moviesDAO;
    private ShowtimesDAO showtimesDAO;
    public TheaterDetailsService() {
        theatersDAO = new TheatersDAO();
        roomsDAO = new RoomsDAO();
        moviesDAO = new MoviesDAO();
        showtimesDAO = new ShowtimesDAO();
    }
    public Theaters getTheaterById(int theaterId) {
        return theatersDAO.getTheaterById(theaterId);
    }
    public List<Rooms> getRoomsByTheaterId(int theaterId) {
        return roomsDAO.getRoomsByTheaterId(theaterId);
    }
    public List<Showtimes> getShowtimesByTheaterAndDate(int theaterId, LocalDate date) {
        return showtimesDAO.getShowtimesByTheaterAndDate(theaterId, date);
    }
    public List<Movies> getMoviesByIds(List<Integer> movieIds) {
        return moviesDAO.getMoviesByIds(movieIds);
    }
}
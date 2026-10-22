package com.movieweb.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.model.Movies;
import com.movieweb.model.Rooms;
import com.movieweb.model.Showtimes;
import com.movieweb.model.Theaters;
import com.movieweb.service.TheaterDetailsService;

@WebServlet("/theater-details")
public class TheaterDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TheaterDetailsService theaterDetailsService;
    public TheaterDetailsServlet() {
        this(new TheaterDetailsService());
    }
    public TheaterDetailsServlet(TheaterDetailsService theaterDetailsService) {
        this.theaterDetailsService = Objects.requireNonNull(theaterDetailsService);
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {
        String idParameter = req.getParameter("id");

        // No ID was provided
        if (idParameter == null || idParameter.trim().isEmpty()) {
            req.setAttribute("theater", null);
            req.getRequestDispatcher("/theater_details.jsp").forward(req, resp);
            return;
        }
        int theaterId;
        try {
            theaterId = Integer.parseInt(idParameter);
        }
        catch (NumberFormatException e) {
            req.setAttribute("theater", null);
            req.getRequestDispatcher("/theater_details.jsp").forward(req, resp);
            return;
        }

        // Get theater
        Theaters theater = theaterDetailsService.getTheaterById(theaterId);

        // Theater does not exist
        if (theater == null) {
            req.setAttribute("theater", null);
            req.getRequestDispatcher("/theater_details.jsp").forward(req, resp);
            return;
        }

        // Get rooms belonging to this theater
        List<Rooms> rooms = theaterDetailsService.getRoomsByTheaterId(theaterId);

        /* Selected date */
        LocalDate today = LocalDate.now();
        String dateParameter = req.getParameter("date");
        LocalDate selectedDate;
        if (dateParameter == null || dateParameter.trim().isEmpty()) {
            // No date selected -> show today
            selectedDate = today;
        }
        else {
            try {
                // Expected format: yyyy-MM-dd
                selectedDate = LocalDate.parse(dateParameter);
            }
            catch (Exception e) {
                // Invalid date -> show today
                selectedDate = today;
            }
        }

        /* Only allow today through today + 6 */
        LocalDate lastDate = today.plusDays(6);
        if (selectedDate.isBefore(today)
                || selectedDate.isAfter(lastDate)) {
            selectedDate = today;
        }

        /* Showtimes for selected date */
        List<Showtimes> scheduleShowtimes =
                theaterDetailsService.getShowtimesByTheaterAndDate(
                        theaterId,
                        selectedDate
                );

        /* A movie can have multiple showtimes.
         * Example: Showtimes:
         * movie 11
         * movie 12
         * movie 11
         * movie 14       
         * We only need:
         * 11, 12, 14 */

        Set<Integer> movieIdSet = new LinkedHashSet<>();
        for (Showtimes showtime : scheduleShowtimes) {
            movieIdSet.add(showtime.getMovie_id());
        }
        List<Integer> movieIds = new ArrayList<>(movieIdSet);

        /*Get the actual Movies objects*/
        List<Movies> scheduleMovieList = theaterDetailsService.getMoviesByIds(movieIds);

        Map<Integer, Movies> scheduleMovies = new HashMap<>();
        for (Movies movie : scheduleMovieList) {
            scheduleMovies.put(movie.getMovie_id(), movie);
        }
        
        /* Find today and the next 7 days */
        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDates.add(today.plusDays(i));
        }
        
        req.setAttribute("theater", theater);
        req.setAttribute("rooms", rooms);

        // Schedule
        req.setAttribute("scheduleShowtimes", scheduleShowtimes);
        req.setAttribute("scheduleMovies", scheduleMovies);

        // Date selector
        req.setAttribute("today", today);
        req.setAttribute("selectedDate", selectedDate);
        req.setAttribute("weekDates", weekDates);

        // Display page
        req.getRequestDispatcher("/theater_details.jsp").forward(req, resp);
    }
}
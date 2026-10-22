package com.movieweb.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.MoviesDAO;
import com.movieweb.DAO.ShowtimesDAO;
import com.movieweb.model.Movies;
import com.movieweb.model.Showtimes;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

@WebServlet("/booking-showtimes")
public class BookingShowtimesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        String contextPath = request.getContextPath();
        //Require logged in
        HttpSession session = request.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null
            || "GUEST".equalsIgnoreCase(currentUser.getRole())) {
            String loginUrl = request.getContextPath()
                    + "/log_in.jsp?redirect="
                    + java.net.URLEncoder.encode(
                            request.getRequestURI()
                            + (request.getQueryString() == null
                                ? ""
                                : "?" + request.getQueryString()),
                            java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(loginUrl);
            return;
        }
        
        // 1. Validate movie_id.
        int movieId;
        try {
            movieId = Integer.parseInt(
                request.getParameter("movie_id"));
        } 
        catch (NumberFormatException | NullPointerException e) {
            response.sendRedirect(contextPath + "/main.jsp");
            return;
        }

        // 2. Load movie.
        Movies movie = moviesDAO.getMovieById(movieId);
        if (movie == null) {
            response.sendRedirect(contextPath + "/main.jsp");
            return;
        }
        request.setAttribute("movie", movie);

     // 3. Read and validate the selected date.
        LocalDate today = LocalDate.now();
        LocalDate selectedDate = today;
        String dateParameter = request.getParameter("date");
        if (dateParameter != null && !dateParameter.trim().isEmpty()) {
            try {
                LocalDate requestedDate = LocalDate.parse(dateParameter);

                // Only allow today through the next 6 days.
                if (!requestedDate.isBefore(today)
                        && !requestedDate.isAfter(today.plusDays(6))) {
                    selectedDate = requestedDate;
                }
            } 
            catch (DateTimeParseException e) {
                // Invalid dates fall back to today.
                selectedDate = today;
            }
        }
        request.setAttribute("selectedDate", selectedDate.toString());

        // 4. Load theaters through ShowtimesDAO.
        List<Theaters> theaters = showtimesDAO.getTheatersByMovieId(movieId);
        request.setAttribute("theaters", theaters);

        // 5. Validate the selected theater.
        int selectedTheaterId = 0;
        String theaterParameter = request.getParameter("theater_id");
        if (theaterParameter != null
                && !theaterParameter.trim().isEmpty()) {
            try {
                selectedTheaterId = Integer.parseInt(theaterParameter);
            } 
            catch (NumberFormatException e) {
                selectedTheaterId = 0;
            }
        }
        boolean validTheater = false;
        for (Theaters theater : theaters) {
            if (theater.getTheater_id() == selectedTheaterId) {
                validTheater = true;
                break;
            }
        }

        // Default to the first theater if none was selected.
        if (!validTheater && !theaters.isEmpty()) {
            selectedTheaterId = theaters.get(0).getTheater_id();
        } 
        else if (theaters.isEmpty()) {
            selectedTheaterId = 0;
        }
        request.setAttribute("selectedTheaterId", selectedTheaterId);

        // 6. Load showtimes through ShowtimesDAO.
        List<Showtimes> showtimes = new ArrayList<>();
        if (selectedTheaterId > 0) {
            showtimes = showtimesDAO.getShowtimesByMovieTheaterAndDate(movieId, selectedTheaterId, selectedDate);
        }
        request.setAttribute("showtimes", showtimes);

        // 7. Forward to JSP.
        request.getRequestDispatcher("/booking_showtimes.jsp").forward(request, response);
    }
}
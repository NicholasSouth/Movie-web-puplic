package com.movieweb.servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.DAO.MoviesDAO;
import com.movieweb.DAO.RoomsDAO;
import com.movieweb.DAO.ShowtimesDAO;
import com.movieweb.model.Movies;
import com.movieweb.model.Rooms;
import com.movieweb.model.Showtimes;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

@WebServlet("/manager-showtimes")
public class ManagerShowtimesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ManagersDAO managersDAO = new ManagersDAO();
    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getCurrentManager(request, response);
        if (currentUser == null) {
            return;
        }
        Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
        List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
        LocalDate selectedDate = LocalDate.now();
        String dateParam = request.getParameter("date");
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            try {
                selectedDate = LocalDate.parse(dateParam);
            } catch (RuntimeException e) {
                request.setAttribute("errorMessage", "Invalid date. Today's date is displayed instead.");
            }
        }
        request.setAttribute("selectedDate", selectedDate.toString());
        HttpSession session = request.getSession();
        String error = (String) session.getAttribute("showtimesError");
        String success = (String) session.getAttribute("showtimesSuccess");
        if (error != null) {
            request.setAttribute("errorMessage", error);
            session.removeAttribute("showtimesError");
        }
        if (success != null) {
            request.setAttribute("successMessage", success);
            session.removeAttribute("showtimesSuccess");
        }
        if (currentTheaterId == null) {
            request.setAttribute("rooms", new ArrayList<Rooms>());
            request.setAttribute("movies", new ArrayList<Movies>());
            request.setAttribute("showtimes", new ArrayList<Showtimes>());
            request.getRequestDispatcher("/manager_showtimes.jsp").forward(request, response);
            return;
        }
        try {
            int theaterId = currentTheaterId.intValue();
            if (!managersDAO.isManagerAssignedToTheater(currentUser.getUserId(), theaterId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to the selected theater.");
                return;
            }
            List<Rooms> rooms = roomsDAO.getRoomsByTheaterId(theaterId);
            List<Movies> movies = moviesDAO.getSchedulableMovies();
            List<Showtimes> showtimes = showtimesDAO.getManagerShowtimesByTheaterAndDate(theaterId, selectedDate);
            request.setAttribute("rooms", rooms);
            request.setAttribute("movies", movies);
            request.setAttribute("showtimes", showtimes);
            request.getRequestDispatcher("/manager_showtimes.jsp").forward(request, response);
        } 
        catch (Exception e) {
            getServletContext().log("Failed to load manager showtimes.", e);
            throw new ServletException("Failed to load manager showtimes.", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users currentUser = getCurrentManager(request, response);
        if (currentUser == null) {
            return;
        }
        Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
        if (currentTheaterId == null) {
            request.getSession().setAttribute("showtimesError", "No active theater is selected.");
            redirectToPage(request, response, null);
            return;
        }
        int theaterId = currentTheaterId.intValue();
        int userId = currentUser.getUserId();
        LocalDate selectedDate = parseSelectedDate(request);
        try {
            if (!managersDAO.isManagerAssignedToTheater(userId, theaterId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to the selected theater.");
                return;
            }
            String action = request.getParameter("action");
            if ("create".equals(action)) {
                createShowtime(request, userId, theaterId);
            } 
            else if ("delete".equals(action)) {
                deleteShowtime(request, userId, theaterId);
            } 
            else {
                request.getSession().setAttribute("showtimesError", "Invalid showtime action.");
            }
        } 
        catch (IllegalArgumentException e) {
            request.getSession().setAttribute("showtimesError", e.getMessage());
        } 
        catch (Exception e) {
            getServletContext().log("Failed to process manager showtime action.", e);
            request.getSession().setAttribute("showtimesError", "The operation failed. Please check the information and try again.");
        }
        redirectToPage(request, response, selectedDate);
    }
    
    private void createShowtime(HttpServletRequest request, int userId, int theaterId) throws Exception {
        int roomId = parsePositiveInt(
            request.getParameter("roomId"),
            "Please select a valid room."
        );
        int movieId = parsePositiveInt(
            request.getParameter("movieId"),
            "Please select a valid movie."
        );
        int price = parsePositiveInt(
            request.getParameter("price"),
            "Ticket price must be greater than zero."
        );
        Timestamp startAt = parseDateTime(request.getParameter("startAt"));
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (!startAt.after(now)) {
            throw new IllegalArgumentException(
                "The showtime must start in the future."
            );
        }

        // Get the selected movie to calculate its ending time.
        Movies movie = moviesDAO.getMovieById(movieId);
        if (movie == null) {
            throw new IllegalArgumentException(
                "The selected movie could not be found."
            );
        }
        int duration = movie.getDuration_minute();
        if (duration <= 0) {
            throw new IllegalArgumentException("The selected movie has an invalid duration.");
        }

        // Calculate end time automatically from the movie duration.
        long endMillis = startAt.getTime() + (long) duration * 60 * 1000;
        Timestamp endAt = new Timestamp(endMillis);
        Showtimes showtime = new Showtimes();
        showtime.setRoom_id(roomId);
        showtime.setMovie_id(movieId);
        showtime.setStart_at(startAt);
        showtime.setEnd_at(endAt);
        showtime.setStatus("SCHEDULED");

        boolean created = showtimesDAO.createManagerShowtime(
            userId,
            theaterId,
            showtime,
            price
        );

        if (created) {
            request.getSession().setAttribute(
                "showtimesSuccess",
                "Showtime created successfully."
            );
        } 
        else {
            request.getSession().setAttribute(
                "showtimesError",
                "The showtime could not be created. Check the room, movie availability, and schedule for conflicts."
            );
        }
    }
    
    private void deleteShowtime(HttpServletRequest request, int userId, int theaterId) throws Exception {
        int showtimeId = parsePositiveInt(request.getParameter("showtimeId"), "Invalid showtime ID.");
        boolean deleted = showtimesDAO.softDeleteManagerShowtime(userId, theaterId, showtimeId);
        if (deleted) {
            request.getSession().setAttribute("showtimesSuccess", "Showtime removed successfully.");
        } 
        else {
            request.getSession().setAttribute("showtimesError", "The showtime could not be removed. It may already be removed, may not belong to this theater, or may have a successful payment.");
        }
    }
    
    private int parsePositiveInt(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number <= 0) {
                throw new IllegalArgumentException(errorMessage);
            }
            return number;
        } 
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
    
    private Timestamp parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Start time and end time are required.");
        }
        String normalized = value.trim().replace('T', ' ');
        if (normalized.length() == 16) {
            normalized += ":00";
        }
        try {
            return Timestamp.valueOf(normalized);
        } 
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid start or end time.");
        }
    }
    
    private LocalDate parseSelectedDate(HttpServletRequest request) {
        String value = request.getParameter("date");
        if (value == null || value.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException e) {
            return LocalDate.now();
        }
    }
    
    private Users getCurrentManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Users currentUser = session == null ? null : (Users) session.getAttribute("user");
        if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        return currentUser;
    }
    
    private void redirectToPage(HttpServletRequest request, HttpServletResponse response, LocalDate date) throws IOException {
        String url = request.getContextPath() + "/manager-showtimes";
        if (date != null) {
            url += "?date=" + date.toString();
        }
        response.sendRedirect(url);
    }
}
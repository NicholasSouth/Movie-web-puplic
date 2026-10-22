package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import com.movieweb.service.ManagerBookingService;
import com.movieweb.model.Seats;
import com.movieweb.service.SeatSelectionService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.DAO.MoviesDAO;
import com.movieweb.DAO.ShowtimesDAO;
import com.movieweb.model.Movies;
import com.movieweb.model.Showtimes;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

@WebServlet("/manager-create-booking")
public class ManagerCreateBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    private final ManagersDAO managersDAO = new ManagersDAO();
    private final ManagerBookingService managerBookingService = new ManagerBookingService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        /* 1. Check manager login */
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null
            || currentUser.getRole() == null
            || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }

        /* 2. Get manager's assigned theaters
         * Do NOT use theater_id from the URL.*/
        List<Theaters> theaters = managersDAO.getTheatersByManagerId(currentUser.getUserId());
        request.setAttribute("theaters", theaters);

        /* 3. Get current theater*/
        Integer currentTheaterId = (Integer) session.getAttribute("currentTheaterId");
        if (theaters == null
            || theaters.isEmpty()) {
            request.setAttribute("currentTheaterId", null);
            request.setAttribute("movies", Collections.emptyList());
            request.setAttribute("showtimes", Collections.emptyList());
            request.setAttribute("selectedMovieId", null);
            request.setAttribute("selectedDate", LocalDate.now().toString());
            request.getRequestDispatcher("/pos_create_booking.jsp").forward(request, response);
            return;
        }

        /* 4. Validate current theater */
        boolean validTheater = false;
        if (currentTheaterId != null) {
            for (Theaters theater : theaters) {
                if (theater.getTheater_id() == currentTheaterId) {
                    validTheater = true;
                    break;
                }
            }
        }

        /* If there is no theater selected, or the old theater is no longer assigned, 
         * automatically use the first active theater assigned to this manager. */
        if (!validTheater) {
            currentTheaterId = theaters.get(0).getTheater_id();
            session.setAttribute("currentTheaterId", currentTheaterId);
        }
        request.setAttribute("currentTheaterId", currentTheaterId);

        /* 5. Load movies */
        Movies movieFilter = new Movies();
        List<Movies> movies = moviesDAO.searchAndFilterMovies(movieFilter);
        request.setAttribute("movies", movies);

        /* 6. Read selected movie */
        Integer selectedMovieId = null;
        String movieParameter = request.getParameter("movie_id");
        if (movieParameter != null
            && !movieParameter.trim().isEmpty()) {
            try {
                int movieId = Integer.parseInt(movieParameter);
                if (movieId > 0) {
                    selectedMovieId = movieId;
                }

            }
            catch (NumberFormatException e) {
                selectedMovieId = null;
            }
        }
        request.setAttribute("selectedMovieId", selectedMovieId);

        /* 7. Read selected date */
        LocalDate today = LocalDate.now();
        LocalDate selectedDate = today;
        String dateParameter = request.getParameter("date");
        if (dateParameter != null
            && !dateParameter.trim().isEmpty()) {
            try {
                LocalDate requestedDate = LocalDate.parse(dateParameter);
                if (!requestedDate.isBefore(today)
                    && !requestedDate.isAfter(today.plusDays(6))) {
                    selectedDate = requestedDate;
                }
            }
            catch (DateTimeParseException e) {
                selectedDate = today;
            }
        }
        request.setAttribute("selectedDate", selectedDate.toString());

        /* 8. Load showtimes
         * Showtimes are determined by:
         * movie
         * current theater
         * selected date
         * Theater does NOT come from the URL.
         */
        List<Showtimes> showtimes = Collections.emptyList();
        if (selectedMovieId != null) {
            showtimes = showtimesDAO.getShowtimesByMovieTheaterAndDate(
                                selectedMovieId,
                                currentTheaterId,
                                selectedDate);
        }
        request.setAttribute("showtimes", showtimes);

        /* 9. Forward to JSP */
        request.getRequestDispatcher("/pos_create_booking.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null
            || currentUser.getRole() == null
            || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        String showtimeParameter = request.getParameter("showtime_id");
        String ticketTypeParameter = request.getParameter("ticket_type_id");
        String promotionCode = request.getParameter("promotion_code");
        String[] seatParameters = request.getParameterValues("seat_id");
        try {
            int showtimeId = Integer.parseInt(showtimeParameter);
            int ticketTypeId = Integer.parseInt(ticketTypeParameter);
            if (showtimeId <= 0 || ticketTypeId <= 0) {
                throw new IllegalArgumentException("Invalid booking information.");
            }
            if (seatParameters == null || seatParameters.length == 0) {
                throw new IllegalArgumentException("Please select at least one seat.");
            }
            List<Integer> selectedSeatIds = new ArrayList<>();
            for (String seatParameter : seatParameters) {
                int seatId = Integer.parseInt(seatParameter);
                if (seatId <= 0) {
                    throw new IllegalArgumentException("Invalid seat.");
                }
                selectedSeatIds.add(seatId);
            }
            int bookingId = managerBookingService.createBooking(
                    currentUser.getUserId(),
                    showtimeId,
                    ticketTypeId,
                    promotionCode,
                    selectedSeatIds);
            response.sendRedirect(
                    request.getContextPath()
                    + "/manager-payment?booking_id="
                    + bookingId);
        } 
        catch (NumberFormatException e) {
            request.setAttribute("bookingError", "Invalid booking information.");
            reloadSeatSelection(request, response);
        } 
        catch (IllegalArgumentException e) {
            request.setAttribute("bookingError", e.getMessage());
            reloadSeatSelection(request, response);
        } 
        catch (SQLException e) {
            getServletContext().log("Manager booking creation failed.", e);
            request.setAttribute("bookingError", "The booking could not be created. Please try again.");
            reloadSeatSelection(request, response);
        }
    }
    
    private void reloadSeatSelection(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeParameter = request.getParameter("showtime_id");
        String ticketTypeParameter = request.getParameter("ticket_type_id");
        try {
            int showtimeId = Integer.parseInt(showtimeParameter);
            int ticketTypeId = Integer.parseInt(ticketTypeParameter);
            Showtimes showtime = new SeatSelectionService().getBookableShowtime(showtimeId);
            if (showtime == null) {
                request.setAttribute("bookingError", "This showtime is no longer available.");
                request.getRequestDispatcher("/pos_seat_selection.jsp").forward(request, response);
                return;
            }
            List<Seats> seats = new SeatSelectionService().getSeatsWithPrice(showtime);
            request.setAttribute("seats", seats);
            request.setAttribute("showtimeId", showtimeId);
            request.setAttribute("ticketTypeId", ticketTypeId);
            request.getRequestDispatcher("/pos_seat_selection.jsp").forward(request, response);
        } 
        catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/manager-create-booking");
        }
    }

}
package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Seats;
import com.movieweb.model.Showtimes;
import com.movieweb.model.Users;
import com.movieweb.service.SeatSelectionService;

@WebServlet("/manager-seat-selection")
public class ManagerSeatSelectionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final SeatSelectionService seatSelectionService = new SeatSelectionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null
            || currentUser.getRole() == null
            || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/log_in.jsp");
            return;
        }
        String showtimeParameter = req.getParameter("showtime_id");
        int showtimeId;
        try {
            showtimeId = Integer.parseInt(showtimeParameter.trim());
        } 
        catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid showtime.");
            return;
        }
        Showtimes showtime = seatSelectionService.getBookableShowtime(showtimeId);
        if (showtime == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Showtime is not available.");
            return;
        }
        List<Seats> seats = seatSelectionService.getSeatsWithPrice(showtime);
        if (seats == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Pricing is not available for this showtime.");
            return;
        }
        String ticketTypeParameter = req.getParameter("ticket_type_id");
        Integer ticketTypeId = null;
        if (ticketTypeParameter != null
            && !ticketTypeParameter.trim().isEmpty()) {
            try {
                ticketTypeId = Integer.parseInt(ticketTypeParameter.trim());
            } 
            catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ticket type.");
                return;
            }
        }
        req.setAttribute("seats", seats);
        req.setAttribute("showtimeId", showtimeId);
        req.setAttribute("ticketTypeId", ticketTypeId);
        req.setAttribute("bookingError", req.getParameter("booking_error"));
        req.getRequestDispatcher("/pos_seat_selection.jsp").forward(req, resp);
    }
}
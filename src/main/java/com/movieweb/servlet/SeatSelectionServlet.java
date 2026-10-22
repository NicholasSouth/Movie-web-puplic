package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.model.Seats;
import com.movieweb.model.Showtimes;
import com.movieweb.service.SeatSelectionService;

@WebServlet("/seat_selection")
public class SeatSelectionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final SeatSelectionService seatSelectionService = new SeatSelectionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParameter = req.getParameter("showtime_id");
        int showtimeId;
        try {
            showtimeId = Integer.parseInt(idParameter.trim());
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

        req.setAttribute("seats", seats);
        req.setAttribute("showtimeId", showtimeId);
        req.getRequestDispatcher("/seat_selection.jsp").forward(req, resp);
        req.setAttribute("bookingError", req.getParameter("booking_error"));
    }
}
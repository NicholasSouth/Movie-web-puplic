package com.movieweb.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.model.Promotions;
import com.movieweb.service.BookingService;
import com.movieweb.service.PromotionCodeValidate;

@WebServlet("/create-booking")
public class CreateBookingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private final BookingService bookingService = new BookingService();
    private final PromotionCodeValidate promotionService = new PromotionCodeValidate();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");

        // A guest or logged-out visitor cannot create a booking.
        if (currentUser == null
            || "GUEST".equalsIgnoreCase(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        int showtimeId;
        try {
            showtimeId = Integer.parseInt(req.getParameter("showtime_id"));
        } 
        catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid showtime.");
            return;
        }
        String[] seatIdParameters = req.getParameterValues("seat_id");
        if (seatIdParameters == null || seatIdParameters.length == 0) {
            redirectWithError(req, resp, showtimeId, "seat");
            return;
        }
        List<Integer> selectedSeatIds = new ArrayList<>();
        try {
            for (String seatId : seatIdParameters) {
                selectedSeatIds.add(Integer.parseInt(seatId));
            }
        } 
        catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid seat selection.");
            return;
        }
        String promotionCode = req.getParameter("promotion_code");
        int promotionId = 0;
        Promotions promotion = null;
        if (promotionCode != null && !promotionCode.trim().isEmpty()) {
            promotion = promotionService.validatePromotion(promotionCode);
            if (promotion == null) {
                redirectWithError(req, resp, showtimeId, "promotion");
                return;
            }
            promotionId = promotion.getPromotion_id();
        }
        try {        
            int bookingId = bookingService.createBooking(
                    currentUser.getUserId(),
                    showtimeId,
                    promotionId,
                    promotion,
                    selectedSeatIds);
            resp.sendRedirect(req.getContextPath() + "/payment?booking_id=" + bookingId);

        } 
        catch (IllegalArgumentException | SQLException e) {
            System.err.println("=== CREATE BOOKING FAILED ===");
            e.printStackTrace();
            redirectWithError(req, resp, showtimeId, "booking");
        }
    }

    private void redirectWithError(
            HttpServletRequest req,
            HttpServletResponse resp,
            int showtimeId,
            String errorType) throws IOException {
        resp.sendRedirect(
        		req.getContextPath() 
        		+ "/seat_selection?showtime_id=" 
				+ showtimeId 
				+ "&booking_error=" 
				+ errorType);
    }
}
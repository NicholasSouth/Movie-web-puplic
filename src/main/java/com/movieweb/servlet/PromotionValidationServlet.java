package com.movieweb.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.model.Promotions;
import com.movieweb.model.Seats;
import com.movieweb.model.Showtimes;
import com.movieweb.service.PricingService;
import com.movieweb.service.PromotionCodeValidate; 
import com.movieweb.service.SeatSelectionService;

@WebServlet("/validate-promotion")
public class PromotionValidationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PromotionCodeValidate promotionService = new PromotionCodeValidate();
    private final SeatSelectionService seatSelectionService = new SeatSelectionService();
    private final PricingService pricingService = new PricingService();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String promotionCode = req.getParameter("promotion_code");
        String showtimeParameter = req.getParameter("showtime_id");
        String[] seatIdParameters = req.getParameterValues("seat_id");
        if (promotionCode == null
            || promotionCode.trim().isEmpty()) {
            sendError(resp, "Please enter a promotion code.");
            return;
        }
        int showtimeId;
        try {
            showtimeId = Integer.parseInt(showtimeParameter);
        }
        catch (Exception e) {
            sendError(resp, "Invalid showtime.");
            return;
        }
        if (seatIdParameters == null
            || seatIdParameters.length == 0) {
            sendError(resp, "Please select at least one seat.");
            return;
        }
        Promotions promotion = promotionService.validatePromotion(promotionCode);
        if (promotion == null) {
            sendError(resp, "Invalid, expired, inactive, or unavailable promotion code.");
            return;
        }
        Showtimes showtime = seatSelectionService.getBookableShowtime(showtimeId);
        if (showtime == null) {
            sendError(resp, "This showtime is no longer available.");
            return;
        }
        List<Integer> selectedSeatIds = new ArrayList<>();
        try {
            for (String seatId : seatIdParameters) {
                selectedSeatIds.add(Integer.parseInt(seatId));
            }

        }
        catch (NumberFormatException e) {
            sendError(resp, "Invalid seat selection.");
            return;
        }
        List<Seats> seats = seatSelectionService.getSeatsWithPrice(showtime);
        if (seats == null) {
            sendError(resp, "Pricing is not available.");
            return;
        }
        long total = 0;
        for (Seats seat : seats) {
            if (!selectedSeatIds.contains(seat.getSeat_id())) {
                continue;
            }
            if (!seat.isActive()
                || seat.isBooked()) {
                sendError(resp, "One or more selected seats are unavailable.");
                return;
            }
            int price = pricingService.calculatePrice(
                            showtime,
                            seat,
                            1,
                            promotion);
            if (price < 0) {
                sendError(resp, "Could not calculate the ticket price.");
                return;
            }
            total += price;
        }
        if (total <= 0) {
            sendError(resp, "Could not calculate the booking total.");
            return;
        }
        resp.getWriter().write(
                "{"
                + "\"success\":true,"
                + "\"total\":" + total + ","
                + "\"promotion_id\":"
                + promotion.getPromotion_id()
                + "}"
        );
    }

    private void sendError(
            HttpServletResponse resp,
            String message) throws IOException {
        resp.getWriter().write(
                "{"
                + "\"success\":false,"
                + "\"message\":\""
                + escapeJson(message)
                + "\""
                + "}"
        );
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
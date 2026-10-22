package com.movieweb.servlet;

import com.movieweb.DAO.Booking_seatsDAO;
import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.DAO.Payment_methodsDAO;
import com.movieweb.DAO.PromotionsDAO;
import com.movieweb.model.Payment_methods;
import com.movieweb.model.Users;
import com.movieweb.service.PaymentService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/manager-payment")
public class ManagerPaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PaymentService paymentService = new PaymentService();
    private final Booking_showtimesDAO bookingDAO = new Booking_showtimesDAO();
    private final Booking_seatsDAO bookingSeatsDAO = new Booking_seatsDAO();
    private final Payment_methodsDAO paymentMethodsDAO = new Payment_methodsDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        Users manager = getLoggedInManager(request);
        if (manager == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        Integer bookingId = parsePositiveInt(request.getParameter("booking_id"));
        if (bookingId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid booking ID.");
            return;
        }
        HttpSession session = request.getSession(false);
        Integer theaterId = session == null
                ? null
                : (Integer) session.getAttribute("currentTheaterId");
        if (theaterId == null) {
            response.sendRedirect(request.getContextPath() + "/main_manager.jsp");
            return;
        }
        try {
            Map<String, Object> booking = findBooking(theaterId, bookingId);
            if (booking == null) {
                response.sendError( HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return;
            }
            loadPaymentData(request, manager.getUserId(), bookingId, booking);
            request.getRequestDispatcher("/pos_payment.jsp").forward(request, response);
        } 
        catch (SQLException e) {
            throw new ServletException("Unable to load POS payment details.", e);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users manager = getLoggedInManager(request);
        if (manager == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        Integer bookingId = parsePositiveInt(request.getParameter("booking_id"));
        String paymentMethod = request.getParameter("payment_method");
        Integer paymentMethodId = parsePositiveInt(request.getParameter("payment_method_id"));
        if (bookingId == null
            || paymentMethod == null
            || paymentMethod.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid payment information.");
            return;
        }
        paymentMethod = paymentMethod.trim().toUpperCase();
        try {
            String transactionCode;
            switch (paymentMethod) {
                case "VISA":
                    if (paymentMethodId == null) {
                        throw new IllegalArgumentException("Please select one of your active VISA cards.");
                    }
                    transactionCode = paymentService.payWithManagerVisa(manager.getUserId(), bookingId, paymentMethodId);
                    break;
                case "CASH":
                    transactionCode = paymentService.payWithManagerCash(manager.getUserId(), bookingId);
                    break;
                case "QR":
                    transactionCode = paymentService.payWithManagerQR(manager.getUserId(), bookingId);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid payment method.");
            }
            response.sendRedirect(request.getContextPath() + "/manager-payment?booking_id=" + bookingId);
        } 
        catch (IllegalArgumentException e) {
            showPaymentError(request, response, manager, bookingId, e.getMessage());
        } 
        catch (SQLException e) {
            getServletContext().log("Manager POS payment failed.", e);
            showPaymentError(request, response, manager, bookingId, "The payment could not be completed. " + "Please try again.");
        }
    }

    private void showPaymentError(HttpServletRequest request,
            HttpServletResponse response,
            Users manager,
            int bookingId,
            String message)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer theaterId = session == null
                ? null
                : (Integer) session.getAttribute("currentTheaterId");
        if (theaterId == null) {
            response.sendRedirect(request.getContextPath() + "/main_manager.jsp");
            return;
        }
        try {
            Map<String, Object> booking = findBooking(theaterId,  bookingId);
            if (booking == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return;
            }
            loadPaymentData(request, manager.getUserId(), bookingId,  booking);
            request.setAttribute("paymentError", message);
            request.getRequestDispatcher("/pos_payment.jsp").forward(request, response);
        } 
        catch (SQLException e) {
            throw new ServletException("Unable to reload POS payment details.", e);
        }
    }

    private Map<String, Object> findBooking(
            int theaterId,
            int bookingId)
            throws SQLException {
        List<Map<String, Object>> bookings = bookingDAO.getBookingHistoryByTheaterId(theaterId);
        if (bookings == null) {
            return null;
        }
        for (Map<String, Object> booking : bookings) {
            Object id = booking.get("bookingId");
            if (id instanceof Number && ((Number) id).intValue() == bookingId) {
                return booking;
            }
        }
        return null;
    }
    
    private void loadPaymentData(HttpServletRequest request,
            int managerId,
            int bookingId,
            Map<String, Object> booking)
            throws SQLException {
        request.setAttribute("bookingId", booking.get("bookingId"));
        request.setAttribute("bookingStatus", booking.get("bookingStatus"));
        request.setAttribute("bookingPrice", booking.get("price"));
        request.setAttribute("movieName", booking.get("movieName"));
        request.setAttribute("theaterName", booking.get("theaterName"));
        request.setAttribute("roomName", booking.get("roomName"));
        request.setAttribute("showtimeStart", booking.get("startAt"));
        request.setAttribute("showtimeEnd", booking.get("endAt"));
        request.setAttribute("showtimeStatus", booking.get("showtimeStatus"));
        request.setAttribute("paymentStatus", booking.get("paymentStatus"));
        request.setAttribute("transactionCode", booking.get("transactionCode"));
        request.setAttribute("isExpired", false);
        request.setAttribute("promotion", null);
        List<Map<String, Object>> bookingSeats = bookingSeatsDAO.getBookedSeats(bookingId);
        request.setAttribute("bookingSeats", bookingSeats);
        List<Payment_methods> paymentMethods = paymentMethodsDAO.getPaymentMethodsByUserId(managerId);
        List<Map<String, Object>> visaCards = new ArrayList<>();
        if (paymentMethods != null) {
            for (Payment_methods paymentMethod : paymentMethods) {
                if (!"VISA".equalsIgnoreCase(paymentMethod.getMethod())) {
                    continue;
                }
                if (!paymentMethod.isActive()) {
                    continue;
                }
                Map<String, Object> card = new HashMap<>();
                card.put("paymentMethodId", paymentMethod.getPayment_method_id());
                String cardNumber = paymentMethod.getCard_number();
                String lastFour = cardNumber == null
                        ? "----"
                        : cardNumber.substring(Math.max(0, cardNumber.length() - 4));
                card.put("maskedNumber", "•••• •••• •••• " + lastFour);
                card.put("expiredDate", paymentMethod.getExpired_date());
                visaCards.add(card);
            }
        }
        request.setAttribute("visaCards", visaCards);
    }

    private Users getLoggedInManager(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userObject = session.getAttribute("user");
        if (!(userObject instanceof Users)) {
            return null;
        }
        Users user = (Users) userObject;
        if (!"MANAGER".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return user;
    }
    
    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0
                    ? parsed
                    : null;
        } 
        catch (NumberFormatException
                | NullPointerException e) {
            return null;
        }
    }
}

package com.movieweb.servlet;

import com.movieweb.model.Users;
import com.movieweb.service.PaymentService;
import com.movieweb.DAO.PromotionsDAO;
import com.movieweb.model.Promotions;
import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.DAO.Booking_seatsDAO;

import com.movieweb.DAO.Payment_methodsDAO;
import com.movieweb.model.Payment_methods;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PaymentService paymentService = new PaymentService();
    private final PromotionsDAO promotionsDAO = new PromotionsDAO();
    private final Booking_showtimesDAO bookingDAO = new Booking_showtimesDAO(); 
    private final Booking_seatsDAO bookingSeatsDAO = new Booking_seatsDAO(); 
    private final Payment_methodsDAO paymentMethodsDAO = new Payment_methodsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Integer bookingId = parsePositiveInt(request.getParameter("booking_id"));
        if (bookingId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid booking ID.");
            return;
        }
        try {
            if (!loadCheckoutData(request, user.getUserId(), bookingId)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return;
            }
            request.getRequestDispatcher("/payment.jsp").forward(request, response);
        } 
        catch (SQLException e) {
            throw new ServletException("Unable to load checkout details.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Integer bookingId = parsePositiveInt(request.getParameter("booking_id"));
        Integer paymentMethodId = parsePositiveInt(request.getParameter("payment_method_id"));
        if (bookingId == null || paymentMethodId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid booking or payment method.");
            return;
        }
        try {
            String transactionCode = paymentService.payWithVisa(
                    user.getUserId(),
                    bookingId,
                    paymentMethodId
            );

            // Redirect after a successful payment to avoid resubmitting POST.
            response.sendRedirect(request.getContextPath() + "/payment?booking_id=" + bookingId);
        } 
        catch (IllegalArgumentException e) {
            showPaymentError(request, response, user.getUserId(), bookingId, e.getMessage());

        } 
        catch (SQLException e) {
            // Log the detailed exception on the server; show a safe message.
            getServletContext().log("Fake VISA payment failed.", e);
            showPaymentError(request, response, user.getUserId(), bookingId,
                    "The payment could not be completed. Please try again.");
        }
    }
    
    private void showPaymentError(HttpServletRequest request,
                                  HttpServletResponse response,
                                  int userId,
                                  int bookingId,
                                  String message)
            throws ServletException, IOException {
        try {
            if (!loadCheckoutData(request, userId, bookingId)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return;
            }
            request.setAttribute("paymentError", message);
            request.getRequestDispatcher("/payment.jsp").forward(request, response);
        } 
        catch (SQLException e) {
            throw new ServletException("Unable to reload checkout details.", e);
        }
    }
    
    private boolean loadCheckoutData(HttpServletRequest request, int userId, int bookingId)
            throws SQLException 
    {
    	Map<String, Object> booking = bookingDAO.getCheckoutBooking(userId, bookingId);
        if (booking == null) 
        {
            return false; 
        }
        request.setAttribute("bookingId", booking.get("bookingId"));
        request.setAttribute("bookingStatus", booking.get("bookingStatus"));
        request.setAttribute("bookingPrice", booking.get("bookingPrice"));
        int promotionId = (Integer) booking.get("promotionId");
        if (promotionId > 0) 
        {
            Promotions promotion = promotionsDAO.getPromotionById(promotionId);
            request.setAttribute("promotion", promotion);
        }
        List<Map<String, Object>> bookingSeats = bookingSeatsDAO.getBookedSeats(bookingId);
        request.setAttribute("bookingSeats", bookingSeats);
        List<Payment_methods> paymentMethods = paymentMethodsDAO.getPaymentMethodsByUserId(userId);
        List<Map<String, Object>> visaCards = new ArrayList<>();
        for (Payment_methods paymentMethod : paymentMethods)
        {
            Map<String, Object> card = new HashMap<>();
            card.put("paymentMethodId", paymentMethod.getPayment_method_id());
            String cardNumber = paymentMethod.getCard_number();
            String lastFour = cardNumber == null
                    ? "----"
                    : cardNumber.substring(
                            Math.max(0, cardNumber.length() - 4));
            card.put("maskedNumber", "•••• •••• •••• " + lastFour);
            card.put("expiredDate", paymentMethod.getExpired_date());
            visaCards.add(card);
        }
        request.setAttribute("visaCards", visaCards);
        request.setAttribute("isExpired", booking.get("isExpired"));
        request.setAttribute("movieName", booking.get("movieName"));
        request.setAttribute("showtimeStart", booking.get("showtimeStart"));
        request.setAttribute("showtimeEnd", booking.get("showtimeEnd"));
        request.setAttribute("showtimeStatus", booking.get("showtimeStatus"));
        request.setAttribute("roomName", booking.get("roomName"));
        request.setAttribute("theaterName", booking.get("theaterName"));
        request.setAttribute("paymentStatus", booking.get("paymentStatus"));
        request.setAttribute("transactionCode", booking.get("transactionCode"));
        return true;
    }

    private Users getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userObject = session.getAttribute("user");
        if (!(userObject instanceof Users)) {
            return null;
        }
        Users user = (Users) userObject;

        // Checkout is for normal users, not managers.
        if (!"USER".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return user;
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : null;
        } 
        catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
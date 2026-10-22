
package com.movieweb.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Date;
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

import com.movieweb.DAO.Payment_methodsDAO;
import com.movieweb.model.Payment_methods;
import com.movieweb.model.Users;

@WebServlet("/manager-payments")
public class ManagerPaymentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Payment_methodsDAO paymentMethodsDAO = new Payment_methodsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!checkManager(request, response)) {
            return;
        }
        Users manager = getCurrentManager(request);
        try {
            List<Payment_methods> methods = paymentMethodsDAO.getPaymentMethodsByUserId(manager.getUserId());
            List<Map<String, Object>> paymentMethods = new ArrayList<Map<String, Object>>();
            for (Payment_methods method : methods) {
                Map<String, Object> payment = new HashMap<String, Object>();
                payment.put("paymentMethodId", method.getPayment_method_id());
                payment.put("method", method.getMethod());
                payment.put("cardNumber", method.getCard_number());
                Date expiredDate = method.getExpired_date();
                payment.put("expiredDate", expiredDate == null
                                ? null
                                : expiredDate.toString());
                payment.put("createdAt", method.getCreated_at());
                payment.put("isActive", method.isActive());
                paymentMethods.add(payment);
            }
            request.setAttribute("paymentMethods", paymentMethods);
        }
        catch (Exception e) {
            request.setAttribute("paymentError", "Unable to load payment methods: " + e.getMessage());
            request.setAttribute("paymentMethods", new ArrayList<Map<String, Object>>());
        }
        request.getRequestDispatcher("/pos_payment_methods.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!checkManager(request, response)) {
            return;
        }
        Users manager = getCurrentManager(request);
        String action = request.getParameter("action");
        if ("add".equalsIgnoreCase(action)) {
            addPaymentMethod(request, response, manager.getUserId());
            return;
        }
        if ("remove".equalsIgnoreCase(action)) {
            removePaymentMethod(request, response, manager.getUserId());
            return;
        }
        response.sendRedirect(request.getContextPath() + "/manager-payments");
    }

    private void addPaymentMethod(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String method = request.getParameter("method");
        String cardNumber = request.getParameter("card_number");
        String expiredDate = request.getParameter("expired_date");
        if (method == null || method.trim().isEmpty()) {
            redirectError(request, response, "Payment method is required.");
            return;
        }
        method = method.trim().toUpperCase();
        if (!method.equals("VISA")
                && !method.equals("CASH")
                && !method.equals("QR")) {
            redirectError(request, response, "Invalid payment method.");
            return;
        }
        if ("VISA".equals(method)) {
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                redirectError(request, response, "Card number is required for VISA.");
                return;
            }
            if (expiredDate == null || expiredDate.trim().isEmpty()) {
                redirectError(request, response, "Expired date is required for VISA.");
                return;
            }
        }
        else {
            cardNumber = null;
            expiredDate = null;
        }
        try {
            Payment_methods paymentMethod = new Payment_methods();
            paymentMethod.setUser_id(userId);
            paymentMethod.setMethod(method);
            paymentMethod.setCard_number(cardNumber == null ? null : cardNumber.trim());
            paymentMethod.setExpired_date(parseExpiryDate(expiredDate));
            paymentMethod.setActive(true);
            boolean added = paymentMethodsDAO.addPaymentMethod(paymentMethod);
            if (!added) {
                redirectError(request, response, "Unable to add payment method.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/manager-payments?success=added");
        }
        catch (IllegalArgumentException e) {
            redirectError(request, response, e.getMessage());
        }
        catch (Exception e) {
            redirectError(request, response, "Unable to add payment method.");
        }
    }

    private void removePaymentMethod(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String parameter = request.getParameter("payment_method_id");
        int paymentMethodId;
        try {
            paymentMethodId = Integer.parseInt(parameter);
            if (paymentMethodId <= 0) {
                throw new NumberFormatException();
            }
        }
        catch (Exception e) {
            redirectError(request, response, "Invalid payment method.");
            return;
        }
        boolean removed = paymentMethodsDAO.deactivatePaymentMethod(paymentMethodId, userId);
        if (!removed) {
            redirectError(request, response, "Payment method was not found or could not be removed.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/manager-payments?success=removed");
    }

    private Date parseExpiryDate(String expiredDate) {
        if (expiredDate == null || expiredDate.trim().isEmpty()) {
            return null;
        }
        String value = expiredDate.trim();

        // Convert MM/YYYY to the first day of that month.
        if (value.matches("(0[1-9]|1[0-2])/\\d{4}")) {
            String[] parts = value.split("/");
            return Date.valueOf(parts[1] + "-" + parts[0] + "-01");
        }
        throw new IllegalArgumentException("Invalid expiry date. Use MM/YYYY.");
    }

    private Users getCurrentManager(HttpServletRequest request) {
        return (Users) request.getSession(false).getAttribute("user");
    }

    private boolean checkManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null
            | !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return false;
        }
        return true;
    }

    private void redirectError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        if (message == null || message.trim().isEmpty()) {
            message = "Payment operation failed.";
        }
        String encodedMessage = URLEncoder.encode(message, "UTF-8");
        response.sendRedirect(request.getContextPath() + "/manager-payments?error=" + encodedMessage);
    }
}
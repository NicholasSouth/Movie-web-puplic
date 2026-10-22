package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Payment_methods;
import com.movieweb.model.Users;
import com.movieweb.service.PaymentMethodsService;

@WebServlet("/pos-payment-method")
public class POSPaymentMethodsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private PaymentMethodsService paymentMethodsService;

    @Override
    public void init() throws ServletException {
        paymentMethodsService = new PaymentMethodsService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }

        // Manager only
        if (!"MANAGER".equals(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }

        // Get selected theater
        Integer currentTheaterId = (Integer) session.getAttribute("currentTheaterId");
        if (currentTheaterId == null) {
            request.setAttribute("error", "Please select a theater first.");
            request.getRequestDispatcher("/pos_payment_methods.jsp").forward(request, response);
            return;
        }

        // Get success message from session
        String success = (String) session.getAttribute("success");
        if (success != null) {
            request.setAttribute("success", success);
            session.removeAttribute("success");
        }

        // Get VISA cards belonging to this manager
        // AND assigned to the selected theater.
        List<Payment_methods> paymentMethods = paymentMethodsService.getPaymentMethodsByManagerAndTheater(
                        currentUser.getUserId(),
                        currentTheaterId
                    );
        request.setAttribute("paymentMethods", paymentMethods);
        request.getRequestDispatcher("/pos_payment_methods.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        if (!"MANAGER".equals(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        Integer currentTheaterId = (Integer) session.getAttribute("currentTheaterId");
        if (currentTheaterId == null) {
            request.setAttribute("error", "Please select a theater first.");
            doGet(request, response);
            return;
        }
        String action = request.getParameter("action");

        // Add VISA
        if ("add".equals(action)) {
            String card_number = request.getParameter("card_number");
            String expired_date = request.getParameter("expired_date");
            String error = paymentMethodsService.addVisaCardForTheater(
                        currentUser.getUserId(),
                        currentTheaterId,
                        card_number,
                        expired_date
                    );
            if (error != null) {
                request.setAttribute("error", error);

                // Keep entered values
                request.setAttribute("card_number", card_number);
                request.setAttribute("expired_date", expired_date);
                doGet(request, response);
                return;
            }
            session.setAttribute("success", "VISA card added successfully.");
            response.sendRedirect(request.getContextPath() + "/pos-payment-method");
            return;
        }

        // Deactivate VISA
        if ("deactivate".equals(action)) {
            String payment_method_id = request.getParameter("payment_method_id");
            try {
                int id = Integer.parseInt(payment_method_id);
                String error = paymentMethodsService.deactivatePaymentMethodForTheater(id, currentUser.getUserId(), currentTheaterId);
                if (error != null) {
                    request.setAttribute("error", error);
                    doGet(request, response);
                    return;
                }
                session.setAttribute("success", "VISA card deactivated successfully.");
                response.sendRedirect(request.getContextPath() + "/pos-payment-method");
            } 
            catch (NumberFormatException e) {
                request.setAttribute("error", "Invalid payment method.");
                doGet(request, response);
            }
            return;
        }

        // Invalid action
        request.setAttribute("error", "Invalid payment method action.");
        doGet(request, response);
    }
}
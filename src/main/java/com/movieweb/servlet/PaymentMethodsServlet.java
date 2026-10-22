package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.movieweb.model.Payment_methods;
import com.movieweb.model.Users;
import com.movieweb.service.PaymentMethodsService;

@WebServlet("/payment-method")
public class PaymentMethodsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private PaymentMethodsService paymentMethodsService;

    @Override
    public void init() throws ServletException {
        paymentMethodsService = new PaymentMethodsService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = (Users) request.getSession().getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }

        // Get success message from session.
        String success = (String) request.getSession().getAttribute("success");
        if (success != null) {
            request.setAttribute("success", success);
            request.getSession().removeAttribute("success");
        }

        // Get all payment methods belonging to the user.
        List<Payment_methods> paymentMethods = paymentMethodsService.getPaymentMethodsByUserId(currentUser.getUserId());
        request.setAttribute("paymentMethods", paymentMethods);
        request.getRequestDispatcher("/payment_methods.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = (Users) request.getSession().getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        String action = request.getParameter("action");

        // Add visa
        if ("add".equals(action)) {
            String card_number = request.getParameter("card_number");
            String expired_date = request.getParameter("expired_date");

            String error = paymentMethodsService.addVisaCard(currentUser.getUserId(), card_number, expired_date);
            if (error != null) {
                request.setAttribute("error", error);

                // Keep the entered values so the user does not have to type everything again.
                request.setAttribute("card_number", card_number);
                request.setAttribute("expired_date", expired_date);
                doGet(request, response);
                return;
            }

            // Successful addition.
            request.getSession().setAttribute("success", "VISA card added successfully.");
            response.sendRedirect(request.getContextPath() + "/payment-method");
            return;
        }

        // Deactivate visa
        if ("deactivate".equals(action)) {
            String payment_method_id = request.getParameter("payment_method_id");
            try {
                int id = Integer.parseInt(payment_method_id);
                String error = paymentMethodsService.deactivatePaymentMethod(id, currentUser.getUserId());
                if (error != null) {
                    request.setAttribute("error", error);
                    doGet(request, response);
                    return;
                }
                request.getSession().setAttribute("success", "VISA card deactivated successfully.");
                response.sendRedirect(request.getContextPath() + "/payment-method");
            } 
            catch (NumberFormatException e) {
                request.setAttribute("error", "Invalid payment method.");
                doGet(request, response);
            }
            return;
        }

        // Invalid Action
        request.setAttribute("error", "Invalid payment method action.");
        doGet(request, response);
    }
}
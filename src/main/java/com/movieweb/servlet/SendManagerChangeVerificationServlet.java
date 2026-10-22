package com.movieweb.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.service.EmailService;
import com.movieweb.service.OTPValidation;

@WebServlet("/manager-send-change-verification")
public class SendManagerChangeVerificationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final OTPValidation verificationService = new OTPValidation();
    private final EmailService emailService = new EmailService();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null) {
            sendMessage(response, "Your session has expired. Please log in again.");
            return;
        }
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null) {
            sendMessage(response, "You must be logged in to change your password.");
            return;
        }
        if (!"MANAGER".equals(currentUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            sendMessage(response, "Only managers can request this verification code.");
            return;
        }
        String email = currentUser.getEmail();
        if (email == null || email.trim().isEmpty()) {
            sendMessage(response, "Your account does not have an email address.");
            return;
        }
        email = email.trim().toLowerCase();
        String code = verificationService.generateCode();
        verificationService.storeCode(session, email, "manager_change_password", code);
        boolean sent = emailService.sendVerificationCode(email, code, "manager_change_password");
        if (sent) {
            sendMessage(response, "Verification code sent successfully.");
        } 
        else {
            verificationService.clearCode(session);
            sendMessage(response, "Failed to send verification email.");
        }
    }
    
    private void sendMessage(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
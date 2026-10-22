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

/** Emails a 6-digit code to the logged-in administrator for the password change on /admin-profile. */
@WebServlet("/admin-send-verification")
public class SendAdminVerificationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final OTPValidation verificationService = new OTPValidation();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user == null) {
            sendMessage(response, "Your session has expired. Please log in again.");
            return;
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole()) || !user.isActive()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            sendMessage(response, "Only administrators can request this verification code.");
            return;
        }
        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            sendMessage(response, "Your account does not have an email address.");
            return;
        }
        email = email.trim().toLowerCase();
        String code = verificationService.generateCode();
        verificationService.storeCode(session, email, AdminProfileServlet.PASSWORD_PURPOSE, code);
        if (emailService.sendVerificationCode(email, code, AdminProfileServlet.PASSWORD_PURPOSE)) {
            sendMessage(response, "Verification code sent successfully.");
        } else {
            verificationService.clearCode(session);
            sendMessage(response, "Failed to send verification email.");
        }
    }

    private void sendMessage(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}

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

@WebServlet("/send-change-verification")
public class SendChangeVerificationServlet
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final OTPValidation verificationService = new OTPValidation();
    private final EmailService emailService = new EmailService();

    // POST /send-password-change-verification
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        // Check whether the user is logged in
        if (session == null)
        {
            sendMessage(response, "Your session has expired. Please log in again.");
            return;
        }
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null)
        {
            sendMessage(response, "You must be logged in to change your password.");
            return;
        }

        // Get the email from the logged-in account
        String email = currentUser.getEmail();
        if (email == null || email.trim().isEmpty())
        {
            sendMessage(response, "Your account does not have an email address.");
            return;
        }
        email = email.trim().toLowerCase();

        // Generate OTP
        String code = verificationService.generateCode();

        // Store OTP in session
        verificationService.storeCode(
                session,
                email,
                "change_password",
                code
        );

        // Send email
        boolean sent = emailService.sendVerificationCode(
                email,
                code,
                "change_password"
        );

        if (sent)
        {
            sendMessage(response, "Verification code sent successfully.");
        }
        else
        {
            // Email failed, so remove the stored OTP
            verificationService.clearCode(session);
            sendMessage(response, "Failed to send verification email.");
        }
    }

    // Send plain text response
    private void sendMessage(
            HttpServletResponse response,
            String message)
            throws IOException
    {
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
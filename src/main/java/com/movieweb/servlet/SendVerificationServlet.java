package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.service.EmailService;
import com.movieweb.service.OTPValidation;

@WebServlet("/send-verification")
public class SendVerificationServlet
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final OTPValidation verificationService = new OTPValidation();
    private final EmailService emailService = new EmailService();

    // POST /send-verification
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");
        String purpose = request.getParameter("purpose");

        // Validate email
        if (email == null ||
            email.trim().isEmpty())
        {
            sendMessage(response, "Please enter your email address.");
            return;
        }

        // Validate purpose
        if (purpose == null ||
            (!purpose.equals("register") &&
             !purpose.equals("forgot_password")))
        {
            sendMessage(response, "Invalid verification request.");
            return;
        }
        email = email.trim().toLowerCase();

        // Generate OTP
        String code = verificationService.generateCode();

        // Store OTP in session
        HttpSession session = request.getSession();
        verificationService.storeCode(
					                session,
					                email,
					                purpose,
					                code);

        // Send email
        boolean sent = emailService.sendVerificationCode(
								                        email,
								                        code,
								                        purpose);
        if (sent)
        {
            sendMessage(response, "Verification code sent successfully.");
        }
        else
        {
            //Email failed -> no OTP
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
        response.setContentType( "text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
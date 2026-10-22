package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.service.EmailService;
import com.movieweb.service.FetchEmailFromAccount;
import com.movieweb.service.OTPValidation;

@WebServlet("/send-forgot-verification")
public class SendForgetVerificationServlet
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final FetchEmailFromAccount forgotService = new FetchEmailFromAccount();
    private final OTPValidation otpValidation = new OTPValidation();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        String account = request.getParameter("account");
        if (account == null ||
            account.trim().isEmpty())
        {
            sendMessage(
                    response,
                    "Please enter your username, email or phone number.");
            return;
        }
        account = account.trim();

        //Find the account and get its registered email.
        String email = forgotService.getAccountEmail(account);

        if (email == null)
        {
            sendMessage(
                    response,
                    "No active account was found with that information.");
            return;
        }

        //Generate OTP.
        String code = otpValidation.generateCode();

        //Store OTP in the session.
        //Store the EMAIL, not the username/phone.
        HttpSession session = request.getSession();

        otpValidation.storeCode(
				                session,
				                email,
				                "forgot_password",
				                code);
        
        //Send OTP
        boolean sent = emailService.sendVerificationCode(
								                        email,
								                        code,
								                        "forgot_password");
        if (sent)
        {
            sendMessage(
                    response,
                    "Verification code sent successfully.");
        }
        else
        {
            otpValidation.clearCode(session);
            sendMessage(
                    response,
                    "Failed to send verification email.");
        }
    }
    private void sendMessage(
            HttpServletResponse response,
            String message)
            throws IOException
    {
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}
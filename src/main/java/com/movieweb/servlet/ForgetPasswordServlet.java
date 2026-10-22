package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.service.OTPValidation;
import com.movieweb.service.UserLoginValidation;

@WebServlet("/forgot-password")
public class ForgetPasswordServlet
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final UserLoginValidation userService = new UserLoginValidation();
    private final OTPValidation otpValidation = new OTPValidation();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        String account = request.getParameter("account");
        String newPassword = request.getParameter("new_password");
        String confirmPassword = request.getParameter("confirm_password");
        String verificationCode = request.getParameter("verification_code");

        // Basic validation
        if (account == null ||
            account.trim().isEmpty())
        {
            showError(request, response, "Please enter your email or phone number.", account);
            return;
        }
        account = account.trim();

        // Find user using username/email/phone logic
        Users user = userService.getUserByLogin(account);
        if (user == null)
        {
            showError(request, response, "No account was found with that information.", account);
            return;
        }

        // Check if account is active
        if (!user.isActive())
        {
            showError(request, response, "This account is currently disabled.", account);
            return;
        }

        // Validate password
        if (newPassword == null ||
            newPassword.isEmpty())
        {
            showError(request, response, "Please enter a new password.", account);
            return;
        }
        if (confirmPassword == null ||
            confirmPassword.isEmpty())
        {
            showError(request, response, "Please confirm your new password.", account);
            return;
        }
        if (!newPassword.equals(confirmPassword))
        {
            showError(request, response, "Passwords do not match.", account);
            return;
        }

        // Get the user's actual email.
        // This allows future support for phone number login.
        String email = user.getEmail();
        if (email == null ||
            email.trim().isEmpty())
        {
            showError(request, response, "This account does not have an email address.", account);
            return;
        }
        email = email.trim().toLowerCase();

        // Verify OTP
        HttpSession session = request.getSession();
        boolean verified = otpValidation.
        		verifyCode(session, email, "forgot_password", verificationCode);
        if (!verified)
        {
            showError(request, response, "Invalid or expired verification code.", account);
            return;
        }

        // Reset password
        String error = userService.resetPassword(email, newPassword, confirmPassword);
        if (error != null)
        {
            showError(request, response, error, account);
            return;
        }

        // Password successfully changed
        request.getSession().setAttribute("message", "Password reset successfully. You can now log in.");
        response.sendRedirect(request.getContextPath() + "/log_in.jsp");
        return;
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String error,
            String account)
            throws ServletException, IOException
    {
        request.setAttribute("error", error);
        request.setAttribute("account", account);
        request.getRequestDispatcher("/forget.jsp").forward(request, response);
    }
}
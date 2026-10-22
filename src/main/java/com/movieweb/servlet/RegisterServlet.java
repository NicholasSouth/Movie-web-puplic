package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.service.UserLoginValidation;
import com.movieweb.service.OTPValidation;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final UserLoginValidation userService = new UserLoginValidation();
    private final OTPValidation verificationService = new OTPValidation();

    // POST /register
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");

        // Get form data       
        String username = request.getParameter("username");
        String fullName = request.getParameter("full_name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");
        String verificationCode = request.getParameter("verification_code");
        String manager = request.getParameter("manager");

        // Basic cleanup
        if (username != null)
        {
            username = username.trim();
        }
        if (fullName != null)
        {
            fullName = fullName.trim();
        }
        if (email != null)
        {
            email = email.trim().toLowerCase();
        }
        if (phone != null)
        {
            phone = phone.trim();
        }
        if (verificationCode != null)
        {
            verificationCode = verificationCode.trim();
        }

        // Determine role
        String role;
        if (manager != null)
        {
            role = "MANAGER";
        }
        else
        {
            role = "USER";
        }

        // Basic validation
        if (username == null ||
            username.isEmpty())
        {
            returnError(
                    request,
                    response,
                    "Username is required.");
            return;
        }
        if (fullName == null ||
            fullName.isEmpty())
        {
            returnError(
                    request,
                    response,
                    "Full name is required.");
            return;
        }
        if (email == null ||
            email.isEmpty())
        {
            returnError(
                    request,
                    response,
                    "Email is required.");
            return;
        }
        if (password == null ||
            password.isEmpty())
        {
            returnError(
                    request,
                    response,
                    "Password is required.");
            return;
        }
        if (confirmPassword == null ||
            confirmPassword.isEmpty())
        {
            returnError(
                    request,
                    response,
                    "Please confirm your password.");
            return;
        }
        if (verificationCode == null ||
            verificationCode.isEmpty())
        {
            returnError(
                    request,
                    response,
                    "Verification code is required.");
            return;
        }

        // Verify email OTP
        HttpSession session = request.getSession();
        boolean verified = verificationService.verifyCode(
								                        session,
								                        email,
								                        "register",
								                        verificationCode);
        if (!verified)
        {
            returnError(
                    request,
                    response,
                    "Invalid or expired verification code.");
            return;
        }

        // Register user through Service
        String error = userService.registerUser(
						                        username,
						                        fullName,
						                        email,
						                        phone,
						                        password,
						                        confirmPassword,
						                        role);

        // Registration failed
        if (error != null)
        {
            returnError(
                    request,
                    response,
                    error);
            return;
        }

        // Registration successful
        if (role.equals("MANAGER"))
        {            
            response.sendRedirect(
                    request.getContextPath()
                    + "/waiting.jsp");
        }
        else
        {
            response.sendRedirect(
                    request.getContextPath()
                    + "/log_in.jsp");
        }
    }

    // Display registration error
    private void returnError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws ServletException, IOException
    {
        request.setAttribute(
                "error",
                message);
        request.getRequestDispatcher(
                "/register.jsp")
                .forward(
                        request,
                        response);
    }
}
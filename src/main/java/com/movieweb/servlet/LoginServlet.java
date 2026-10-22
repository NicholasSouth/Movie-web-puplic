package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.service.RememberMeService;
import com.movieweb.service.UserLoginValidation;

@WebServlet("/login")
public class LoginServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final UserLoginValidation userService = new UserLoginValidation();
    private final RememberMeService rememberMeService = new RememberMeService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        String rememberMeParameter = request.getParameter("remember_me");
        boolean rememberMe = "true".equals(rememberMeParameter);
        if (login != null)
        {
            login = login.trim();
        }

        //validate login
        Users user = userService.login(login, password);
        if (user == null)
        {
            request.setAttribute("error", "Invalid username/email/phone or password.");
            request.setAttribute("login", login);
            request.getRequestDispatcher("/log_in.jsp").forward(request, response);
            return;
        }

        //Sucessful login
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        //Create remember me cookie
        if (rememberMe)
        {
            rememberMeService.createRememberMe(user, response);
        }

        // Redirect based on user's role
        if ("MANAGER".equalsIgnoreCase(user.getRole()))
        {
            response.sendRedirect(request.getContextPath() + "/main_manager.jsp");
        }
        else if ("ADMIN".equalsIgnoreCase(user.getRole()))
        {
            response.sendRedirect(request.getContextPath() + "/admin-dashboard");
        }
        else
        {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
        }
    }
}
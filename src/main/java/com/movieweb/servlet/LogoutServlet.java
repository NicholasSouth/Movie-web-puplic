package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.service.RememberMeService;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private final RememberMeService rememberMeService = new RememberMeService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        //Remove remember me cookie and database
        rememberMeService.removeRememberMe(
                request,
                response);

        //Remove current session
        HttpSession session = request.getSession(false);
        if (session != null)
        {
            session.invalidate();
        }

        //Back to main page
        response.sendRedirect(
                request.getContextPath()
                + "/main.jsp");
    }
}
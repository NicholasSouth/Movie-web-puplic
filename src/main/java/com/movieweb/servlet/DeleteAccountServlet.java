package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.service.DeleteAccount;

@WebServlet("/delete-account")
public class DeleteAccountServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private DeleteAccount deleteAccount;

    @Override
    public void init() throws ServletException
    {
        deleteAccount = new DeleteAccount();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        // Administrator accounts cannot be deleted (UsersDAO.softDeleteAccount enforces this too).
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole()))
        {
            session.setAttribute("adminProfileFlashError", "Administrator accounts cannot be deleted.");
            response.sendRedirect(request.getContextPath() + "/admin-profile");
            return;
        }
        boolean deleted = deleteAccount.deleteAccount(currentUser);
        if (deleted)
        {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/main.jsp");
        }
        else
        {
            request.setAttribute("error", "Unable to delete your account. Please try again.");
            request.getRequestDispatcher("/settings.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        response.sendRedirect(request.getContextPath() + "/settings.jsp");
    }
}
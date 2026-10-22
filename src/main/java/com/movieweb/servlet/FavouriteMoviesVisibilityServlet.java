package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;

@WebServlet("/favourite-visibility")
public class FavouriteMoviesVisibilityServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private UsersDAO usersDAO;

    @Override
    public void init()
            throws ServletException
    {
        usersDAO = new UsersDAO();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);

        /*Make sure the user is logged in.*/
        if (session == null)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null)
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        /*Get the new visibility value.*/
        String publicValue = request.getParameter("isPublic");
        if (publicValue == null)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        boolean isPublic = Boolean.parseBoolean(publicValue);
        
        /*Update the currently logged-in user's favourite movie visibility.*/
        boolean updated = usersDAO.updateFavouriteMoviesVisibility(
                        currentUser.getUserId(),
                        isPublic);
        if (updated)
        {
            /*Update the user stored in the session 
        	=> New value is immediately available.*/
            currentUser.setFavouriteMoviesVisibility(isPublic);
            session.setAttribute("user", currentUser);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        else
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
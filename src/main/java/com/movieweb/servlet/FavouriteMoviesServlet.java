package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.service.FavouriteMoviesService;

@WebServlet("/favorite")
public class FavouriteMoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private FavouriteMoviesService favouriteMoviesService;

    @Override
    public void init() throws ServletException {
        favouriteMoviesService = new FavouriteMoviesService();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        //Check login status
        if (session == null
            || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        Users user = (Users) session.getAttribute("user");

        // CSRF protection
        if (!MoviePageServlet.validCsrf(request, request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        //Read form parameters
        String movieIdParameter = request.getParameter("movieId");
        String action = request.getParameter("action");
        if (movieIdParameter == null
            || action == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing movie ID or favorite action.");
            return;
        }
        try {
            int movieId = Integer.parseInt(movieIdParameter);
            int userId = user.getUserId();
            boolean success;

            //Add or remove favorite
            if ("add".equals(action)) {
                success = favouriteMoviesService.addFavourite(
                                userId,
                                movieId);
            } 
            else if ("remove".equals(action)) {
                success = favouriteMoviesService.removeFavourite(
                                userId,
                                movieId);

            } 
            else {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid favorite action.");
                return;
            }

            //Redirect back to the movie details page
            String status;
            if (success) {
                status = "add".equals(action)
                        ? "added"
                        : "removed";
            } 
            else {
                status = "error";
            }
            response.sendRedirect(
                    request.getContextPath()
                    + "/movie-details?id="
                    + movieId
                    + "&favourite="
                    + status);
        } 
        catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid movie ID.");
        }
    }
}
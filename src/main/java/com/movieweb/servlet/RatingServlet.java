package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.service.RatingsService;

@WebServlet("/rating")
public class RatingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private RatingsService ratingsService;

    @Override
    public void init() throws ServletException {
        ratingsService = new RatingsService();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        // Check login status
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

        // Read form parameters
        String movieIdParameter = request.getParameter("movieId");
        String ratingParameter = request.getParameter("rating");
        if (movieIdParameter == null
            || ratingParameter == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing movie ID or rating.");
            return;
        }
        try {
            int movieId = Integer.parseInt(movieIdParameter);
            int rating = Integer.parseInt(ratingParameter);
            int userId = user.getUserId();

            // Add or update rating
            boolean success = ratingsService.setRating(userId, movieId, rating);

            // Redirect back to movie details page
            String status = success
                            ? "success"
                            : "error";
            response.sendRedirect(
                    request.getContextPath()
                    + "/movie-details?id="
                    + movieId
                    + "&rating="
                    + status
            );
        } 
        catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid movie ID or rating.");
        } 
        catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to save rating.");
        }
    }
}
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
import com.movieweb.service.CommentsService;

@WebServlet("/comment")
public class CommentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CommentsService commentsService;

    @Override
    public void init() throws ServletException {
        commentsService = new CommentsService();
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
        String commentText = request.getParameter("commentText");
        if (movieIdParameter == null
            || commentText == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing movie ID or comment.");
            return;
        }
        try {
            int movieId = Integer.parseInt(movieIdParameter);
            int userId = user.getUserId();

            // Add comment
            boolean success = commentsService.addComment(
                            userId,
                            movieId,
                            commentText
                    );

            // Redirect back to movie details page
            String status = success
                            ? "success"
                            : "error";
            response.sendRedirect(
                    request.getContextPath()
                    + "/movie-details?id="
                    + movieId
                    + "&comment="
                    + status
            );
        } 
        catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid movie ID.");
        } 
        catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } 
        catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to save comment.");
        }
    }
}
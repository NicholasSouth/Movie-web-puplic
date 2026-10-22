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
import com.movieweb.service.ReportsService;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReportsService reportsService;

    @Override
    public void init() throws ServletException {
        reportsService = new ReportsService();
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
        String commentIdParameter = request.getParameter("commentId");
        String movieIdParameter = request.getParameter("movieId");
        if (commentIdParameter == null
            || movieIdParameter == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing comment ID or movie ID.");
            return;
        }
        try {
            int commentId = Integer.parseInt(commentIdParameter);
            int movieId = Integer.parseInt(movieIdParameter);
            int userId = user.getUserId();

            // Add report
            boolean success = reportsService.addReport(userId, commentId);

            // Redirect back to movie details page
            String status = success
                            ? "success"
                            : "already";
            response.sendRedirect(
                    request.getContextPath()
                    + "/movie-details?id="
                    + movieId
                    + "&report="
                    + status
            );
        } 
        catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid comment ID or movie ID.");
        } 
        catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } 
        catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to report comment.");
        }
    }
}
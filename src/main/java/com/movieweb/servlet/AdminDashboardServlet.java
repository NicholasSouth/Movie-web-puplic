package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.DAO.AdminDashboardDAO;
import com.movieweb.model.AdminDashboardSummary;

/* Authorization is enforced by AdminFilter (mapped to /admin-dashboard). */
@WebServlet("/admin-dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer theaterId = (Integer) request.getAttribute("adminTheaterId"); // null = all theaters
        LocalDate today = LocalDate.now();
        AdminDashboardDAO dao = new AdminDashboardDAO();
        boolean summaryAvailable = false;
        try {
            AdminDashboardSummary summary = dao.getSummary(theaterId, today);
            request.setAttribute("adminSummary", summary);
            summaryAvailable = true;
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin dashboard summary (theater=" + theaterId + ")", e);
        }
        boolean breakdownAvailable = false;
        try {
            request.setAttribute("adminTheaterBreakdown", dao.getTheaterBreakdown(today));
            breakdownAvailable = true;
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin theater breakdown", e);
            request.setAttribute("adminTheaterBreakdown", Collections.emptyList());
        }
        request.setAttribute("adminSummaryAvailable", summaryAvailable);
        request.setAttribute("adminBreakdownAvailable", breakdownAvailable);
        request.getRequestDispatcher("/main_admin.jsp").forward(request, response);
    }
}

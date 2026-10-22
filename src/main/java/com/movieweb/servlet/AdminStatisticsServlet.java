package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.DAO.AdminStatisticsDAO;

/* System-wide statistics. Authorization and the theater selection (request attribute adminTheaterId,
 * null = all theaters, validated server-side) come from AdminFilter. Each report section is loaded on
 * its own so one failing query is reported as unavailable instead of being shown as zeros.
 */
@WebServlet("/admin-statistics")
public class AdminStatisticsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_DATE_RANGE_DAYS = 366;
    private final AdminStatisticsDAO dao = new AdminStatisticsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer theaterId = (Integer) request.getAttribute("adminTheaterId");
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        String startParam = request.getParameter("startDate");
        String endParam = request.getParameter("endDate");
        String errorMessage = null;
        if (notBlank(startParam) || notBlank(endParam)) {
            try {
                if (!notBlank(startParam) || !notBlank(endParam)) {
                    throw new IllegalArgumentException("Please choose both a start date and an end date.");
                }
                startDate = LocalDate.parse(startParam.trim());
                endDate = LocalDate.parse(endParam.trim());
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date must be on or before end date.");
                }
                if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_DATE_RANGE_DAYS) {
                    throw new IllegalArgumentException("Please select a date range of 366 days or fewer.");
                }
            } catch (RuntimeException e) {
                startDate = LocalDate.now().minusDays(6);
                endDate = LocalDate.now();
                errorMessage = e instanceof IllegalArgumentException
                        ? e.getMessage() : "Invalid date range. The last seven days are displayed.";
            }
        }
        List<String> failed = new ArrayList<>();
        try {
            request.setAttribute("statistics", dao.getSummary(theaterId, startDate, endDate));
        } catch (SQLException e) { fail(failed, "summary", e); }
        try {
            request.setAttribute("dailyStatistics", dao.getDaily(theaterId, startDate, endDate));
        } catch (SQLException e) { fail(failed, "daily trends", e); }
        try {
            request.setAttribute("topMovies", dao.getTopMovies(theaterId, startDate, endDate));
        } catch (SQLException e) { fail(failed, "top movies", e); }
        try {
            request.setAttribute("theaterComparison", dao.getTheaterComparison(startDate, endDate));
        } catch (SQLException e) { fail(failed, "theater comparison", e); }
        try {
            request.setAttribute("paymentMethods", dao.getPaymentMethods(theaterId, startDate, endDate));
        } catch (SQLException e) { fail(failed, "payment methods", e); }
        try {
            request.setAttribute("bookingStatuses", dao.getBookingStatusCounts(theaterId, startDate, endDate));
        } catch (SQLException e) { fail(failed, "booking statuses", e); }

        request.setAttribute("failedSections", failed.isEmpty() ? Collections.emptyList() : failed);
        request.setAttribute("startDate", startDate.toString());
        request.setAttribute("endDate", endDate.toString());
        request.setAttribute("errorMessage", errorMessage);
        request.getRequestDispatcher("/admin_statistics.jsp").forward(request, response);
    }

    private void fail(List<String> failed, String section, SQLException e) {
        getServletContext().log("Admin statistics: failed to load " + section, e);
        failed.add(section);
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}

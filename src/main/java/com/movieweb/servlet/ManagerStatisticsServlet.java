package com.movieweb.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.DAO.ManagerStatisticsDAO;
import com.movieweb.model.ManagerDailyStatistic;
import com.movieweb.model.ManagerMovieStatistic;
import com.movieweb.model.ManagerStatistics;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

@WebServlet("/manager-statistics")
public class ManagerStatisticsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_DATE_RANGE_DAYS = 366;
    private final ManagersDAO managersDAO = new ManagersDAO();
    private final ManagerStatisticsDAO statisticsDAO = new ManagerStatisticsDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getCurrentManager(request, response);
        if (currentUser == null) {
            return;
        }
        Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
        List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        String startParam = request.getParameter("startDate");
        String endParam = request.getParameter("endDate");
        String errorMessage = null;
        if ((startParam != null && !startParam.trim().isEmpty()) || (endParam != null && !endParam.trim().isEmpty())) {
            try {
                if (startParam == null || startParam.trim().isEmpty() || endParam == null || endParam.trim().isEmpty()) {
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
                errorMessage = e instanceof IllegalArgumentException ? e.getMessage() : "Invalid date range. The last seven days are displayed.";
            }
        }
        request.setAttribute("startDate", startDate.toString());
        request.setAttribute("endDate", endDate.toString());
        request.setAttribute("errorMessage", errorMessage);
        if (currentTheaterId == null) {
            request.setAttribute("statistics", new ManagerStatistics());
            request.setAttribute("dailyStatistics", new ArrayList<ManagerDailyStatistic>());
            request.setAttribute("topMovies", new ArrayList<ManagerMovieStatistic>());
            request.getRequestDispatcher("/manager_statistics.jsp").forward(request, response);
            return;
        }
        try {
            int theaterId = currentTheaterId.intValue();
            if (!managersDAO.isManagerAssignedToTheater(currentUser.getUserId(), theaterId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to the selected theater.");
                return;
            }
            ManagerStatistics statistics = statisticsDAO.getSummaryByTheaterAndDateRange(theaterId, startDate, endDate);
            List<ManagerDailyStatistic> dailyStatistics = statisticsDAO.getDailyStatisticsByTheaterAndDateRange(theaterId, startDate, endDate);
            List<ManagerMovieStatistic> topMovies = statisticsDAO.getTopMoviesByTheaterAndDateRange(theaterId, startDate, endDate);
            request.setAttribute("statistics", statistics);
            request.setAttribute("dailyStatistics", dailyStatistics);
            request.setAttribute("topMovies", topMovies);
            request.getRequestDispatcher("/manager_statistics.jsp").forward(request, response);
        } catch (Exception e) {
            getServletContext().log("Failed to load manager statistics.", e);
            throw new ServletException("Failed to load manager statistics.", e);
        }
    }
    
    private Users getCurrentManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Users currentUser = session == null ? null : (Users) session.getAttribute("user");
        if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        return currentUser;
    }
}
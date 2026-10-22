package com.movieweb.servlet;

import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.DAO.PaymentsDAO;
import com.movieweb.DAO.RoomsDAO;
import com.movieweb.DAO.ShowtimesDAO;
import com.movieweb.model.Users;
import com.movieweb.DAO.ManagerStatisticsDAO;
import com.movieweb.model.ManagerStatistics;
import com.movieweb.model.ManagerMovieStatistic;


import java.io.IOException;
import java.sql.SQLException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/manager-dashboard")
public class ManagerDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Users currentUser = session == null ? null : (Users) session.getAttribute("user");
        if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        Integer theaterId = (Integer) request.getAttribute("currentTheaterId");
        if (theaterId == null && session != null) {
            theaterId = (Integer) session.getAttribute("currentTheaterId");
        }
        int todayBookings = 0;
        long todayRevenue = 0L;
        int upcomingShowtimeCount = 0;
        int activeRoomCount = 0;
        List<Map<String, Object>> dashboardShowtimes = Collections.emptyList();
        List<Map<String, Object>> recentBookings = Collections.emptyList();
        List<Map<String, Object>> dashboardRooms = Collections.emptyList();
		ManagerStatistics dashboardStatistics = new ManagerStatistics();
		ManagerMovieStatistic dashboardTopMovie = null;
		boolean dashboardStatisticsAvailable = false;
        if (theaterId != null && theaterId > 0) {
            LocalDate today = LocalDate.now();
            Booking_showtimesDAO bookingDAO = new Booking_showtimesDAO();
            PaymentsDAO paymentsDAO = new PaymentsDAO();
            ShowtimesDAO showtimesDAO = new ShowtimesDAO();
            RoomsDAO roomsDAO = new RoomsDAO();
            todayBookings = bookingDAO.getTodayConfirmedBookingCountByTheaterId(theaterId);
            try {
                todayRevenue = paymentsDAO.getDailyRevenueByTheaterId(theaterId);
            } catch (SQLException e) {
                getServletContext().log("Failed to load today's revenue for theater " + theaterId, e);
            }
            upcomingShowtimeCount = showtimesDAO.countDashboardShowtimesByTheaterAndDate(theaterId, today);
            dashboardShowtimes = showtimesDAO.getDashboardShowtimesByTheaterAndDate(theaterId, today);
            recentBookings = bookingDAO.getRecentBookingsByTheaterId(theaterId);
            activeRoomCount = roomsDAO.countActiveRoomsByTheaterId(theaterId);
            dashboardRooms = roomsDAO.getDashboardRoomsByTheaterId(theaterId);
			LocalDate statisticsEndDate = LocalDate.now();
			LocalDate statisticsStartDate = statisticsEndDate.minusDays(6);
			ManagerStatisticsDAO statisticsDAO = new ManagerStatisticsDAO();	
			try {
			    dashboardStatistics = statisticsDAO.getSummaryByTheaterAndDateRange(
			            theaterId, statisticsStartDate, statisticsEndDate);
			    dashboardTopMovie = statisticsDAO.getTopSellingMovieByTheaterAndDateRange(
			            theaterId, statisticsStartDate, statisticsEndDate);
			    dashboardStatisticsAvailable = true;
			} 
			catch (SQLException e) {
			    getServletContext().log(
			        "Failed to load dashboard statistics for theater " + theaterId, e);
			}
        }
        request.setAttribute("todayBookings", todayBookings);
        request.setAttribute("todayRevenue", todayRevenue);
        request.setAttribute("upcomingShowtimeCount", upcomingShowtimeCount);
        request.setAttribute("activeRoomCount", activeRoomCount);
        request.setAttribute("dashboardShowtimes", dashboardShowtimes);
        request.setAttribute("recentBookings", recentBookings);
        request.setAttribute("dashboardRooms", dashboardRooms);
		request.setAttribute("dashboardStatistics", dashboardStatistics);
		request.setAttribute("dashboardTopMovie", dashboardTopMovie);
		request.setAttribute("dashboardStatisticsAvailable", dashboardStatisticsAvailable);
        request.getRequestDispatcher("/main_manager.jsp").forward(request, response);
    }
}
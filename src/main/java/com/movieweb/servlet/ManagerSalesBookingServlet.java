package com.movieweb.servlet;

import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.DAO.PaymentsDAO;
import com.movieweb.model.Users;
import com.movieweb.DAO.ManagersDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/sales-bookings")
public class ManagerSalesBookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");

        if (currentUser == null
                || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }

        Integer theaterId = (Integer) session.getAttribute("currentTheaterId");

        if (theaterId == null) {
            response.sendRedirect(request.getContextPath() + "/main_manager.jsp");
            return;
        }

        // Verify that this manager is assigned to the selected theater
        ManagersDAO managersDAO = new ManagersDAO();
        if (!managersDAO.isManagerAssignedToTheater(
                currentUser.getUserId(), theaterId)) {
            response.sendRedirect(request.getContextPath() + "/main_manager.jsp");
            return;
        }
        final int pageSize = 5;
        String transactionCode = request.getParameter("transactionCode");
        if (transactionCode == null) {
            transactionCode = "";
        }
        transactionCode = transactionCode.trim();
        int requestedPage = 1;
        try {
            requestedPage = Integer.parseInt(request.getParameter("page"));
        } 
        catch (NumberFormatException | NullPointerException e) {
            requestedPage = 1;
        }
        if (requestedPage < 1) {
            requestedPage = 1;
        }
        try {
            Booking_showtimesDAO bookingDAO = new Booking_showtimesDAO();
            PaymentsDAO paymentsDAO = new PaymentsDAO();
            int totalBookings = bookingDAO.countBookingHistoryByTheaterId(theaterId, transactionCode);
            int totalPages = (int) Math.ceil(totalBookings / (double) pageSize);

            // An empty result still uses page 1
            if (totalPages == 0) {
                totalPages = 1;
            }

            // Keep manually entered page numbers within valid bounds
            int currentPage = Math.min(requestedPage, totalPages);
            int offset = (currentPage - 1) * pageSize;
            List<Map<String, Object>> bookings = bookingDAO.getBookingHistoryByTheaterId(theaterId, transactionCode, offset, pageSize);
            int dailyRevenue = paymentsDAO.getDailyRevenueByTheaterId(theaterId);
            request.setAttribute("bookingHistory", bookings);
            request.setAttribute("dailyRevenue", dailyRevenue);
            request.setAttribute("transactionCode", transactionCode);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalBookings", totalBookings);
            request.setAttribute("totalPages", totalPages);
            request.getRequestDispatcher("/pos.jsp").forward(request, response);
        } 
        catch (SQLException e) {
            throw new ServletException("Unable to load sales and booking data.", e);
        }
    }
    
}
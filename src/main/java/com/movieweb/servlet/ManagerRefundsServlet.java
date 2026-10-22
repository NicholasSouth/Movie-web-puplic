package com.movieweb.servlet;

import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.DAO.ManagersDAO;
import com.movieweb.model.Users;

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

@WebServlet("/manager-refunds")
public class ManagerRefundsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 5;
    private final Booking_showtimesDAO bookingDAO = new Booking_showtimesDAO();
    private final ManagersDAO managersDAO = new ManagersDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        Users manager = (Users) session.getAttribute("user");
        if (manager == null
            || manager.getRole() == null
            || !"MANAGER".equalsIgnoreCase(manager.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Object theaterAttribute = session.getAttribute("currentTheaterId");
        if (!(theaterAttribute instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/main_manager.jsp");
            return;
        }
        int theaterId = (Integer) theaterAttribute;
        if (!managersDAO.isManagerAssignedToTheater(manager.getUserId(), theaterId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Read and validate the status filter.
        String selectedStatus = request.getParameter("status");
        if (!"REFUNDED".equals(selectedStatus)
            && !"CANCELLED".equals(selectedStatus)) {
            selectedStatus = "ALL";
        }

        // Read the transaction-code search.
        String transactionCode = request.getParameter("transactionCode");
        if (transactionCode == null) {
            transactionCode = "";
        }
        transactionCode = transactionCode.trim();

        // Read the requested page safely.
        int currentPage = 1;
        try {
            currentPage = Integer.parseInt(request.getParameter("page"));
        } 
        catch (NumberFormatException | NullPointerException e) {
            currentPage = 1;
        }
        if (currentPage < 1) {
            currentPage = 1;
        }
        try {
            int totalBookings = bookingDAO.countRefundHistoryByTheaterId(theaterId, selectedStatus, transactionCode);
            int totalPages = Math.max(1, (int) Math.ceil(totalBookings / (double) PAGE_SIZE));

            // Keep the page number within the available range.
            if (currentPage > totalPages) {
                currentPage = totalPages;
            }
            int offset = (currentPage - 1) * PAGE_SIZE;
            List<Map<String, Object>> refundHistory =
                    bookingDAO.getRefundHistoryByTheaterId(
                            theaterId,
                            selectedStatus,
                            transactionCode,
                            offset,
                            PAGE_SIZE);
            request.setAttribute("refundHistory", refundHistory);
            request.setAttribute("selectedStatus", selectedStatus);
            request.setAttribute("transactionCode", transactionCode);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalBookings", totalBookings);
            request.setAttribute("pageSize", PAGE_SIZE);
            request.setAttribute("currentManagerPage", "refunds");
            request.getRequestDispatcher("/pos_refund.jsp").forward(request, response);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("refundError", "Unable to load refund history. Please try again.");
            request.setAttribute("selectedStatus", selectedStatus);
            request.setAttribute("transactionCode", transactionCode);
            request.setAttribute("currentManagerPage", "refunds");
            request.getRequestDispatcher("/pos_refund.jsp").forward(request, response);
        }
    }
}
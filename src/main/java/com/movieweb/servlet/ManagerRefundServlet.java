/* Note that this process refund request in pos.jsp, not loading infor into pos_refund.jsp */
package com.movieweb.servlet;

import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.DAO.ManagersDAO;
import com.movieweb.model.Users;
import com.movieweb.service.EmailService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/manager-refund")
public class ManagerRefundServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Booking_showtimesDAO bookingDAO = new Booking_showtimesDAO();
    private final ManagersDAO managersDAO = new ManagersDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        Users manager = (Users) session.getAttribute("user");
        if (manager == null || manager.getRole() == null
            || !"MANAGER".equalsIgnoreCase(manager.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Object theaterAttribute = session.getAttribute("currentTheaterId");
        if (!(theaterAttribute instanceof Integer)) {
            redirectWithResult(request, response, "invalidTheater");
            return;
        }
        int theaterId = (Integer) theaterAttribute;
        if (!managersDAO.isManagerAssignedToTheater(manager.getUserId(), theaterId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int bookingId;
        try {
            bookingId = Integer.parseInt(request.getParameter("booking_id"));
            if (bookingId <= 0) {
                throw new NumberFormatException();
            }
        } 
        catch (NumberFormatException e) {
            redirectWithResult(request, response, "invalidBooking");
            return;
        }
        try {
            // Update only if the booking and payment satisfy
            // the refund requirements.
            boolean updated = bookingDAO.requestRefund(bookingId, theaterId);
            if (!updated) {
                redirectWithResult(request, response, "notEligible");
                return;
            }

            // The request is recorded. Now notify the website inbox.
            Map<String, Object> details = bookingDAO.getRefundEmailDetails(bookingId, theaterId);
            if (details == null) {
                redirectWithResult(request, response, "emailFailed");
                return;
            }
            boolean emailSent = emailService.sendRefundRequest(
                bookingId,
                (String) details.get("movieName"),
                (String) details.get("theaterName"),
                (String) details.get("roomName")
            );
            if (emailSent) {
                redirectWithResult(request, response, "success");
            } 
            else {
                // Booking is already REFUNDED; only email failed.
                redirectWithResult(request, response, "emailFailed");
            }

        } 
        catch (SQLException e) {
            e.printStackTrace();
            redirectWithResult(request, response, "error");
        }
    }

    private void redirectWithResult(
            HttpServletRequest request,
            HttpServletResponse response,
            String result) throws IOException {
        response.sendRedirect(request.getContextPath() + "/sales-bookings?refundResult=" + result);
    }
}
package com.movieweb.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminRefundsDAO;
import com.movieweb.DAO.AdminRefundsDAO.Outcome;
import com.movieweb.model.AdminRefundRow;
import com.movieweb.service.EmailService;

/* Admin refund review: lists pending requests (booking REFUNDED + payment SUCCESS) and approved refunds,
 * and approves or rejects a pending request. Authorization is enforced by AdminFilter, which also
 * provides the optional theater scope as request attribute adminTheaterId (null = all theaters).
 * Decisions are POST-only and PRG-redirected; the theater scope applies to the list only, because a
 * decision is addressed by booking id and is validated against the booking/payment state in the DAO.
 */
@WebServlet("/admin-refunds")
public class AdminRefundsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private final AdminRefundsDAO dao = new AdminRefundsDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = normalizeView(request.getParameter("view"));
        String search = clean(request.getParameter("q"));
        Integer theaterId = (Integer) request.getAttribute("adminTheaterId");
        int page = parseInt(request.getParameter("page"), 1);
        try {
            int total = dao.count(view, theaterId, search);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(Math.max(page, 1), pages);
            request.setAttribute("refundRows", dao.list(view, theaterId, search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalRefunds", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin refunds page", e);
            request.setAttribute("loadError", Boolean.TRUE); // never shown as an empty list
        }
        request.setAttribute("view", view);
        request.setAttribute("q", search);
        HttpSession session = request.getSession(false);
        if (session != null) { // one-time messages from the previous POST (PRG)
            request.setAttribute("flash", session.getAttribute("adminFlash"));
            request.setAttribute("flashError", session.getAttribute("adminFlashError"));
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_refunds.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        int bookingId = parseInt(request.getParameter("bookingId"), 0);
        String message = "Invalid request.";
        boolean error = true;
        if (bookingId > 0 && ("approve".equals(action) || "reject".equals(action))) {
            try {
                Outcome outcome = "approve".equals(action) ? dao.approve(bookingId) : dao.reject(bookingId);
                if (outcome == Outcome.DONE) {
                    error = false;
                    boolean approved = "approve".equals(action);
                    message = approved
                        ? "Refund approved for booking #" + bookingId + ": the booking is now CANCELLED and its payment REFUNDED."
                        : "Refund rejected for booking #" + bookingId + ": the booking is CONFIRMED again and the payment is unchanged.";
                    // The decision is already committed; a failed email must never undo or hide it.
                    message += " " + notifyCustomer(bookingId, approved);
                } else if (outcome == Outcome.SEAT_TAKEN) {
                    message = "Booking #" + bookingId + " cannot be restored: at least one of its seats was booked by someone else "
                            + "after the refund was requested. Nothing was changed; approve the refund instead.";
                } else {
                    message = "Nothing was changed: booking #" + bookingId
                            + " is not a pending refund request (it may already have been decided).";
                }
            } catch (SQLException e) {
                getServletContext().log("Admin refund action failed: " + action + " booking=" + bookingId, e);
                message = "The action failed because of a database error. Nothing was changed.";
            }
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        String q = clean(request.getParameter("q"));
        String back = request.getContextPath() + "/admin-refunds?view=" + normalizeView(request.getParameter("view"))
                + "&q=" + URLEncoder.encode(q, "UTF-8")
                + "&page=" + Math.max(parseInt(request.getParameter("page"), 1), 1);
        response.sendRedirect(back);
    }


    /** Emails the booking owner about the decision. Returns a short sentence for the admin; never throws. */
    private String notifyCustomer(int bookingId, boolean approved) {
        try {
            AdminRefundRow d = dao.getNotificationDetails(bookingId);
            if (d == null || d.getCustomerEmail() == null || d.getCustomerEmail().trim().isEmpty()) {
                return "No email was sent: the booking has no email address on record.";
            }
            boolean sent = emailService.sendRefundDecision(
                d.getCustomerEmail(), d.getCustomerName(), bookingId, d.getMovieName(), d.getTheaterName(),
                d.getRoomName(), d.getShowtimeStart(), d.getPaymentAmount(), d.getTransactionCode(), approved);
            return sent ? "The customer was notified by email."
                        : "The decision was saved, but the notification email could not be sent.";
        } catch (SQLException e) {
            getServletContext().log("Could not load refund email details for booking " + bookingId, e);
            return "The decision was saved, but the notification email could not be prepared.";
        }
    }

    private String normalizeView(String v) {
        return "APPROVED".equals(v) ? "APPROVED" : "PENDING";
    }

    private String clean(String s) {
        s = s == null ? "" : s.trim();
        return s.length() > 100 ? s.substring(0, 100) : s;
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

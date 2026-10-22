package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminTheaterRequestsDAO;
import com.movieweb.model.AdminTheaterRequestRow;
import com.movieweb.service.EmailService;
import com.movieweb.model.Users;

/* Admin review of manager theater requests. Authorization is enforced by AdminFilter. */
@WebServlet("/admin-theater-requests")
public class AdminTheaterRequestsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private static final int NOTE_MAX = 500;
    private final AdminTheaterRequestsDAO dao = new AdminTheaterRequestsDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = normalizeStatus(request.getParameter("status"));
        int page = parseInt(request.getParameter("page"), 1);
        try {
            int total = dao.count(status);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(Math.max(page, 1), pages);
            request.setAttribute("requests", dao.list(status, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalRequests", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);
            request.setAttribute("pendingCount", dao.count("PENDING"));
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin theater requests", e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        request.setAttribute("status", status);
        HttpSession session = request.getSession(false);
        if (session != null) { // flash message from the previous POST (PRG), same keys as the managers page
            request.setAttribute("flash", session.getAttribute("adminFlash"));
            request.setAttribute("flashError", session.getAttribute("adminFlashError"));
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_theater_requests.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Users admin = (Users) session.getAttribute("user");
        String action = request.getParameter("action");
        int requestId = parseInt(request.getParameter("requestId"), 0);
        String note = request.getParameter("reviewNote");
        note = note == null ? "" : note.trim();
        if (note.length() > NOTE_MAX) {
            note = note.substring(0, NOTE_MAX);
        }
        String reviewNote = note.isEmpty() ? null : note;
        String message;
        boolean error = false;
        if (admin == null || requestId <= 0 || action == null) {
            message = "Invalid request.";
            error = true;
        } else {
            try {
                if ("approve".equals(action)) {
                    int result = dao.approve(requestId, admin.getUserId(), reviewNote);
                    if (result == AdminTheaterRequestsDAO.APPROVED) {
                        message = "Request approved and applied." + emailManager(request, requestId, true);
                    } else if (result == AdminTheaterRequestsDAO.CANNOT_APPLY) {
                        error = true;
                        message = "The request could not be applied: the manager is no longer active, the theater "
                                + "was removed, or the assignment already (or no longer) exists. "
                                + "Nothing was changed; you can reject the request.";
                    } else {
                        error = true;
                        message = "Nothing was changed: the request was already handled or cancelled.";
                    }
                } else if ("reject".equals(action)) {
                    if (dao.reject(requestId, admin.getUserId(), reviewNote)) {
                        message = "Request rejected." + emailManager(request, requestId, false);
                    } else {
                        error = true;
                        message = "Nothing was changed: the request was already handled or cancelled.";
                    }
                } else {
                    error = true;
                    message = "Unknown action.";
                }
            } catch (SQLException e) {
                getServletContext().log("Admin theater request action failed: " + action + " request=" + requestId, e);
                error = true;
                message = "The action failed because of a database error.";
            }
        }
        session.setAttribute(error ? "adminFlashError" : "adminFlash", message);
        response.sendRedirect(request.getContextPath() + "/admin-theater-requests?status="
                + normalizeStatus(request.getParameter("status")));
    }

    /**
     * Emails the manager about the decision when the admin ticked "Email the manager".
     * The decision is already saved; a mail failure never undoes it, it is only reported.
     */
    private String emailManager(HttpServletRequest request, int requestId, boolean approved) {
        if (!"1".equals(request.getParameter("sendEmail"))) {
            return "";
        }
        try {
            AdminTheaterRequestRow row = dao.getById(requestId);
            if (row == null || row.getEmail() == null || row.getEmail().trim().isEmpty()) {
                return " The manager has no email address, so no email was sent.";
            }
            boolean sent = emailService.sendTheaterRequestDecision(row.getEmail(), row.getFullName(),
                    row.getTheaterName(), row.getRequestType(), approved, row.getReviewNote());
            return sent ? " Email sent to the manager." : " The email to the manager could not be sent.";
        } catch (SQLException e) {
            getServletContext().log("Failed to load request " + requestId + " for the decision email", e);
            return " The email to the manager could not be sent.";
        }
    }

    private String normalizeStatus(String s) {
        if ("PENDING".equals(s) || "APPROVED".equals(s) || "REJECTED".equals(s) || "CANCELLED".equals(s)) {
            return s;
        }
        return s == null ? "PENDING" : "ALL"; // first visit opens the pending queue
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.ManagerTheaterRequestsDAO;
import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;

/* Lets a manager request to be assigned to, or removed from, a theater.
 * Deliberately not mapped in ManagerTheaterFilter: that filter passes managers with no theater
 * through, but this page must also work for them (they need to request their first theater).
 */
@WebServlet("/manager-theater-requests")
public class ManagerTheaterRequestsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int HISTORY_LIMIT = 20;
    private static final int NOTE_MAX = 500;
    private final ManagerTheaterRequestsDAO dao = new ManagerTheaterRequestsDAO();
    private final UsersDAO usersDAO = new UsersDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users manager = requireActiveManager(request, response);
        if (manager == null) {
            return;
        }
        try {
            request.setAttribute("theaterOptions", dao.getTheaterOptions(manager.getUserId()));
            request.setAttribute("requestHistory", dao.getRequestsByUser(manager.getUserId(), HISTORY_LIMIT));
        } catch (SQLException e) {
            getServletContext().log("Failed to load theater requests for user " + manager.getUserId(), e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        HttpSession session = request.getSession(false);
        if (session != null) { // one-time message from the previous POST (PRG)
            request.setAttribute("flash", session.getAttribute("managerRequestFlash"));
            request.setAttribute("flashError", session.getAttribute("managerRequestFlashError"));
            session.removeAttribute("managerRequestFlash");
            session.removeAttribute("managerRequestFlashError");
        }
        request.setAttribute("currentManagerPage", "theater-requests");
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/manager_theater_requests.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users manager = requireActiveManager(request, response);
        if (manager == null) {
            return;
        }
        String action = request.getParameter("action");
        int theaterId = parseInt(request.getParameter("theaterId"), 0);
        int requestId = parseInt(request.getParameter("requestId"), 0);
        String note = request.getParameter("note");
        note = note == null ? "" : note.trim();
        if (note.length() > NOTE_MAX) {
            note = note.substring(0, NOTE_MAX);
        }
        String message;
        boolean error = false;
        try {
            boolean valid = true;
            boolean ok = false;
            message = "";
            if ("assign".equals(action) && theaterId > 0) {
                ok = dao.create(manager.getUserId(), theaterId, "ASSIGN", note.isEmpty() ? null : note);
                message = "Assignment request sent. An administrator will review it.";
            } else if ("remove".equals(action) && theaterId > 0) {
                ok = dao.create(manager.getUserId(), theaterId, "REMOVE", note.isEmpty() ? null : note);
                message = "Removal request sent. An administrator will review it.";
            } else if ("cancel".equals(action) && requestId > 0) {
                ok = dao.cancel(requestId, manager.getUserId());
                message = "Request cancelled.";
            } else {
                valid = false;
            }
            if (!valid) {
                error = true;
                message = "Invalid request.";
            } else if (!ok) {
                error = true;
                message = "Nothing was changed: the request is no longer valid "
                        + "(already pending, already assigned or removed, or the theater is unavailable).";
            }
        } catch (SQLException e) {
            getServletContext().log("Theater request action failed: " + action + " user=" + manager.getUserId(), e);
            error = true;
            message = "The action failed because of a database error.";
        }
        request.getSession().setAttribute(error ? "managerRequestFlashError" : "managerRequestFlash", message);
        response.sendRedirect(request.getContextPath() + "/manager-theater-requests");
    }

    /** Same checks as ManagerProfileServlet: logged in, MANAGER, and still an active account. */
    private Users requireActiveManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Users current = session == null ? null : (Users) session.getAttribute("user");
        if (current == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return null;
        }
        if (!"MANAGER".equalsIgnoreCase(current.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        Users fresh = usersDAO.getUserById(current.getUserId());
        if (fresh == null || !"MANAGER".equalsIgnoreCase(fresh.getRole()) || !fresh.isActive()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "This page is only available to active theater managers.");
            return null;
        }
        return fresh;
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminManagersDAO;
import com.movieweb.model.AdminManagerRow;

/* Manager approval and theater assignment. Authorization is enforced by AdminFilter. */
@WebServlet("/admin-managers")
public class AdminManagersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private final AdminManagersDAO dao = new AdminManagersDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = normalizeStatus(request.getParameter("status"));
        String search = request.getParameter("q");
        search = search == null ? "" : search.trim();
        if (search.length() > 100) {
            search = search.substring(0, 100);
        }
        int page = parseInt(request.getParameter("page"), 1);
        try {
            int total = dao.count(status, search);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(Math.max(page, 1), pages);
            request.setAttribute("managers", dao.list(status, search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalManagers", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);
            request.setAttribute("pendingCount", dao.count("PENDING", ""));

            int selectedId = parseInt(request.getParameter("userId"), 0);
            if (selectedId > 0) {
                AdminManagerRow selected = dao.getById(selectedId);
                request.setAttribute("selectedManager", selected);
                request.setAttribute("selectedTheaters",
                        selected == null ? Collections.emptyList() : dao.getAssignedTheaters(selectedId));
            }
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin managers page", e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        request.setAttribute("status", status);
        request.setAttribute("q", search);
        HttpSession session = request.getSession(false);
        if (session != null) { // flash message from the previous POST (PRG)
            request.setAttribute("flash", session.getAttribute("adminFlash"));
            request.setAttribute("flashError", session.getAttribute("adminFlashError"));
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_managers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        int userId = parseInt(request.getParameter("userId"), 0);
        int theaterId = parseInt(request.getParameter("theaterId"), 0);
        String message;
        boolean error = false;
        if (action == null || userId <= 0) {
            message = "Invalid request.";
            error = true;
        } else {
            try {
                boolean ok;
                switch (action) {
                    case "approve":    ok = dao.approve(userId);    message = "Manager approved. Assign a theater so they can start working."; break;
                    case "reject":     ok = dao.reject(userId);     message = "Application rejected and account removed."; break;
                    case "activate":   ok = dao.activate(userId);   message = "Manager account activated."; break;
                    case "deactivate": ok = dao.deactivate(userId); message = "Manager account deactivated."; break;
                    case "delete":     ok = dao.delete(userId);     message = "Manager account deleted."; break;
                    case "assign":
                        ok = theaterId > 0 && dao.assign(userId, theaterId);
                        message = "Theater assigned."; break;
                    case "unassign":
                        ok = theaterId > 0 && dao.unassign(userId, theaterId);
                        message = "Theater assignment removed."; break;
                    default:
                        ok = false; message = "Unknown action.";
                }
                if (!ok) {
                    error = true;
                    message = "Nothing was changed: the account is no longer in the required state, "
                            + "or the selection is invalid.";
                }
            } catch (SQLException e) {
                getServletContext().log("Admin manager action failed: " + action + " user=" + userId, e);
                error = true;
                message = "The action failed because of a database error.";
            }
        }
        HttpSession session = request.getSession();
        session.setAttribute(error ? "adminFlashError" : "adminFlash", message);
        String status = normalizeStatus(request.getParameter("status"));
        String back = request.getContextPath() + "/admin-managers?status=" + status;
        if (!"delete".equals(action) && !"reject".equals(action) && userId > 0) {
            back += "&userId=" + userId;
        }
        response.sendRedirect(back);
    }

    private String normalizeStatus(String s) {
        if ("PENDING".equals(s) || "ACTIVE".equals(s) || "DEACTIVATED".equals(s)) {
            return s;
        }
        return "ALL";
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

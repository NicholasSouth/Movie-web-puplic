package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminUsersDAO;

/* USER account administration (view, block, unblock). Authorization is enforced by AdminFilter. */
@WebServlet("/admin-users")
public class AdminUsersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private final AdminUsersDAO dao = new AdminUsersDAO();

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
            request.setAttribute("userRows", dao.list(status, search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);
            int id = parseInt(request.getParameter("id"), 0);
            if (id > 0) {
                request.setAttribute("selectedUser", dao.getById(id));
            }
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin users page", e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        request.setAttribute("status", status);
        request.setAttribute("q", search);
        HttpSession session = request.getSession(false);
        if (session != null) {
            request.setAttribute("flash", session.getAttribute("adminFlash"));
            request.setAttribute("flashError", session.getAttribute("adminFlashError"));
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        int userId = parseInt(request.getParameter("userId"), 0);
        String message = "Invalid request.";
        boolean error = true;
        if (userId > 0 && action != null) {
            try {
                if ("block".equals(action)) {
                    if (dao.block(userId)) {
                        message = "User blocked. They can no longer log in; a session that is already open ends when it expires.";
                        error = false;
                    } else {
                        message = "Nothing was changed: the account is already blocked, closed, or not a user account.";
                    }
                } else if ("unblock".equals(action)) {
                    if (dao.unblock(userId)) {
                        message = "User unblocked.";
                        error = false;
                    } else {
                        message = "Nothing was changed: the account is not blocked, or it was closed.";
                    }
                }
            } catch (SQLException e) {
                getServletContext().log("Admin user action failed: " + action + " user=" + userId, e);
                message = "The action failed because of a database error.";
            }
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        String back = request.getContextPath() + "/admin-users?status=" + normalizeStatus(request.getParameter("status"));
        if (userId > 0) {
            back += "&id=" + userId;
        }
        response.sendRedirect(back);
    }

    private String normalizeStatus(String s) {
        return "ACTIVE".equals(s) || "BLOCKED".equals(s) || "DELETED".equals(s) ? s : "ALL";
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

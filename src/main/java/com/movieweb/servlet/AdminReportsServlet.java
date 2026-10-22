package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminReportsDAO;
import com.movieweb.model.AdminReportRow;

/* Reported-comment queue: dismiss reports or remove the comment. Authorization is enforced by AdminFilter. */
@WebServlet("/admin-reports")
public class AdminReportsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private final AdminReportsDAO dao = new AdminReportsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("q");
        search = search == null ? "" : search.trim();
        if (search.length() > 100) {
            search = search.substring(0, 100);
        }
        int page = parseInt(request.getParameter("page"), 1);
        try {
            int total = dao.count(search);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(Math.max(page, 1), pages);
            request.setAttribute("reportRows", dao.list(search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalReported", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);
            int id = parseInt(request.getParameter("id"), 0);
            if (id > 0) {
                AdminReportRow selected = dao.getById(id);
                request.setAttribute("selectedReport", selected);
                if (selected != null) {
                    request.setAttribute("reporters", dao.getReporters(id));
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin reports page", e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        request.setAttribute("q", search);
        HttpSession session = request.getSession(false);
        if (session != null) {
            request.setAttribute("flash", session.getAttribute("adminFlash"));
            request.setAttribute("flashError", session.getAttribute("adminFlashError"));
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_reports.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        int commentId = parseInt(request.getParameter("commentId"), 0);
        String message = "Invalid request.";
        boolean error = true;
        if (commentId > 0 && action != null) {
            try {
                if ("dismiss".equals(action)) {
                    if (dao.dismissReports(commentId)) {
                        message = "Reports dismissed. The comment stays visible.";
                        error = false;
                    } else {
                        message = "Nothing was changed: this comment has no reports (already handled?).";
                    }
                } else if ("remove".equals(action)) {
                    if (dao.removeComment(commentId)) {
                        message = "Comment removed together with its reports.";
                        error = false;
                    } else {
                        message = "Nothing was changed: this comment has no reports or no longer exists (already handled?).";
                    }
                }
            } catch (SQLException e) {
                getServletContext().log("Admin report action failed: " + action + " comment=" + commentId, e);
                message = "The action failed because of a database error.";
            }
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        response.sendRedirect(request.getContextPath() + "/admin-reports");
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

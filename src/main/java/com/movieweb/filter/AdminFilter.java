package com.movieweb.filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminTheatersDAO;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

/* Guards every ADMIN page and provides the admin-wide theater context.
 * Unlike ManagerTheaterFilter it never checks manager assignment and it supports "All Theaters".
 * Request attributes: adminTheaters (List), adminTheaterId (Integer, null = all theaters).
 * Session attribute:  adminTheaterId (Integer, absent = all theaters).
 * A theater is chosen with ?theaterId=<id> or ?theaterId=all on any admin GET page.
 * Add each new admin URL to the list below as its servlet/JSP is created.
 */
@WebFilter({
    "/main_admin.jsp", "/admin-dashboard",
    "/admin-managers", "/admin-theaters",
    "/admin-users", "/admin-reports",
    "/admin-statistics", "/admin-promotions", "/admin-refunds",
    "/admin-theater-requests", "/admin-profile",
    "/admin-ads", "/admin-movies", "/admin-genres-tags"
})
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        Users currentUser = session == null ? null : (Users) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())
                || !currentUser.isActive()) {
            // Same behavior as ManagerTheaterFilter for unauthorized users.
            resp.sendRedirect(req.getContextPath() + "/main.jsp");
            return;
        }
        // The dashboard JSP must be reached through its servlet so its data is loaded.
        if ("/main_admin.jsp".equals(req.getServletPath())
                && req.getDispatcherType() == DispatcherType.REQUEST) {
            resp.sendRedirect(req.getContextPath() + "/admin-dashboard");
            return;
        }

        List<Theaters> theaters = null;
        Integer selected = (Integer) session.getAttribute("adminTheaterId");
        try {
            theaters = new AdminTheatersDAO().getSelectableTheaters();
            // Only read the parameter on GET so multipart POSTs (profile pictures) are left untouched.
            String param = "GET".equalsIgnoreCase(req.getMethod()) ? req.getParameter("theaterId") : null;
            if ("GET".equalsIgnoreCase(req.getMethod()) && param != null) {
                param = param.trim();
                if (param.equalsIgnoreCase("all") || param.isEmpty()) {
                    selected = null;
                } else {
                    try {
                        int requested = Integer.parseInt(param);
                        if (containsTheater(theaters, requested)) {
                            selected = requested;
                        }
                        // An unknown id is ignored: keep the previous valid selection.
                    } catch (NumberFormatException ignored) {
                        // Invalid input: keep the previous valid selection.
                    }
                }
            }
            // Drop a stale selection (theater since soft-deleted).
            if (selected != null && !containsTheater(theaters, selected)) {
                selected = null;
            }
        } catch (SQLException e) {
            req.getServletContext().log("AdminFilter: failed to load theaters", e);
            req.setAttribute("adminTheatersError", Boolean.TRUE);
            selected = null;
        }
        if (selected == null) {
            session.removeAttribute("adminTheaterId");
        } else {
            session.setAttribute("adminTheaterId", selected);
        }
        req.setAttribute("adminTheaters", theaters);
        req.setAttribute("adminTheaterId", selected);
        chain.doFilter(req, resp);
    }

    private boolean containsTheater(List<Theaters> theaters, int id) {
        for (Theaters t : theaters) {
            if (t.getTheater_id() == id) {
                return true;
            }
        }
        return false;
    }
}

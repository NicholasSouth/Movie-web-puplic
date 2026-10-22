package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminLabelsDAO;

/* Admin page for the movie genres and tags: searchable paged lists with usage counts, add, delete.
 * Authorization is enforced by AdminFilter. The Add/Edit Movie form and the Movies page filters read the
 * same Genres/Tags tables, so a change here shows up there immediately.
 * Flow: GET -> AdminLabelsDAO.list for both kinds -> admin_genres_tags.jsp
 *       POST action=add|delete, kind=genre|tag -> AdminLabelsDAO -> redirect back with a one-time message (PRG).
 */
@WebServlet("/admin-genres-tags")
public class AdminLabelsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    /* Keep at or below the real size of genre_name / tag_name. */
    private static final int NAME_MAX = 50;
    private static final int PAGE_SIZE = 10;

    private final AdminLabelsDAO dao = new AdminLabelsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("q");
        search = search == null ? "" : search.trim();
        if (search.length() > NAME_MAX) {
            search = search.substring(0, NAME_MAX);
        }
        request.setAttribute("q", search);
        try {
            // One search box filters both lists; they are paged independently: ?genrePage=N&tagPage=N
            loadPage(request, AdminLabelsDAO.GENRE, "genre", search);
            loadPage(request, AdminLabelsDAO.TAG, "tag", search);
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin genres and tags page", e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        HttpSession session = request.getSession(false);
        if (session != null) { // one-time messages from the previous POST (PRG)
            Object ok = session.getAttribute("adminFlash");
            Object err = session.getAttribute("adminFlashError");
            if (ok != null) request.setAttribute("flash", ok);
            if (err != null) request.setAttribute("flashError", err);
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_genres_tags.jsp").forward(request, response);
    }

    /* Sets <prefix>Rows, <prefix>Total, <prefix>Page and <prefix>Pages for one list. The page number is
     * clamped, so a page that no longer exists (for example after deleting the last row of it) shows the last one. */
    private void loadPage(HttpServletRequest request, String kind, String prefix, String search)
            throws SQLException {
        int total = dao.count(kind, search);
        int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(Math.max(parseInt(request.getParameter(prefix + "Page"), 1), 1), pages);
        request.setAttribute(prefix + "Rows", dao.list(kind, search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
        request.setAttribute(prefix + "Total", total);
        request.setAttribute(prefix + "Page", page);
        request.setAttribute(prefix + "Pages", pages);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String kind = request.getParameter("kind");
        boolean validKind = AdminLabelsDAO.GENRE.equals(kind) || AdminLabelsDAO.TAG.equals(kind);
        String label = AdminLabelsDAO.GENRE.equals(kind) ? "genre" : "tag";
        String message = "Invalid request.";
        boolean error = true;
        try {
            if (validKind && "add".equals(action)) {
                String name = request.getParameter("name");
                name = name == null ? "" : name.trim().replaceAll("\\s+", " ");
                if (name.isEmpty() || name.length() > NAME_MAX) {
                    message = "The " + label + " name is required and must not exceed " + NAME_MAX + " characters.";
                } else if (dao.add(kind, name)) {
                    message = "The " + label + " \"" + name + "\" was added.";
                    error = false;
                } else {
                    message = "A " + label + " named \"" + name + "\" already exists.";
                }
            } else if (validKind && "delete".equals(action)) {
                int id = parseInt(request.getParameter("id"), 0);
                int links = id > 0 ? dao.delete(kind, id) : -1;
                if (links < 0) {
                    message = "Nothing was changed: that " + label + " no longer exists.";
                } else {
                    message = "The " + label + " was deleted."
                            + (links > 0 ? " It was removed from " + links + " movie" + (links == 1 ? "" : "s") + "." : "");
                    error = false;
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Admin genre/tag action failed: " + action + " " + kind, e);
            message = "The action failed because of a database error. Nothing was changed.";
            error = true;
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        // Stay on the same search and the same pages of both lists.
        String q = request.getParameter("q");
        response.sendRedirect(request.getContextPath() + "/admin-genres-tags?q="
                + java.net.URLEncoder.encode(q == null ? "" : q.trim(), "UTF-8") + "&genrePage="
                + Math.max(parseInt(request.getParameter("genrePage"), 1), 1)
                + "&tagPage=" + Math.max(parseInt(request.getParameter("tagPage"), 1), 1));
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

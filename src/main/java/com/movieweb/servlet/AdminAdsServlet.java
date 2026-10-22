package com.movieweb.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.movieweb.DAO.AdminAdsDAO;
import com.movieweb.model.AdminAdRow;
import com.movieweb.service.AdImageService;

/* Admin advertisement management: list/search, create (with image upload), edit, activate/deactivate, delete.
 * Authorization is enforced by AdminFilter. Ads are global, so the admin theater selector is not used.
 * The ads are shown by components/right_sidebar.jsp on the customer pages (active, not deleted, inside
 * their start/end period).
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
@WebServlet("/admin-ads")
public class AdminAdsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private static final int NAME_MAX = 100;
    private static final int LINK_MAX = 2000; // ad_link is varchar(MAX); 2000 is a sensible URL limit

    private final AdminAdsDAO dao = new AdminAdsDAO();
    private AdImageService images;

    @Override
    public void init() throws ServletException {
        images = new AdImageService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = normalizeStatus(request.getParameter("status"));
        String search = request.getParameter("q");
        search = search == null ? "" : search.trim();
        if (search.length() > NAME_MAX) {
            search = search.substring(0, NAME_MAX);
        }
        int page = parseInt(request.getParameter("page"), 1);
        try {
            int total = dao.count(status, search);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(Math.max(page, 1), pages);
            request.setAttribute("adRows", dao.list(status, search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalAds", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);

            int editId = parseInt(request.getParameter("edit"), 0);
            if (request.getAttribute("form") != null) {
                // Re-displaying a form after failed validation (see doPost): keep it as is.
            } else if (editId > 0) {
                AdminAdRow row = dao.getById(editId);
                if (row == null) {
                    request.setAttribute("flashError", "That advertisement no longer exists.");
                } else {
                    request.setAttribute("form", toForm(row));
                    request.setAttribute("formMode", "edit");
                    request.setAttribute("formId", editId);
                    request.setAttribute("formImagePath", row.getImagePath());
                }
            } else if ("1".equals(request.getParameter("new"))) {
                Map<String, String> form = new LinkedHashMap<>();
                form.put("isActive", "true");
                request.setAttribute("form", form);
                request.setAttribute("formMode", "create");
            }
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin ads page", e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        request.setAttribute("status", status);
        request.setAttribute("q", search);
        HttpSession session = request.getSession(false);
        if (session != null) { // one-time messages from the previous POST (PRG)
            Object ok = session.getAttribute("adminFlash");
            Object err = session.getAttribute("adminFlashError");
            if (ok != null) request.setAttribute("flash", ok);
            if (err != null && request.getAttribute("flashError") == null) request.setAttribute("flashError", err);
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_ads.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        int id = parseInt(request.getParameter("id"), 0);
        String status = normalizeStatus(request.getParameter("status"));
        String q = request.getParameter("q");
        int page = parseInt(request.getParameter("page"), 1);
        String message = "Invalid request.";
        boolean error = true;
        try {
            if ("create".equals(action) || "update".equals(action)) {
                boolean creating = "create".equals(action);
                if (!creating && id <= 0) {
                    message = "Invalid request.";
                } else {
                    Map<String, String> form = readForm(request, creating);
                    Timestamp[] dates = new Timestamp[2];
                    String problem = validate(form, dates);
                    Part image = null;
                    if (problem == null) {
                        try {
                            image = request.getPart("image");
                        } catch (IllegalStateException e) {
                            problem = "The image is too large. Maximum size is 5 MB.";
                        }
                        if (image != null && image.getSize() == 0) {
                            image = null;
                        }
                        if (problem == null && creating && image == null) {
                            problem = "Please choose an image for the advertisement.";
                        }
                    }
                    AdminAdRow existing = creating ? null : dao.getById(id);
                    if (problem == null && !creating && existing == null) {
                        problem = "That advertisement no longer exists.";
                    }
                    String newPath = null;
                    if (problem == null && image != null) {
                        try {
                            newPath = images.save(image);
                        } catch (IllegalArgumentException e) {
                            problem = e.getMessage();
                        }
                    }
                    if (problem != null) {
                        // Validation failed: redisplay the form with what was typed (no redirect).
                        // The chosen file cannot be kept by the browser, so the admin picks it again.
                        request.setAttribute("form", form);
                        request.setAttribute("formMode", creating ? "create" : "edit");
                        request.setAttribute("formId", id);
                        if (existing != null) {
                            request.setAttribute("formImagePath", existing.getImagePath());
                        }
                        request.setAttribute("flashError", problem);
                        doGet(request, response);
                        return;
                    }
                    boolean saved = false;
                    try {
                        if (creating) {
                            saved = dao.create(form.get("name"), newPath, form.get("link"), dates[0], dates[1],
                                    "true".equals(form.get("isActive")));
                            message = "Advertisement created.";
                        } else {
                            saved = dao.update(id, form.get("name"), newPath, form.get("link"), dates[0], dates[1]);
                            message = "Advertisement updated.";
                        }
                    } finally {
                        if (!saved) {
                            images.deleteQuietly(newPath); // do not leave an unused upload behind
                        }
                    }
                    if (saved) {
                        error = false;
                        if (!creating && newPath != null && existing != null) {
                            images.deleteQuietly(existing.getImagePath()); // replaced image
                        }
                    } else {
                        message = "That advertisement no longer exists.";
                    }
                }
            } else if (id > 0 && ("activate".equals(action) || "deactivate".equals(action))) {
                boolean on = "activate".equals(action);
                if (dao.setActive(id, on)) {
                    message = on ? "Advertisement activated." : "Advertisement deactivated. It is no longer shown to customers.";
                    error = false;
                } else {
                    message = "Nothing was changed: the advertisement is already in that state or no longer exists.";
                }
            } else if (id > 0 && "delete".equals(action)) {
                if (dao.softDelete(id)) {
                    message = "Advertisement deleted.";
                    error = false;
                } else {
                    message = "Nothing was changed: the advertisement no longer exists.";
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Admin ad action failed: " + action + " id=" + id, e);
            message = "The action failed because of a database error.";
            error = true;
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        String back = request.getContextPath() + "/admin-ads?status=" + status
                + "&q=" + java.net.URLEncoder.encode(q == null ? "" : q.trim(), "UTF-8")
                + "&page=" + Math.max(page, 1);
        response.sendRedirect(back);
    }

    private Map<String, String> readForm(HttpServletRequest request, boolean creating) {
        Map<String, String> form = new LinkedHashMap<>();
        for (String key : new String[] {"name", "link", "startAt", "endAt"}) {
            String v = request.getParameter(key);
            form.put(key, v == null ? "" : v.trim());
        }
        if (creating) {
            form.put("isActive", request.getParameter("isActive") != null ? "true" : "false");
        }
        return form;
    }

    private Map<String, String> toForm(AdminAdRow r) {
        SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"); // <input type="datetime-local">
        Map<String, String> form = new LinkedHashMap<>();
        form.put("name", nz(r.getName()));
        form.put("link", nz(r.getLink()));
        form.put("startAt", r.getStartAt() == null ? "" : local.format(r.getStartAt()));
        form.put("endAt", r.getEndAt() == null ? "" : local.format(r.getEndAt()));
        return form;
    }

    /* Returns a problem message, or null when valid (then dates[0] = start, dates[1] = end; either may be null). */
    private String validate(Map<String, String> form, Timestamp[] dates) {
        String name = form.get("name");
        String link = form.get("link");
        if (name.isEmpty() || name.length() > NAME_MAX) {
            return "The name is required and must not exceed " + NAME_MAX + " characters.";
        }
        if (link.isEmpty() || link.length() > LINK_MAX) {
            return "The link is required and must not exceed " + LINK_MAX + " characters.";
        }
        if (!isHttpUrl(link)) {
            return "The link must be a full web address starting with http:// or https://.";
        }
        try {
            dates[0] = form.get("startAt").isEmpty() ? null : Timestamp.valueOf(LocalDateTime.parse(form.get("startAt")));
            dates[1] = form.get("endAt").isEmpty() ? null : Timestamp.valueOf(LocalDateTime.parse(form.get("endAt")));
        } catch (DateTimeParseException e) {
            return "Please enter a valid start and end date and time, or leave them empty.";
        }
        if (dates[0] != null && dates[1] != null && !dates[1].after(dates[0])) {
            return "The end time must be later than the start time.";
        }
        return null;
    }

    /* Only http(s) links are accepted, so a link can never run script (the sidebar puts it in an href). */
    private boolean isHttpUrl(String link) {
        try {
            URI uri = new URI(link);
            String scheme = uri.getScheme();
            return scheme != null
                && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String normalizeStatus(String s) {
        return "ACTIVE".equals(s) || "INACTIVE".equals(s) ? s : "ALL";
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

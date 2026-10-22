package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminManagersDAO;
import com.movieweb.DAO.AdminTheatersDAO;
import com.movieweb.model.Theaters;

/* Admin theater management: list, details, create, edit, activate/deactivate and manager assignment.
 * Authorization is enforced by AdminFilter. Parameter "id" is the theater id (not "theaterId",
 * which AdminFilter uses for the dashboard selector).
 */
@WebServlet("/admin-theaters")
public class AdminTheatersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private static final Pattern TIME = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$");
    // Matches the existing convention: a relative path under pictures/, e.g. pictures/theaters/galaxyqt.jpg
    private static final Pattern IMAGE_PATH = Pattern.compile("^pictures/[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)*/[A-Za-z0-9_\\-]+\\.(jpg|jpeg|png|webp)$");

    private final AdminTheatersDAO dao = new AdminTheatersDAO();
    private final AdminManagersDAO managersDao = new AdminManagersDAO();

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
            request.setAttribute("theaterRows", dao.list(status, search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalTheaters", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);

            int id = parseInt(request.getParameter("id"), 0);
            int editId = parseInt(request.getParameter("edit"), 0);
            if (request.getAttribute("form") != null) {
                // Re-displaying a form after failed validation (see doPost): keep it as is.
            } else if (id > 0) {
                loadDetails(request, id);
            } else if (editId > 0) {
                Theaters t = dao.getTheaterForEdit(editId);
                if (t == null) {
                    request.setAttribute("flashError", "That theater no longer exists.");
                } else if (request.getAttribute("form") == null) {
                    request.setAttribute("form", toForm(t));
                    request.setAttribute("formMode", "edit");
                    request.setAttribute("formId", editId);
                }
            } else if ("1".equals(request.getParameter("new")) && request.getAttribute("form") == null) {
                request.setAttribute("form", new LinkedHashMap<String, String>());
                request.setAttribute("formMode", "create");
            }
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin theaters page", e);
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
        request.getRequestDispatcher("/admin_theaters.jsp").forward(request, response);
    }

    private void loadDetails(HttpServletRequest request, int id) throws SQLException {
        request.setAttribute("selectedTheater", dao.getRowById(id));
        request.setAttribute("selectedTheaterFull", dao.getTheaterForEdit(id));
        request.setAttribute("assignedManagers", dao.getAssignedManagers(id));
        request.setAttribute("assignableManagers", dao.getAssignableManagers(id));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        int id = parseInt(request.getParameter("id"), 0);
        int userId = parseInt(request.getParameter("userId"), 0);
        String status = normalizeStatus(request.getParameter("status"));
        String message = "Invalid request.";
        boolean error = true;
        int redirectId = id;
        try {
            if ("create".equals(action) || "update".equals(action)) {
                boolean creating = "create".equals(action);
                if (!creating && id <= 0) {
                    message = "Invalid request.";
                } else {
                    Map<String, String> form = readForm(request);
                    Theaters t = new Theaters();
                    String problem = validate(form, t);
                    if (problem == null) {
                        t.setTheater_id(creating ? 0 : id);
                        if (dao.nameExists(t.getTheater_name(), creating ? 0 : id)) {
                            problem = "Another theater already uses this name.";
                        }
                    }
                    if (problem != null) {
                        // Validation failed: redisplay the form with what was typed (no redirect).
                        request.setAttribute("form", form);
                        request.setAttribute("formMode", creating ? "create" : "edit");
                        request.setAttribute("formId", id);
                        request.setAttribute("flashError", problem);
                        doGet(request, response);
                        return;
                    }
                    if (creating) {
                        redirectId = dao.create(t);
                        message = "Theater created. It is active; add rooms and assign a manager next.";
                        error = false;
                    } else if (dao.update(t)) {
                        message = "Theater updated.";
                        error = false;
                    } else {
                        message = "That theater no longer exists.";
                    }
                }
            } else if (id > 0 && ("activate".equals(action) || "deactivate".equals(action))) {
                boolean on = "activate".equals(action);
                if (dao.setActive(id, on)) {
                    message = on ? "Theater activated." : "Theater deactivated. Its rooms, showtimes and bookings were not changed.";
                    error = false;
                } else {
                    message = "Nothing was changed: the theater is already in that state or no longer exists.";
                }
            } else if (id > 0 && userId > 0 && "assignManager".equals(action)) {
                if (managersDao.assign(userId, id)) {
                    message = "Manager assigned.";
                    error = false;
                } else {
                    message = "Nothing was changed: the manager is not approved, already assigned, or no longer exists.";
                }
            } else if (id > 0 && userId > 0 && "unassignManager".equals(action)) {
                if (managersDao.unassign(userId, id)) {
                    message = "Manager assignment removed.";
                    error = false;
                } else {
                    message = "Nothing was changed: that assignment no longer exists.";
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Admin theater action failed: " + action + " id=" + id, e);
            message = "The action failed because of a database error.";
            error = true;
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        String back = request.getContextPath() + "/admin-theaters?status=" + status;
        if (redirectId > 0) {
            back += "&id=" + redirectId;
        }
        response.sendRedirect(back);
    }

    private Map<String, String> readForm(HttpServletRequest request) {
        Map<String, String> form = new LinkedHashMap<>();
        for (String key : new String[] {"name", "address", "imagePath", "description",
                "latitude", "longitude", "openTime", "closingTime"}) {
            String v = request.getParameter(key);
            form.put(key, v == null ? "" : v.trim());
        }
        return form;
    }
    private Map<String, String> toForm(Theaters t) {
        Map<String, String> form = new LinkedHashMap<>();
        SimpleDateFormat hm = new SimpleDateFormat("HH:mm");
        form.put("name", nz(t.getTheater_name()));
        form.put("address", nz(t.getTheater_address()));
        form.put("imagePath", nz(t.getTheater_image_path()));
        form.put("description", nz(t.getDescription()));
        form.put("latitude", String.valueOf(t.getLatitude()));
        form.put("longitude", String.valueOf(t.getLongtitude()));
        form.put("openTime", t.getOpen_time() == null ? "" : hm.format(t.getOpen_time()));
        form.put("closingTime", t.getClosing_time() == null ? "" : hm.format(t.getClosing_time()));
        return form;
    }

    /* Returns an error message, or null and fills {@code out} when the form is valid. */
    private String validate(Map<String, String> f, Theaters out) {
        String name = f.get("name");
        if (name.isEmpty() || name.length() > 100) return "Name is required (max 100 characters).";
        String address = f.get("address");
        if (address.isEmpty() || address.length() > 255) return "Address is required (max 255 characters).";
        String description = f.get("description");
        if (description.length() > 1000) return "Description is too long (max 1000 characters).";
        String image = f.get("imagePath");
        // Required: theaters.jsp prints the path without a null check, so an empty value would render ".../null".
        if (!IMAGE_PATH.matcher(image).matches() || image.length() > 255) {
            return "Image path must look like pictures/theaters/name.jpg (jpg, jpeg, png or webp).";
        }
        double lat, lng;
        try {
            lat = Double.parseDouble(f.get("latitude"));
            lng = Double.parseDouble(f.get("longitude"));
        } catch (NumberFormatException e) {
            return "Latitude and longitude must be numbers.";
        }
        if (Double.isNaN(lat) || Double.isNaN(lng) || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            return "Latitude must be between -90 and 90, and longitude between -180 and 180.";
        }
        String open = f.get("openTime");
        String close = f.get("closingTime");
        if (!TIME.matcher(open).matches() || !TIME.matcher(close).matches()) {
            return "Opening and closing times are required (HH:mm).";
        }
        out.setTheater_name(name);
        out.setTheater_address(address);
        out.setDescription(description.isEmpty() ? null : description);
        out.setTheater_image_path(image);
        out.setLatitude(lat);
        out.setLongtitude(lng);
        out.setOpen_time(Time.valueOf(open.length() == 5 ? open + ":00" : open));
        out.setClosing_time(Time.valueOf(close.length() == 5 ? close + ":00" : close));
        return null;
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

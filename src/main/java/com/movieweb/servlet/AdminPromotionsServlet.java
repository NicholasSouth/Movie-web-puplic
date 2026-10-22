package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.AdminPromotionsDAO;
import com.movieweb.DAO.PromotionsDAO;
import com.movieweb.model.AdminPromotionRow;
import com.movieweb.model.Promotions;

/* Admin promotion management: list/search, create, edit, activate/deactivate.
 * Authorization is enforced by AdminFilter. Promotions are global (all theaters), so the admin theater
 * selector is not used. */
@WebServlet("/admin-promotions")
public class AdminPromotionsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    // PricingService.parsePercent / parseFixedAmount only understand whole numbers, so decimals are rejected
    // here (they would silently give no discount). Max 5 characters because price_modify is varchar(5).
    private static final Pattern PRICE_MODIFY = Pattern.compile("^[+-]?\\d{1,4}%?$");

    private final AdminPromotionsDAO dao = new AdminPromotionsDAO();
    private final PromotionsDAO promotionsDao = new PromotionsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = normalizeStatus(request.getParameter("status"));
        String search = request.getParameter("q");
        search = search == null ? "" : search.trim();
        if (search.length() > 20) {
            search = search.substring(0, 20); // codes are at most 20 characters
        }
        int page = parseInt(request.getParameter("page"), 1);
        try {
            int total = dao.count(status, search);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(Math.max(page, 1), pages);
            request.setAttribute("promotionRows", dao.list(status, search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalPromotions", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);

            int editId = parseInt(request.getParameter("edit"), 0);
            if (request.getAttribute("form") != null) {
                // Re-displaying a form after failed validation (see doPost): keep it as is.
            } else if (editId > 0) {
                AdminPromotionRow row = dao.getById(editId);
                if (row == null) {
                    request.setAttribute("flashError", "That promotion no longer exists.");
                } else {
                    request.setAttribute("form", toForm(row));
                    request.setAttribute("formMode", "edit");
                    request.setAttribute("formId", editId);
                    request.setAttribute("formUsedCount", row.getUsedCount());
                }
            } else if ("1".equals(request.getParameter("new"))) {
                Map<String, String> form = new LinkedHashMap<>();
                form.put("isActive", "true");
                request.setAttribute("form", form);
                request.setAttribute("formMode", "create");
            }
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin promotions page", e);
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
        request.getRequestDispatcher("/admin_promotions.jsp").forward(request, response);
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
                    Promotions p = new Promotions();
                    String problem = validate(form, p);
                    if (problem == null && dao.codeExists(p.getPromotion_code(), creating ? 0 : id)) {
                        problem = "Another promotion already uses this code.";
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
                        p.setActive("true".equals(form.get("isActive")));
                        promotionsDao.createPromotion(p);
                        message = "Promotion created.";
                        error = false;
                    } else {
                        p.setPromotion_id(id);
                        if (dao.update(p)) {
                            message = "Promotion updated. Bookings that already used it keep the price they paid.";
                            error = false;
                        } else {
                            message = "That promotion no longer exists.";
                        }
                    }
                }
            } else if (id > 0 && ("activate".equals(action) || "deactivate".equals(action))) {
                boolean on = "activate".equals(action);
                if (dao.setActive(id, on)) {
                    message = on ? "Promotion activated." : "Promotion deactivated. Customers can no longer apply it; past bookings are unchanged.";
                    error = false;
                } else {
                    message = "Nothing was changed: the promotion is already in that state or no longer exists.";
                }
            }
        } catch (SQLException e) {
            getServletContext().log("Admin promotion action failed: " + action + " id=" + id, e);
            message = "The action failed because of a database error.";
            error = true;
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        String back = request.getContextPath() + "/admin-promotions?status=" + status
                + "&q=" + java.net.URLEncoder.encode(q == null ? "" : q.trim(), "UTF-8")
                + "&page=" + Math.max(page, 1);
        response.sendRedirect(back);
    }

    private Map<String, String> readForm(HttpServletRequest request, boolean creating) {
        Map<String, String> form = new LinkedHashMap<>();
        for (String key : new String[] {"code", "description", "priceModify", "startAt", "endAt", "usageLimit"}) {
            String v = request.getParameter(key);
            form.put(key, v == null ? "" : v.trim());
        }
        if (creating) {
            form.put("isActive", request.getParameter("isActive") != null ? "true" : "false");
        }
        return form;
    }
    private Map<String, String> toForm(AdminPromotionRow r) {
        SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"); // <input type="datetime-local">
        Map<String, String> form = new LinkedHashMap<>();
        form.put("code", nz(r.getCode()));
        form.put("description", nz(r.getDescription()));
        form.put("priceModify", nz(r.getPriceModify()));
        form.put("startAt", r.getStartAt() == null ? "" : local.format(r.getStartAt()));
        form.put("endAt", r.getEndAt() == null ? "" : local.format(r.getEndAt()));
        // A NULL usage_limit is stored as 0 and makes the code unusable, so leave the box empty to force a value.
        form.put("usageLimit", r.getUsageLimit() > 0 ? String.valueOf(r.getUsageLimit()) : "");
        return form;
    }

    /* Returns a problem message, or null when valid (then fills p; isActive is set by the caller). */
    private String validate(Map<String, String> form, Promotions p) {
        String code = form.get("code");
        String description = form.get("description");
        String priceModify = form.get("priceModify");
        if (code.isEmpty() || code.length() > 20) {
            return "The code is required and must not exceed 20 characters.";
        }
        if (code.matches(".*\\s.*")) {
            return "The code must not contain spaces.";
        }
        if (description.isEmpty() || description.length() > 255) {
            return "The description is required (up to 255 characters).";
        }
        if (!PRICE_MODIFY.matcher(priceModify).matches()) {
            return "Invalid price modifier. Use a whole number such as -20 (fixed, in thousands of VND) "
                 + "or a percentage such as -10%, at most 5 characters.";
        }
        int number = Integer.parseInt(priceModify.replace("%", "").replace("+", ""));
        if (number == 0) {
            return "The price modifier must not be 0.";
        }
        if (priceModify.endsWith("%") && (number < -100 || number > 100)) {
            return "A percentage modifier must be between -100% and 100%.";
        }
        Timestamp startAt;
        Timestamp endAt;
        try {
            startAt = Timestamp.valueOf(LocalDateTime.parse(form.get("startAt")));
            endAt = Timestamp.valueOf(LocalDateTime.parse(form.get("endAt")));
        } catch (DateTimeParseException e) {
            return "Please enter a valid start and end date and time.";
        }
        if (!endAt.after(startAt)) {
            return "The end time must be later than the start time.";
        }
        int usageLimit;
        try {
            usageLimit = Integer.parseInt(form.get("usageLimit"));
        } catch (NumberFormatException e) {
            return "Please enter a valid whole-number usage limit.";
        }
        if (usageLimit <= 0) {
            return "The usage limit must be greater than zero (the booking flow treats 0 as unusable).";
        }
        p.setPromotion_code(code);
        p.setDescription(description);
        p.setPrice_modify(priceModify);
        p.setStart_at(startAt);
        p.setEnd_at(endAt);
        p.setUsage_limit(usageLimit);
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

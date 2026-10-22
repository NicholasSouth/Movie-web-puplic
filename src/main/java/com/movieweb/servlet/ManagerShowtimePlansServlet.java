package com.movieweb.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.DAO.ShowtimePlansDAO;
import com.movieweb.model.Showtime_plans;
import com.movieweb.model.Users;

/**
 * History of budget plans.
 *   GET /manager-showtime-plans        list of plans of the current theater
 *   GET /manager-showtime-plans?id=N   one plan with all its showtimes
 */
@WebServlet("/manager-showtime-plans")
public class ManagerShowtimePlansServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ManagersDAO managersDAO = new ManagersDAO();
    private final ShowtimePlansDAO plansDAO = new ShowtimePlansDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user == null || !"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        Object success = session.getAttribute("plansSuccess");
        if (success != null) {
            request.setAttribute("successMessage", success);
            session.removeAttribute("plansSuccess");
        }
        Object flashError = session.getAttribute("plansError");
        if (flashError != null) {
            request.setAttribute("errorMessage", flashError);
            session.removeAttribute("plansError");
        }
        Integer theaterId = (Integer) request.getAttribute("currentTheaterId");
        if (theaterId == null) {
            request.getRequestDispatcher("/manager_showtime_plans.jsp").forward(request, response);
            return;
        }
        try {
            if (!managersDAO.isManagerAssignedToTheater(user.getUserId(), theaterId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to the selected theater.");
                return;
            }
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                int planId;
                try {
                    planId = Integer.parseInt(idParam.trim());
                } catch (NumberFormatException e) {
                    response.sendRedirect(request.getContextPath() + "/manager-showtime-plans");
                    return;
                }
                Showtime_plans plan = plansDAO.getPlan(planId, theaterId);   // theater-scoped
                if (plan == null) {
                    request.setAttribute("errorMessage", "That plan was not found in the selected theater.");
                    request.setAttribute("plans", plansDAO.getPlansByTheater(theaterId));
                } else {
                    request.setAttribute("plan", plan);
                    request.setAttribute("items", plansDAO.getPlanItems(planId));
                }
            } else {
                request.setAttribute("plans", plansDAO.getPlansByTheater(theaterId));
            }
            request.getRequestDispatcher("/manager_showtime_plans.jsp").forward(request, response);
        } catch (Exception e) {
            getServletContext().log("Failed to load showtime plans.", e);
            throw new ServletException("Failed to load showtime plans.", e);
        }
    }

    /** POST action=delete, planId=N, showtimeId=... : delete a plan and the picked showtimes; the rest stay. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user == null || !"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        String base = request.getContextPath() + "/manager-showtime-plans";
        Integer theaterId = (Integer) request.getAttribute("currentTheaterId");
        if (theaterId == null) {
            session.setAttribute("plansError", "No active theater is selected.");
            response.sendRedirect(base);
            return;
        }
        String redirect = base;
        try {
            if (!managersDAO.isManagerAssignedToTheater(user.getUserId(), theaterId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to the selected theater.");
                return;
            }
            if (!"delete".equals(request.getParameter("action"))) {
                throw new IllegalArgumentException("Invalid action.");
            }
            int planId;
            try {
                planId = Integer.parseInt(request.getParameter("planId").trim());
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid plan.");
            }
            Set<Integer> picked = new HashSet<>();
            String[] ids = request.getParameterValues("showtimeId");
            if (ids != null) {
                for (String id : ids) {
                    try {
                        picked.add(Integer.parseInt(id.trim()));
                    } catch (NumberFormatException ignored) {
                        // a tampered value is just ignored
                    }
                }
            }
            ShowtimePlansDAO.DeleteResult result = plansDAO.deletePlan(user.getUserId(), theaterId, planId, picked);

            StringBuilder message = new StringBuilder();
            message.append("Plan #").append(planId).append(" deleted. ")
                   .append(result.removedNow).append(" showtime(s) deleted");
            if (result.keptByChoice > 0) {
                message.append(", ").append(result.keptByChoice).append(" kept as you chose");
            }
            if (result.keptPaid > 0) {
                message.append(", ").append(result.keptPaid).append(" kept because they have paid bookings");
            }
            if (result.alreadyRemoved > 0) {
                message.append(", ").append(result.alreadyRemoved).append(" had already been removed by someone else");
            }
            session.setAttribute("plansSuccess", message.append(".").toString());
            redirect += "?id=" + planId;     // show the plan so the manager sees what was kept
        } catch (IllegalArgumentException e) {
            session.setAttribute("plansError", e.getMessage());
            String id = request.getParameter("planId");
            if (id != null && id.trim().matches("\\d{1,9}")) {
                redirect += "?id=" + id.trim();
            }
        } catch (Exception e) {
            getServletContext().log("Failed to delete showtime plan.", e);
            session.setAttribute("plansError", "The plan could not be deleted. Please try again.");
        }
        response.sendRedirect(redirect);
    }
}

package com.movieweb.filter;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

import java.io.IOException;
import java.util.List;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter({
    "/main_manager.jsp", "/manager-dashboard",//for main_manager.jsp
    "/sales-bookings", //for pos_create_booking.jsp
    "/manager-showtimes", //for manager_showtimes.jsp
    "/manager-showtime-planner", "/manager-showtime-plans", //for manager_showtime_planner.jsp and manager_showtime_plans.jsp
    "/manager-rooms", "/manager-seats", //for manager_rooms.jsp and manager_seats.jsp
    "/manager-statistics",
    "/manager-promotions", //for manager_promotions.jsp
    "/manager-create-booking", //for pos.jsp
    "/manager-seat-selection", //for pos_seat_selection.jsp
    "/manager-payment", //for pos_payment.jsp
    "/pos-payment-method", //for pos_payment_method.jsp
    "/manager-refunds" //for pos_refund.jsp
})
public class ManagerTheaterFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null
            || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/main.jsp");
            return;
        }
        // Dashboard data must be loaded by ManagerDashboardServlet.
        if ("/main_manager.jsp".equals(req.getServletPath())
                && req.getDispatcherType() == DispatcherType.REQUEST) {
            resp.sendRedirect(req.getContextPath() + "/manager-dashboard");
            return;
        }
        ManagersDAO managersDAO = new ManagersDAO();
        List<Theaters> theaters = managersDAO.getTheatersByManagerId(currentUser.getUserId());
        /* Manager must have at least one assigned active theater.*/
        if (theaters == null || theaters.isEmpty()) {
            req.setAttribute("theaters", theaters);
            req.setAttribute("currentTheaterId", null);
            chain.doFilter(req, resp);
            return;
        }
        Integer currentTheaterId = (Integer) session.getAttribute("currentTheaterId");
        /* No theater selected yet.
         * Automatically select the first theater assigned to this manager. */
        if (currentTheaterId == null) {
            currentTheaterId = theaters.get(0).getTheater_id();
            session.setAttribute("currentTheaterId", currentTheaterId);
        }

        /* Make sure the currently selected theater is still assigned to this manager. */
        boolean assigned = false;
        for (Theaters theater : theaters) {
            if (theater.getTheater_id() == currentTheaterId) {
                assigned = true;
                break;
            }
        }
        
        /* If the theater is no longer assigned,
         * automatically switch to the first available theater. */
        if (!assigned) {
            currentTheaterId = theaters.get(0).getTheater_id();
            session.setAttribute("currentTheaterId", currentTheaterId);
        }
        req.setAttribute("theaters", theaters);
        req.setAttribute("currentTheaterId", currentTheaterId);
        chain.doFilter(req, resp);
    }
}
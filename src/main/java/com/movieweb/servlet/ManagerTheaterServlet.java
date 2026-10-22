package com.movieweb.servlet;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/manager-theater")
public class ManagerTheaterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null
            || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        String theaterId = request.getParameter("theaterId");
        if (theaterId == null
            || theaterId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/main_manager.jsp");
            return;
        }
        try {
            int selectedTheaterId = Integer.parseInt(theaterId);
            ManagersDAO managersDAO = new ManagersDAO();
            List<Theaters> theaters = managersDAO.getTheatersByManagerId(currentUser.getUserId());
            boolean assigned = false;
            for (Theaters theater : theaters) {
                if (theater.getTheater_id() == selectedTheaterId) {
                    assigned = true;
                    break;
                }
            }

            /* Manager can only select their assigned theaters. */
            if (!assigned) {
                response.sendRedirect(request.getContextPath() + "/manager-dashboard");
                return;
            }
            session.setAttribute("currentTheaterId", selectedTheaterId);
            response.sendRedirect(request.getContextPath() + "/manager-dashboard");
        }
        catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/manager-dashboard");
        }
    }
}
package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;

@WebServlet("/manager-profile")
public class ManagerProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ManagersDAO managersDAO;
    private UsersDAO usersDAO;

    @Override
    public void init() throws ServletException {
        managersDAO = new ManagersDAO();
        usersDAO = new UsersDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Check the logged-in user.
        HttpSession session = request.getSession(false);
        Users currentUser = session == null
                ? null
                : (Users) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }

        // Only managers can access this page.
        if (!"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }

        // Reload the manager's account from the database.
        Users profileUser = usersDAO.getUserById(currentUser.getUserId());
        if (profileUser == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to load your profile. Please try again.");
            return;
        }

        // Ensure the account is still an active manager.
        if (!"MANAGER".equalsIgnoreCase(profileUser.getRole())
            || !profileUser.isActive()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "This page is only available to active theater managers.");
            return;
        }

        // Load active theaters assigned to this manager.
        List<Theaters> assignedTheaters = managersDAO.getTheatersByManagerId(profileUser.getUserId());

        // Prepare the exact attributes expected by manager_profile.jsp.
        session.setAttribute("user", profileUser);
        request.setAttribute("profileUser", profileUser);
        request.setAttribute("assignedTheaters", assignedTheaters);
        request.setAttribute("theaterLoadError", false);

        // Prevent cached profile information.
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/manager_profile.jsp").forward(request, response);
    }
}

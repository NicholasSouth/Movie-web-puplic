package com.movieweb.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;
import com.movieweb.service.EmailService;
import com.movieweb.service.MovieRequestService;

/**
 * A manager asks the administrator to add a movie. Only the movie name is required;
 * the request is sent as an e-mail to the PhnetPhlyx mailbox (like reports and refund requests),
 * with the manager's address as Reply-To.
 *
 * Deliberately not mapped in ManagerTheaterFilter: like the theater request page, it must also
 * work for a manager who has no theater yet.
 */
@WebServlet("/manager-movie-request")
public class ManagerMovieRequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final long COOLDOWN_MILLIS = 60_000;          // one request per minute per session
    private static final String CSRF_KEY = "movieRequestCsrf";
    private static final String LAST_SENT_KEY = "movieRequestLastSent";
    private static final String FLASH_KEY = "movieRequestFlash";

    private final UsersDAO usersDAO = new UsersDAO();
    private final MovieRequestService validator = new MovieRequestService();
    private final EmailService emailService = new EmailService();
    private final SecureRandom random = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users manager = requireActiveManager(request, response);
        if (manager == null) {
            return;
        }
        HttpSession session = request.getSession();
        request.setAttribute("flash", session.getAttribute(FLASH_KEY));      // one-time message (PRG)
        session.removeAttribute(FLASH_KEY);
        showForm(request, response, manager, new LinkedHashMap<String, String>(), null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users manager = requireActiveManager(request, response);
        if (manager == null) {
            return;
        }
        HttpSession session = request.getSession();

        if (!validCsrf(session, request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token.");
            return;
        }

        Map<String, String> raw = new LinkedHashMap<>();
        for (String field : MovieRequestService.FIELDS) {
            raw.put(field, request.getParameter(field));
        }
        MovieRequestService.Result result = validator.validate(raw);

        if (!result.isValid()) {
            showForm(request, response, manager, result.form, result.errors);
            return;
        }

        Object last = session.getAttribute(LAST_SENT_KEY);
        if (last instanceof Long) {
            long wait = COOLDOWN_MILLIS - (System.currentTimeMillis() - (Long) last);
            if (wait > 0) {
                showForm(request, response, manager, result.form, java.util.Collections.singletonList(
                        "Please wait " + ((wait + 999) / 1000) + " second(s) before sending another request."));
                return;
            }
        }

        boolean sent = emailService.sendMovieRequest(
                manager.getUserId(), manager.getUsername(), manager.getFullName(),
                manager.getEmail(), manager.getPhone(), result.data);
        if (!sent) {
            getServletContext().log("Movie request e-mail failed for manager " + manager.getUserId());
            showForm(request, response, manager, result.form, java.util.Collections.singletonList(
                    "The request could not be sent right now. Your answers are kept; please try again in a moment."));
            return;
        }

        session.setAttribute(LAST_SENT_KEY, System.currentTimeMillis());
        session.setAttribute(FLASH_KEY, "Your request for \"" + result.data.getName()
                + "\" was sent to the administrator.");
        response.sendRedirect(request.getContextPath() + "/manager-movie-request");
    }

    // -------------------------------------------------------------- helpers

    private void showForm(HttpServletRequest request, HttpServletResponse response, Users manager,
                          Map<String, String> form, java.util.List<String> errors)
            throws ServletException, IOException {
        request.setAttribute("form", form);
        request.setAttribute("errors", errors);
        request.setAttribute("managerEmail", manager.getEmail());
        request.setAttribute("csrfToken", csrfToken(request.getSession()));
        request.setAttribute("currentManagerPage", "movie-request");
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/manager_movie_request.jsp").forward(request, response);
    }

    private String csrfToken(HttpSession session) {
        Object token = session.getAttribute(CSRF_KEY);
        if (token instanceof String) {
            return (String) token;
        }
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        session.setAttribute(CSRF_KEY, sb.toString());
        return sb.toString();
    }

    private boolean validCsrf(HttpSession session, String submitted) {
        Object expected = session.getAttribute(CSRF_KEY);
        if (!(expected instanceof String) || submitted == null) {
            return false;
        }
        return MessageDigest.isEqual(((String) expected).getBytes(), submitted.getBytes());
    }

    /** Same checks as ManagerTheaterRequestsServlet: logged in, MANAGER, and still an active account. */
    private Users requireActiveManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Users current = session == null ? null : (Users) session.getAttribute("user");
        if (current == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return null;
        }
        if (!"MANAGER".equalsIgnoreCase(current.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        Users fresh = usersDAO.getUserById(current.getUserId());
        if (fresh == null || !"MANAGER".equalsIgnoreCase(fresh.getRole()) || !fresh.isActive()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "This page is only available to active theater managers.");
            return null;
        }
        return fresh;
    }
}

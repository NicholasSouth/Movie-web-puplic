package com.movieweb.servlet;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.ManagersDAO;
import com.movieweb.DAO.MoviesDAO;
import com.movieweb.DAO.RoomsDAO;
import com.movieweb.DAO.ShowtimePlansDAO;
import com.movieweb.DAO.TheatersDAO;
import com.movieweb.model.Movies;
import com.movieweb.model.Rooms;
import com.movieweb.model.Theaters;
import com.movieweb.model.Users;
import com.movieweb.service.ShowtimePlanDraft;
import com.movieweb.service.ShowtimePlannerService;
import com.movieweb.service.ShowtimePlannerService.Params;
import com.movieweb.service.ShowtimePlannerService.Result;
import com.movieweb.service.ShowtimePlannerService.RoomInfo;
import com.movieweb.service.ShowtimePlannerService.Slot;

/**
 * Budget-based showtime planner.
 *   GET  /manager-showtime-planner[?plan=token]   form (+ preview of the draft)
 *   POST action=generate                          build a draft, store it in the session
 *   POST action=confirm                           create the ticked showtimes + history
 *   POST action=discard                           throw the draft away
 */
@WebServlet("/manager-showtime-planner")
public class ManagerShowtimePlannerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DRAFT_KEY = "showtimePlanDraft";
    private static final long MAX_BUDGET = 9_000_000_000_000L;

    private final ManagersDAO managersDAO = new ManagersDAO();
    private final MoviesDAO moviesDAO = new MoviesDAO();
    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final TheatersDAO theatersDAO = new TheatersDAO();
    private final ShowtimePlansDAO plansDAO = new ShowtimePlansDAO();
    private final ShowtimePlannerService planner = new ShowtimePlannerService();

    // ------------------------------------------------------------------ GET

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users user = getCurrentManager(request, response);
        if (user == null) {
            return;
        }
        HttpSession session = request.getSession();
        moveMessage(session, "plannerError", request, "errorMessage");
        moveMessage(session, "plannerSuccess", request, "successMessage");

        Integer theaterId = (Integer) request.getAttribute("currentTheaterId");
        if (theaterId == null) {
            request.getRequestDispatcher("/manager_showtime_planner.jsp").forward(request, response);
            return;
        }
        try {
            if (!managersDAO.isManagerAssignedToTheater(user.getUserId(), theaterId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to the selected theater.");
                return;
            }
            List<Rooms> rooms = roomsDAO.getRoomsByTheaterId(theaterId);
            request.setAttribute("rooms", rooms);
            request.setAttribute("seatCounts", plansDAO.getActiveSeatCountsByTheater(theaterId));
            request.setAttribute("movies", moviesDAO.getSchedulableMovies());
            request.setAttribute("theater", theatersDAO.getTheaterByIdIncludingHidden(theaterId));

            ShowtimePlanDraft draft = (ShowtimePlanDraft) session.getAttribute(DRAFT_KEY);
            String token = request.getParameter("plan");
            if (draft != null && token != null && token.equals(draft.getToken())
                    && draft.getUserId() == user.getUserId() && draft.getTheaterId() == theaterId) {
                request.setAttribute("draft", draft);
            }
            request.getRequestDispatcher("/manager_showtime_planner.jsp").forward(request, response);
        } catch (Exception e) {
            getServletContext().log("Failed to load the showtime planner.", e);
            throw new ServletException("Failed to load the showtime planner.", e);
        }
    }

    // ----------------------------------------------------------------- POST

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = getCurrentManager(request, response);
        if (user == null) {
            return;
        }
        HttpSession session = request.getSession();
        Integer theaterId = (Integer) request.getAttribute("currentTheaterId");
        if (theaterId == null) {
            session.setAttribute("plannerError", "No active theater is selected.");
            response.sendRedirect(request.getContextPath() + "/manager-showtime-planner");
            return;
        }
        String redirect = request.getContextPath() + "/manager-showtime-planner";
        try {
            if (!managersDAO.isManagerAssignedToTheater(user.getUserId(), theaterId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not assigned to the selected theater.");
                return;
            }
            String action = request.getParameter("action");
            if ("generate".equals(action)) {
                ShowtimePlanDraft draft = generate(request, user, theaterId);
                session.setAttribute(DRAFT_KEY, draft);
                redirect += "?plan=" + draft.getToken();
            } else if ("confirm".equals(action)) {
                redirect = confirm(request, session, user, theaterId);
            } else if ("discard".equals(action)) {
                session.removeAttribute(DRAFT_KEY);
            } else {
                session.setAttribute("plannerError", "Invalid action.");
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("plannerError", e.getMessage());
            ShowtimePlanDraft draft = (ShowtimePlanDraft) session.getAttribute(DRAFT_KEY);
            if ("confirm".equals(request.getParameter("action")) && draft != null) {
                redirect += "?plan=" + draft.getToken();
            }
        } catch (Exception e) {
            getServletContext().log("Showtime planner failed.", e);
            session.setAttribute("plannerError", "The operation failed. Please check the information and try again.");
        }
        response.sendRedirect(redirect);
    }

    // ------------------------------------------------------------- generate

    private ShowtimePlanDraft generate(HttpServletRequest request, Users user, int theaterId) throws Exception {
        Params p = new Params();

        // Movie
        int movieId = parsePositiveInt(request.getParameter("movieId"), "Please select a movie.");
        Movies movie = moviesDAO.getMovieById(movieId);          // active, not deleted
        if (movie == null) {
            throw new IllegalArgumentException("The selected movie could not be found.");
        }
        p.movieId = movieId;
        p.movieName = movie.getMovie_name();
        p.durationMinutes = movie.getDuration_minute();
        p.movieFrom = movie.getAvailable_from() == null ? null : movie.getAvailable_from().toLocalDateTime().toLocalDate();
        p.movieUntil = movie.getAvailable_until() == null ? null : movie.getAvailable_until().toLocalDateTime().toLocalDate();

        // Money
        p.budget = parseMoney(request.getParameter("budget"), "Please enter the budget (VND).");
        if (p.budget > MAX_BUDGET) {
            throw new IllegalArgumentException("The budget is too large.");
        }
        long price = parseMoney(request.getParameter("price"), "Please enter the original ticket price (VND).");
        if (price > 100_000_000L) {
            throw new IllegalArgumentException("The ticket price is too large.");
        }
        p.basePrice = (int) price;

        // Period and rules
        p.dateFrom = parseDate(request.getParameter("dateFrom"), "Please choose the first day.");
        p.dateTo = parseDate(request.getParameter("dateTo"), "Please choose the last day.");
        if (p.dateTo.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The period is already over.");
        }
        p.gapMinutes = parseIntOr(request.getParameter("gapMinutes"), 20);
        p.earliest = parseTimeOrNull(request.getParameter("earliest"));
        p.latest = parseTimeOrNull(request.getParameter("latest"));
        String client = request.getParameter("clientName");
        p.clientName = client == null || client.trim().isEmpty() ? null : client.trim();
        if (p.clientName != null && p.clientName.length() > 100) {
            throw new IllegalArgumentException("The author / sponsor name is too long (max 100 characters).");
        }

        // Rooms: only rooms of THIS theater can be used.
        Map<Integer, Integer> seatCounts = plansDAO.getActiveSeatCountsByTheater(theaterId);
        Map<Integer, String> roomNames = new HashMap<>();
        for (Rooms r : roomsDAO.getRoomsByTheaterId(theaterId)) {
            roomNames.put(r.getRoom_id(), r.getRoom_name());
        }
        List<RoomInfo> rooms = new ArrayList<>();
        String[] roomParams = request.getParameterValues("roomIds");
        Set<Integer> seen = new HashSet<>();
        if (roomParams != null) {
            for (String value : roomParams) {
                int id = parsePositiveInt(value, "Invalid room.");
                if (!roomNames.containsKey(id)) {
                    throw new IllegalArgumentException("A selected room does not belong to this theater.");
                }
                if (seen.add(id)) {
                    rooms.add(new RoomInfo(id, roomNames.get(id), seatCounts.getOrDefault(id, 0)));
                    p.roomIds.add(id);
                }
            }
        }
        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one room.");
        }

        Theaters theater = theatersDAO.getTheaterByIdIncludingHidden(theaterId);
        Time open = theater == null ? null : theater.getOpen_time();
        Time close = theater == null ? null : theater.getClosing_time();
        LocalTime theaterOpen = open == null ? LocalTime.of(0, 0) : open.toLocalTime();
        LocalTime theaterClose = close == null ? LocalTime.of(23, 59) : close.toLocalTime();

        Map<Integer, List<LocalDateTime[]>> busy = plansDAO.getBusyIntervals(
                p.roomIds, p.dateFrom.minusDays(1).atStartOfDay(), p.dateTo.plusDays(2).atStartOfDay());

        Result result = planner.generate(p, rooms, busy, LocalDateTime.now(), theaterOpen, theaterClose);

        ShowtimePlanDraft draft = new ShowtimePlanDraft();
        draft.setToken(UUID.randomUUID().toString());
        draft.setUserId(user.getUserId());
        draft.setTheaterId(theaterId);
        draft.setParams(p);
        draft.setSlots(result.slots);
        draft.setWarnings(result.warnings);
        return draft;
    }

    // -------------------------------------------------------------- confirm

    /** @return the URL to redirect to */
    private String confirm(HttpServletRequest request, HttpSession session, Users user, int theaterId) throws Exception {
        ShowtimePlanDraft draft = (ShowtimePlanDraft) session.getAttribute(DRAFT_KEY);
        String token = request.getParameter("plan");
        if (draft == null || token == null || !token.equals(draft.getToken())
                || draft.getUserId() != user.getUserId() || draft.getTheaterId() != theaterId) {
            throw new IllegalArgumentException("This plan has expired. Please generate it again.");
        }
        Set<Integer> chosen = new HashSet<>();
        String[] ids = request.getParameterValues("slotId");
        if (ids != null) {
            Set<Integer> valid = new HashSet<>();
            for (Slot s : draft.getSlots()) {
                valid.add(s.getId());
            }
            for (String id : ids) {
                int value = parsePositiveOrZero(id);
                if (valid.contains(value)) {
                    chosen.add(value);
                }
            }
        }
        if (chosen.isEmpty()) {
            throw new IllegalArgumentException("Select at least one showtime to create.");
        }

        ShowtimePlansDAO.ConfirmResult result = plansDAO.confirmPlan(user.getUserId(), draft, chosen);
        session.removeAttribute(DRAFT_KEY);
        String message = result.created + " showtime(s) created.";
        if (result.skipped > 0) {
            message += " " + result.skipped + " skipped because the room was no longer free.";
        }
        session.setAttribute("plansSuccess", message);
        return request.getContextPath() + "/manager-showtime-plans?id=" + result.planId;
    }

    // -------------------------------------------------------------- helpers

    private void moveMessage(HttpSession session, String key, HttpServletRequest request, String attribute) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(attribute, value);
            session.removeAttribute(key);
        }
    }

    private int parsePositiveInt(String value, String message) {
        try {
            int n = Integer.parseInt(value == null ? "" : value.trim());
            if (n <= 0) {
                throw new IllegalArgumentException(message);
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private int parsePositiveOrZero(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parseIntOr(String value, int fallback) {
        try {
            return value == null || value.trim().isEmpty() ? fallback : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The gap must be a whole number of minutes.");
        }
    }

    /** Accepts "5000000000", "5,000,000,000" or "5.000.000.000" (VND has no decimals). */
    private long parseMoney(String value, String message) {
        String digits = value == null ? "" : value.replaceAll("[,.\\s_]", "");
        if (digits.isEmpty() || !digits.matches("\\d{1,15}")) {
            throw new IllegalArgumentException(message);
        }
        long n = Long.parseLong(digits);
        if (n <= 0) {
            throw new IllegalArgumentException(message);
        }
        return n;
    }

    private LocalDate parseDate(String value, String message) {
        try {
            return LocalDate.parse(value == null ? "" : value.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private LocalTime parseTimeOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid time.");
        }
    }

    private Users getCurrentManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user == null || !"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        return user;
    }
}

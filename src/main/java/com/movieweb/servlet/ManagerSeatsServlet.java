package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.Room_typesDAO;
import com.movieweb.DAO.RoomsDAO;
import com.movieweb.DAO.Seat_typesDAO;
import com.movieweb.DAO.SeatsDAO;
import com.movieweb.model.Rooms;
import com.movieweb.model.Seat_types;
import com.movieweb.model.Seats;
import com.movieweb.model.Users;

@WebServlet("/manager-seats")
public class ManagerSeatsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final SeatsDAO seatsDAO = new SeatsDAO();
    private final Seat_typesDAO seatTypesDAO = new Seat_typesDAO();
    private final Room_typesDAO roomTypesDAO = new Room_typesDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users user = getManager(request, response);
        if (user == null) {
            return;
        }

        request.setAttribute("currentManagerPage", "rooms");
        Integer theaterId = getCurrentTheaterId(request);
        Integer roomId = parseOptionalPositiveInt(request.getParameter("room_id"));
        if (theaterId == null || roomId == null) {
            response.sendRedirect(request.getContextPath() + "/manager-rooms");
            return;
        }

        try {
            Rooms room = findRoomInTheater(theaterId, roomId);
            if (room == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Room not found in the selected theater.");
                return;
            }

            List<Seats> seats = seatsDAO.getSeatsByRoomId(roomId);
            List<Seat_types> seatTypes = seatTypesDAO.getAllSeatTypes();
            request.setAttribute("room", room);
            request.setAttribute("seats", seats);
            request.setAttribute("seatTypes", seatTypes);
            request.setAttribute("roomTypes", roomTypesDAO.getAllRoomTypes());
            consumeFlash(request);
            request.getRequestDispatcher("/manager_seats.jsp").forward(request, response);
        } catch (SQLException e) {
            getServletContext().log("Unable to load manager seat management page.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load seat management.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = getManager(request, response);
        if (user == null) {
            return;
        }

        Integer theaterId = getCurrentTheaterId(request);
        Integer roomId = parseOptionalPositiveInt(request.getParameter("room_id"));
        if (theaterId == null || roomId == null) {
            setFlash(request, "error", "Invalid theater or room selection.");
            redirectToRooms(request, response);
            return;
        }

        String action = request.getParameter("action");
        try {
            Rooms room = findRoomInTheater(theaterId, roomId);
            if (room == null) {
                setFlash(request, "error", "The selected room does not belong to this theater.");
                redirectToRooms(request, response);
                return;
            }

            int managerId = user.getUserId();
            if ("generate".equals(action)) {
                int rowCount = parsePositiveInt(request.getParameter("row_count"), "row count");
                int columnCount = parsePositiveInt(request.getParameter("column_count"), "column count");
                int seatTypeId = parsePositiveInt(request.getParameter("seat_type_id"), "seat type");
                int createdCount = seatsDAO.generateSeats(
                        managerId, theaterId, roomId, rowCount, columnCount, seatTypeId);
                setFlash(request, "success", createdCount + " seats generated successfully.");
            } else if ("addSeat".equals(action)) {
                String seatRow = request.getParameter("seat_row");
                int seatCol = parsePositiveInt(request.getParameter("seat_col"), "seat column");
                int seatTypeId = parsePositiveInt(request.getParameter("seat_type_id"), "seat type");
                int newSeatId = seatsDAO.addSeat(
                        managerId, theaterId, roomId, seatRow, seatCol, seatTypeId);
                setFlash(request, "success", "Seat added successfully (ID " + newSeatId + ").");
            } else if ("updateType".equals(action)) {
                int seatId = parsePositiveInt(request.getParameter("seat_id"), "seat");
                int seatTypeId = parsePositiveInt(request.getParameter("seat_type_id"), "seat type");
                seatsDAO.updateSeatType(managerId, theaterId, roomId, seatId, seatTypeId);
                setFlash(request, "success", "Seat type updated successfully.");
            } else if ("activate".equals(action) || "deactivate".equals(action)) {
                int seatId = parsePositiveInt(request.getParameter("seat_id"), "seat");
                boolean active = "activate".equals(action);
                seatsDAO.setSeatActive(managerId, theaterId, roomId, seatId, active);
                setFlash(request, "success", active
                        ? "Seat activated successfully."
                        : "Seat deactivated successfully.");
            } else if ("delete".equals(action)) {
                int seatId = parsePositiveInt(request.getParameter("seat_id"), "seat");
                seatsDAO.softDeleteSeat(managerId, theaterId, roomId, seatId);
                setFlash(request, "success", "Seat deleted successfully.");
            } else {
                setFlash(request, "error", "Unknown seat action.");
            }
        } catch (IllegalArgumentException e) {
            setFlash(request, "error", e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Seat management operation failed.", e);
            String message = e.getMessage();
            setFlash(request, "error", message != null && !message.trim().isEmpty()
                    ? message : "The seat operation failed. Please try again.");
        } catch (Exception e) {
            getServletContext().log("Unexpected error in manager seat management.", e);
            setFlash(request, "error", "An unexpected error occurred. Please try again.");
        }
        redirectToSeats(request, response, roomId);
    }

    private Rooms findRoomInTheater(int theaterId, int roomId) throws SQLException {
        List<Rooms> rooms = roomsDAO.getManageableRoomsByTheaterId(theaterId);
        for (Rooms room : rooms) {
            if (room.getRoom_id() == roomId) {
                return room;
            }
        }
        return null;
    }

    private Integer getCurrentTheaterId(HttpServletRequest request) {
        Object value = request.getAttribute("currentTheaterId");
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return parseOptionalPositiveInt((String) value);
        }
        return null;
    }

    private Users getManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user == null || user.getRole() == null
                || !"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        return user;
    }

    private int parsePositiveInt(String value, String fieldName) {
        Integer number = parseOptionalPositiveInt(value);
        if (number == null) {
            throw new IllegalArgumentException("Please provide a valid " + fieldName + ".");
        }
        return number;
    }

    private Integer parseOptionalPositiveInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int number = Integer.parseInt(value.trim());
            return number > 0 ? number : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("managerSeatsFlashType", type);
        session.setAttribute("managerSeatsFlashMessage", message);
    }

    private void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        String type = (String) session.getAttribute("managerSeatsFlashType");
        String message = (String) session.getAttribute("managerSeatsFlashMessage");
        session.removeAttribute("managerSeatsFlashType");
        session.removeAttribute("managerSeatsFlashMessage");
        if (message != null) {
            if ("success".equals(type)) {
                request.setAttribute("successMessage", message);
            } else {
                request.setAttribute("errorMessage", message);
            }
        }
    }

    private void redirectToSeats(HttpServletRequest request, HttpServletResponse response, int roomId)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/manager-seats?room_id=" + roomId);
    }

    private void redirectToRooms(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/manager-rooms");
    }
}

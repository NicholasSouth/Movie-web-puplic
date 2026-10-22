package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.Room_typesDAO;
import com.movieweb.DAO.RoomsDAO;
import com.movieweb.model.Room_types;
import com.movieweb.model.Rooms;
import com.movieweb.model.Users;

@WebServlet("/manager-rooms")
public class ManagerRoomsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RoomsDAO roomsDAO = new RoomsDAO();
    private final Room_typesDAO roomTypesDAO = new Room_typesDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getManager(request, response);
        if (currentUser == null) {
            return;
        }
        request.setAttribute("currentManagerPage", "rooms");
        Integer theaterId = getCurrentTheaterId(request);
        List<Rooms> rooms = new ArrayList<>();
        List<Room_types> roomTypes = new ArrayList<>();
        try {
            roomTypes = roomTypesDAO.getAllRoomTypes();
            if (theaterId != null && theaterId > 0) {
                rooms = roomsDAO.getManageableRoomsByTheaterId(theaterId);
            } 
            else {
                request.setAttribute("errorMessage", "No active theater is selected or assigned to your account.");
            }
        } 
        catch (SQLException e) {
            getServletContext().log("Unable to load manager room management page.", e);
            request.setAttribute("errorMessage", "Unable to load rooms or room types. Please try again.");
        }
        request.setAttribute("rooms", rooms);
        request.setAttribute("roomTypes", roomTypes);
        request.getRequestDispatcher("/manager_rooms.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getManager(request, response);
        if (currentUser == null) {
            return;
        }
        request.setCharacterEncoding("UTF-8");
        Integer theaterId = getCurrentTheaterId(request);
        if (theaterId == null || theaterId <= 0) {
            setFlash(request, "error", "No active theater is selected or assigned to your account.");
            redirectToRooms(request, response);
            return;
        }
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            setFlash(request, "error", "Invalid room action.");
            redirectToRooms(request, response);
            return;
        }
        try {
            int managerId = currentUser.getUserId();
            switch (action.trim()) {
                case "add":
                    addRoom(request, managerId, theaterId);
                    setFlash(request, "success", "Room added successfully.");
                    break;
                case "update":
                    updateRoom(request, managerId, theaterId);
                    setFlash(request, "success", "Room updated successfully.");
                    break;
                case "activate":
                    changeRoomStatus(request, managerId, theaterId, true);
                    setFlash(request, "success", "Room activated successfully.");
                    break;
                case "deactivate":
                    changeRoomStatus(request, managerId, theaterId, false);
                    setFlash(request, "success", "Room deactivated successfully.");
                    break;
                case "delete":
                    deleteRoom(request, managerId, theaterId);
                    setFlash(request, "success", "Room deleted successfully.");
                    break;
                default:
                    setFlash(request, "error", "Unknown room action.");
                    break;
            }
        } 
        catch (IllegalArgumentException e) {
            setFlash(request, "error", e.getMessage());
        } 
        catch (SQLException e) {
            getServletContext().log("Room management action failed.", e);
            String message = e.getMessage();
            if (message != null && !message.trim().isEmpty()) {
                setFlash(request, "error", message);
            } 
            else {
                setFlash(request, "error", "The room operation failed. Please try again.");
            }
        } 
        catch (Exception e) {
            getServletContext().log("Unexpected error in manager room management.", e);
            setFlash(request, "error", "An unexpected error occurred. Please try again.");
        }
        redirectToRooms(request, response);
    }
    
    private void addRoom(HttpServletRequest request, int managerId, int theaterId) throws SQLException {
        String roomName = getRequiredRoomName(request);
        int roomTypeId = parsePositiveInt(request.getParameter("room_type_id"), "room type");
        Rooms room = new Rooms();
        room.setTheater_id(theaterId);
        room.setRoom_name(roomName);
        room.setRoom_type_id(roomTypeId);
        roomsDAO.createRoom(managerId, room);
    }
    
    private void updateRoom(HttpServletRequest request, int managerId, int theaterId) throws SQLException {
        int roomId = parsePositiveInt(request.getParameter("room_id"), "room");
        String roomName = getRequiredRoomName(request);
        int roomTypeId = parsePositiveInt(request.getParameter("room_type_id"), "room type");
        Rooms room = new Rooms();
        room.setRoom_id(roomId);
        room.setTheater_id(theaterId);
        room.setRoom_name(roomName);
        room.setRoom_type_id(roomTypeId);
        roomsDAO.updateRoom(managerId, theaterId, room);
    }
    
    private void changeRoomStatus(HttpServletRequest request, int managerId, int theaterId, boolean active) throws SQLException {
        int roomId = parsePositiveInt(request.getParameter("room_id"), "room");
        roomsDAO.setRoomActive(managerId, theaterId, roomId, active);
    }
    
    private void deleteRoom(HttpServletRequest request, int managerId, int theaterId) throws SQLException {
        int roomId = parsePositiveInt(request.getParameter("room_id"), "room");
        roomsDAO.softDeleteRoom(managerId, theaterId, roomId);
    }
    
    private String getRequiredRoomName(HttpServletRequest request) {
        String roomName = request.getParameter("room_name");
        if (roomName == null) {
            throw new IllegalArgumentException("Room name is required.");
        }
        roomName = roomName.trim();
        if (roomName.isEmpty()) {
            throw new IllegalArgumentException("Room name is required.");
        }
        if (roomName.length() > 100) {
            throw new IllegalArgumentException("Room name cannot exceed 100 characters.");
        }
        return roomName;
    }
    
    private int parsePositiveInt(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a valid " + fieldName + ".");
        }
        try {
            int number = Integer.parseInt(value.trim());
            if (number <= 0) {
                throw new IllegalArgumentException("Please select a valid " + fieldName + ".");
            }
            return number;
        } 
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please select a valid " + fieldName + ".");
        }
    }
    
    private Integer getCurrentTheaterId(HttpServletRequest request) {
        Object value = request.getAttribute("currentTheaterId");
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private Users getManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Users currentUser = session == null ? null : (Users) session.getAttribute("user");
        if (currentUser == null || currentUser.getRole() == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        return currentUser;
    }
    
    private void setFlash(HttpServletRequest request, String type, String message) {
        request.getSession().setAttribute("managerRoomsFlashType", type);
        request.getSession().setAttribute("managerRoomsFlashMessage", message);
    }
    
    private void redirectToRooms(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/manager-rooms");
    }
}
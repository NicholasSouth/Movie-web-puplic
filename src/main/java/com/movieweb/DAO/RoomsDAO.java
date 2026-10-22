package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Rooms;
import com.movieweb.model.Room_types;
import com.movieweb.util.DBConnection;

public class RoomsDAO {
    public Rooms getRoomById(int room_id) {
        String sql =
            "SELECT r.*, " +
            "       rt.room_type_name, " +
            "       rt.price_modify " +
            "FROM Rooms r " +
            "INNER JOIN Room_types rt " +
            "    ON r.room_type_id = rt.room_type_id " +
            "WHERE r.room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, room_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRoom(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Existing customer-facing query.
    // Only active, non-deleted rooms are returned.
    public List<Rooms> getRoomsByTheaterId(int theaterId) {
        List<Rooms> rooms = new ArrayList<>();
        String sql =
            "SELECT r.*, " +
            "       rt.room_type_name, " +
            "       rt.price_modify " +
            "FROM Rooms r " +
            "INNER JOIN Room_types rt " +
            "    ON r.room_type_id = rt.room_type_id " +
            "WHERE r.theater_id = ? " +
            "AND r.isActive = 1 " +
            "AND r.deleted_at IS NULL " +
            "ORDER BY r.room_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    // Management query: includes inactive rooms.
    // Soft-deleted rooms are excluded.
    public List<Rooms> getManageableRoomsByTheaterId(int theaterId)
            throws SQLException {
        List<Rooms> rooms = new ArrayList<>();
        String sql =
            "SELECT r.*, " +
            "       rt.room_type_name, " +
            "       rt.price_modify " +
            "FROM Rooms r " +
            "INNER JOIN Room_types rt " +
            "    ON r.room_type_id = rt.room_type_id " +
            "WHERE r.theater_id = ? " +
            "AND r.deleted_at IS NULL " +
            "ORDER BY r.room_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        }
        return rooms;
    }

    // Creates a room and returns its generated room_id.
    public int createRoom(int managerId, Rooms room)
            throws SQLException {
        if (room == null) {
            throw new SQLException("Room information is required.");
        }
        int theaterId = room.getTheater_id();
        int roomTypeId = room.getRoom_type_id();
        String roomName = room.getRoom_name() == null
            ? ""
            : room.getRoom_name().trim();
        if (theaterId <= 0) {
            throw new SQLException("Please select a valid theater.");
        }
        if (roomTypeId <= 0) {
            throw new SQLException("Please select a room type.");
        }
        if (roomName.isEmpty() || roomName.length() > 100) {
            throw new SQLException("Room name must contain between 1 and 100 characters.");
        }
        String duplicateSql =
            "SELECT 1 " +
            "FROM Rooms WITH (UPDLOCK, HOLDLOCK) " +
            "WHERE theater_id = ? " +
            "AND LOWER(LTRIM(RTRIM(room_name))) = LOWER(?) " +
            "AND deleted_at IS NULL";
        String insertSql =
            "INSERT INTO Rooms " +
            "    (theater_id, room_name, room_type_id, isActive) " +
            "VALUES (?, ?, ?, 1)";
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageTheater(conn, managerId, theaterId);
                try (PreparedStatement stmt = conn.prepareStatement(duplicateSql)) {
                    stmt.setInt(1, theaterId);
                    stmt.setString(2, roomName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException("A room with this name already exists in this theater.");
                        }
                    }
                }
                int roomId;
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, theaterId);
                    stmt.setString(2, roomName);
                    stmt.setInt(3, roomTypeId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to create the room.");
                    }
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("The new room ID could not be retrieved.");
                        }
                        roomId = keys.getInt(1);
                    }
                }
                conn.commit();
                return roomId;
            } 
            catch (SQLException e) {
                try {
                    conn.rollback();
                } 
                catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } 
            finally {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } 
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Updates a room's name and room type.
    // theaterId is supplied by the trusted manager context,
    // not accepted as proof of authorization from the form alone.
    public void updateRoom(int managerId, int theaterId, Rooms room)
            throws SQLException {
        if (room == null || room.getRoom_id() <= 0) {
            throw new SQLException("Invalid room information.");
        }
        String roomName = room.getRoom_name() == null
            ? ""
            : room.getRoom_name().trim();
        if (roomName.isEmpty() || roomName.length() > 100) {
            throw new SQLException("Room name must contain between 1 and 100 characters.");
        }
        if (room.getRoom_type_id() <= 0) {
            throw new SQLException("Please select a valid room type.");
        }
        String duplicateSql =
            "SELECT 1 " +
            "FROM Rooms WITH (UPDLOCK, HOLDLOCK) " +
            "WHERE theater_id = ? " +
            "AND room_id <> ? " +
            "AND LOWER(LTRIM(RTRIM(room_name))) = LOWER(?) " +
            "AND deleted_at IS NULL";
        String updateSql =
            "UPDATE Rooms " +
            "SET room_name = ?, room_type_id = ? " +
            "WHERE room_id = ? " +
            "AND theater_id = ? " +
            "AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageRoom(conn, managerId, theaterId, room.getRoom_id());
                try (PreparedStatement stmt = conn.prepareStatement(duplicateSql)) {
                    stmt.setInt(1, theaterId);
                    stmt.setInt(2, room.getRoom_id());
                    stmt.setString(3, roomName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException("A room with this name already exists in this theater.");
                        }
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, roomName);
                    stmt.setInt(2, room.getRoom_type_id());
                    stmt.setInt(3, room.getRoom_id());
                    stmt.setInt(4, theaterId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Room not found or no longer available for editing.");
                    }
                }
                conn.commit();
            } 
            catch (SQLException e) {
                try {
                    conn.rollback();
                } 
                catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } 
            finally {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } 
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Activates or deactivates a room.
    // Deactivation is blocked while relevant future showtimes exist.
    public void setRoomActive(int managerId, int theaterId, int roomId, boolean isActive) 
            		throws SQLException {
        String updateSql =
            "UPDATE Rooms " +
            "SET isActive = ? " +
            "WHERE room_id = ? " +
            "AND theater_id = ? " +
            "AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageRoom(conn, managerId, theaterId, roomId);
                if (!isActive) {
                    ensureNoRelevantFutureShowtimes(conn, roomId);
                }
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setBoolean(1, isActive);
                    stmt.setInt(2, roomId);
                    stmt.setInt(3, theaterId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Room not found or already deleted.");
                    }
                }
                conn.commit();
            } 
            catch (SQLException e) {
                try {
                    conn.rollback();
                } 
                catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } 
            finally {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } 
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Soft-deletes a room.
    public void softDeleteRoom(int managerId, int theaterId, int roomId) 
            		throws SQLException {
        String updateSql =
            "UPDATE Rooms " +
            "SET isActive = 0, deleted_at = GETDATE() " +
            "WHERE room_id = ? " +
            "AND theater_id = ? " +
            "AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageRoom(conn, managerId, theaterId, roomId);
                ensureNoRelevantFutureShowtimes(conn, roomId);
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setInt(1, roomId);
                    stmt.setInt(2, theaterId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Room not found or already deleted.");
                    }
                }
                conn.commit();
            } 
            catch (SQLException e) {
                try {
                    conn.rollback();
                } 
                catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;

            } 
            finally {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } 
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Confirms that the manager is assigned to an active theater.
    private void ensureManagerCanManageTheater(
            Connection conn,
            int managerId,
            int theaterId) throws SQLException {
        String sql =
            "SELECT 1 " +
            "FROM Managers m " +
            "INNER JOIN Theaters t " +
            "    ON t.theater_id = m.theater_id " +
            "WHERE m.user_id = ? " +
            "AND m.theater_id = ? " +
            "AND t.isActive = 1 " +
            "AND t.deleted_at IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("You are not assigned to an active theater.");
                }
            }
        }
    }

    // Confirms manager assignment, active theater status, room ownership, and that the room is not soft-deleted.
    private void ensureManagerCanManageRoom(
            Connection conn,
            int managerId,
            int theaterId,
            int roomId) throws SQLException {
        String sql =
            "SELECT 1 " +
            "FROM Managers m " +
            "INNER JOIN Theaters t " +
            "    ON t.theater_id = m.theater_id " +
            "INNER JOIN Rooms r " +
            "    ON r.theater_id = t.theater_id " +
            "WHERE m.user_id = ? " +
            "AND m.theater_id = ? " +
            "AND r.room_id = ? " +
            "AND t.isActive = 1 " +
            "AND t.deleted_at IS NULL " +
            "AND r.deleted_at IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, theaterId);
            stmt.setInt(3, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Room not found, deleted, or not managed by you.");
                }
            }
        }
    }

    // Protects existing schedules from room deactivation/deletion.
    // Canceled and completed showtimes do not block the operation.
    private void ensureNoRelevantFutureShowtimes(Connection conn, int roomId) 
            		throws SQLException {
        String sql =
            "SELECT TOP 1 showtime_id " +
            "FROM Showtimes " +
            "WHERE room_id = ? " +
            "AND deleted_at IS NULL " +
            "AND end_at >= GETDATE() " +
            "AND UPPER(status) NOT IN ('CANCELED', 'COMPLETED')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException(
                        "Cannot deactivate or delete this room while it has " +
                        "a future or ongoing showtime. Reschedule or cancel " +
                        "the showtime first."
                    );
                }
            }
        }
    }

    // Dashboard: number of active, non-deleted rooms in a theater.
    public int countActiveRoomsByTheaterId(int theaterId) {
        String sql =
            "SELECT COUNT(*) " +
            "FROM Rooms " +
            "WHERE theater_id = ? " +
            "AND isActive = 1 " +
            "AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Dashboard: five active rooms with their active, non-deleted seat counts.
    public List<java.util.Map<String, Object>> getDashboardRoomsByTheaterId(
            int theaterId) {
        List<java.util.Map<String, Object>> rooms = new ArrayList<>();
        String sql =
            "SELECT TOP 5 " +
            "r.room_id AS roomId, " +
            "r.room_name AS roomName, " +
            "(SELECT COUNT(*) " +
            " FROM Seats s " +
            " WHERE s.room_id = r.room_id " +
            " AND s.isActive = 1 " +
            " AND s.deleted_at IS NULL) AS seatCount " +
            "FROM Rooms r " +
            "WHERE r.theater_id = ? " +
            "AND r.isActive = 1 " +
            "AND r.deleted_at IS NULL " +
            "ORDER BY r.room_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                    row.put("roomId", rs.getInt("roomId"));
                    row.put("roomName", rs.getString("roomName"));
                    row.put("seatCount", rs.getInt("seatCount"));
                    rooms.add(row);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private Rooms mapRoom(ResultSet rs) throws SQLException {
        Rooms room = new Rooms();
        room.setRoom_id(rs.getInt("room_id"));
        room.setTheater_id(rs.getInt("theater_id"));
        room.setRoom_name(rs.getString("room_name"));
        room.setRoom_type_id(rs.getInt("room_type_id"));
        room.setActive(rs.getBoolean("isActive"));
        room.setDeleted_at(rs.getTimestamp("deleted_at"));
        Room_types roomType = new Room_types();
        roomType.setRoom_type_id(rs.getInt("room_type_id"));
        roomType.setRoom_type_name(rs.getString("room_type_name"));
        roomType.setPrice_modify(rs.getString("price_modify"));
        room.setRoomType(roomType);
        return room;
    }
}
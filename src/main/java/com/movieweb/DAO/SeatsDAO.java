package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Seats;
import com.movieweb.util.DBConnection;

public class SeatsDAO {
    private static final int HOLD_MINUTES = 15;
    private static final int MAX_ROWS = 26;
    private static final int MAX_COLUMNS = 100;

    public Seats getSeatById(int seat_id) {
        String sql = "SELECT * FROM Seats WHERE seat_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seat_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Seats seat = new Seats();
                    seat.setSeat_id(rs.getInt("seat_id"));
                    seat.setRoom_id(rs.getInt("room_id"));
                    seat.setSeat_row(rs.getString("seat_row"));
                    seat.setSeat_col(rs.getInt("seat_col"));
                    seat.setSeat_type_id(rs.getInt("seat_type_id"));
                    seat.setActive(rs.getBoolean("isActive"));
                    seat.setDeleted_at(rs.getTimestamp("deleted_at"));
                    return seat;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Customer/POS booking method: deliberately retains inactive seats so the UI can show them as unavailable.
    public List<Seats> getSeatsWithStatusByShowtime(int showtimeId) {
        List<Seats> seats = new ArrayList<>();
        String sql =
            "SELECT s.seat_id, s.room_id, s.seat_row, s.seat_col, " +
            "       s.seat_type_id, s.isActive, t.seat_type_name, " +
            "       t.price_modify AS seat_price_modify, " +
            "       CASE WHEN EXISTS (" +
            "           SELECT 1 FROM Booking_seats bs " +
            "           INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
            "           WHERE bs.seat_id = s.seat_id " +
            "             AND b.showtime_id = st.showtime_id " +
            "             AND b.delete_at IS NULL " +
            "             AND (b.status = 'CONFIRMED' OR (b.status = 'PENDING' " +
            "                  AND DATEADD(MINUTE, " + HOLD_MINUTES + ", b.book_at) > GETDATE()))" +
            "       ) THEN 1 ELSE 0 END AS is_booked " +
            "FROM Showtimes st " +
            "INNER JOIN Seats s ON s.room_id = st.room_id " +
            "INNER JOIN Seat_types t ON t.seat_type_id = s.seat_type_id " +
            "WHERE st.showtime_id = ? " +
            "  AND st.deleted_at IS NULL " +
            "  AND s.deleted_at IS NULL " +
            "  AND s.seat_row IS NOT NULL AND s.seat_row <> '' " +
            "  AND s.seat_col IS NOT NULL AND s.seat_col > 0 " +
            "ORDER BY s.seat_row ASC, s.seat_col ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showtimeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seats seat = new Seats();
                    seat.setSeat_id(rs.getInt("seat_id"));
                    seat.setRoom_id(rs.getInt("room_id"));
                    seat.setSeat_row(rs.getString("seat_row"));
                    seat.setSeat_col(rs.getInt("seat_col"));
                    seat.setSeat_type_id(rs.getInt("seat_type_id"));
                    seat.setSeat_type_name(rs.getString("seat_type_name"));
                    seat.setSeat_price_modify(rs.getString("seat_price_modify"));
                    seat.setActive(rs.getBoolean("isActive"));
                    seat.setBooked(rs.getInt("is_booked") == 1);
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    // Management query: returns active and inactive seats, but excludes soft-deleted seats.
    public List<Seats> getSeatsByRoomId(int roomId) throws SQLException {
        List<Seats> seats = new ArrayList<>();
        String sql =
            "SELECT s.*, st.seat_type_name, st.price_modify AS seat_price_modify " +
            "FROM Seats s INNER JOIN Seat_types st ON st.seat_type_id = s.seat_type_id " +
            "WHERE s.room_id = ? AND s.deleted_at IS NULL " +
            "ORDER BY s.seat_row ASC, s.seat_col ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seats seat = new Seats();
                    seat.setSeat_id(rs.getInt("seat_id"));
                    seat.setRoom_id(rs.getInt("room_id"));
                    seat.setSeat_row(rs.getString("seat_row"));
                    seat.setSeat_col(rs.getInt("seat_col"));
                    seat.setSeat_type_id(rs.getInt("seat_type_id"));
                    seat.setSeat_type_name(rs.getString("seat_type_name"));
                    seat.setSeat_price_modify(rs.getString("seat_price_modify"));
                    seat.setActive(rs.getBoolean("isActive"));
                    seat.setDeleted_at(rs.getTimestamp("deleted_at"));
                    seats.add(seat);
                }
            }
        }
        return seats;
    }

    public int generateSeats(int managerId, int theaterId, int roomId,
                             int rowCount, int columnCount, int seatTypeId) throws SQLException {
        if (rowCount < 1 || rowCount > MAX_ROWS) {
            throw new SQLException("Row count must be between 1 and " + MAX_ROWS + ".");
        }
        if (columnCount < 1 || columnCount > MAX_COLUMNS) {
            throw new SQLException("Column count must be between 1 and " + MAX_COLUMNS + ".");
        }
        if (seatTypeId <= 0) {
            throw new SQLException("Please select a valid seat type.");
        }

        String existingSeatsSql =
            "SELECT TOP 1 seat_id FROM Seats WITH (UPDLOCK, HOLDLOCK) WHERE room_id = ?";
        String insertSql =
            "INSERT INTO Seats (room_id, seat_row, seat_col, seat_type_id, isActive) " +
            "VALUES (?, ?, ?, ?, 1)";

        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageRoom(conn, managerId, theaterId, roomId);
                ensureSeatTypeExists(conn, seatTypeId);

                // Any previous record, including a soft-deleted one, means this room has been laid out before.
                try (PreparedStatement stmt = conn.prepareStatement(existingSeatsSql)) {
                    stmt.setInt(1, roomId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException("This room already has seat records. Full layout generation is only allowed for an empty room.");
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    for (int row = 0; row < rowCount; row++) {
                        String seatRow = String.valueOf((char) ('A' + row));
                        for (int col = 1; col <= columnCount; col++) {
                            stmt.setInt(1, roomId);
                            stmt.setString(2, seatRow);
                            stmt.setInt(3, col);
                            stmt.setInt(4, seatTypeId);
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
                conn.commit();
                return rowCount * columnCount;
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        }
    }

    // Adds a single seat. Deleted positions are intentionally not reusable, matching generateSeats().
    public int addSeat(int managerId, int theaterId, int roomId,
                       String seatRow, int seatCol, int seatTypeId) throws SQLException {
        String normalizedRow = normalizeSeatRow(seatRow);
        validateSeatColumn(seatCol);
        if (seatTypeId <= 0) {
            throw new SQLException("Please select a valid seat type.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageRoom(conn, managerId, theaterId, roomId);
                ensureSeatTypeExists(conn, seatTypeId);

                String duplicateSql =
                    "SELECT TOP 1 seat_id FROM Seats WITH (UPDLOCK, HOLDLOCK) " +
                    "WHERE room_id = ? AND seat_row = ? AND seat_col = ?";
                try (PreparedStatement stmt = conn.prepareStatement(duplicateSql)) {
                    stmt.setInt(1, roomId);
                    stmt.setString(2, normalizedRow);
                    stmt.setInt(3, seatCol);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException("Seat " + normalizedRow + seatCol +
                                " already exists in this room (including a previously deleted seat).");
                        }
                    }
                }

                String insertSql =
                    "INSERT INTO Seats (room_id, seat_row, seat_col, seat_type_id, isActive) " +
                    "VALUES (?, ?, ?, ?, 1)";
                int newSeatId;
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, new String[] { "seat_id" })) {
                    stmt.setInt(1, roomId);
                    stmt.setString(2, normalizedRow);
                    stmt.setInt(3, seatCol);
                    stmt.setInt(4, seatTypeId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("The seat could not be added.");
                    }
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("The seat was added, but its generated ID could not be retrieved.");
                        }
                        newSeatId = keys.getInt(1);
                    }
                }
                conn.commit();
                return newSeatId;
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        }
    }

    public void updateSeatType(int managerId, int theaterId, int roomId,
                               int seatId, int seatTypeId) throws SQLException {
        if (seatTypeId <= 0) {
            throw new SQLException("Please select a valid seat type.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageSeat(conn, managerId, theaterId, roomId, seatId);
                ensureSeatTypeExists(conn, seatTypeId);
                String sql = "UPDATE Seats SET seat_type_id = ? " +
                             "WHERE seat_id = ? AND room_id = ? AND deleted_at IS NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, seatTypeId);
                    stmt.setInt(2, seatId);
                    stmt.setInt(3, roomId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Seat not found or you cannot manage this seat.");
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        }
    }

    public void setSeatActive(int managerId, int theaterId, int roomId,
                              int seatId, boolean isActive) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageSeat(conn, managerId, theaterId, roomId, seatId);
                if (!isActive) {
                    ensureSeatHasNoRelevantBookings(conn, seatId);
                }
                String sql = "UPDATE Seats SET isActive = ? " +
                             "WHERE seat_id = ? AND room_id = ? AND deleted_at IS NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setBoolean(1, isActive);
                    stmt.setInt(2, seatId);
                    stmt.setInt(3, roomId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Seat not found or already deleted.");
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        }
    }

    public void softDeleteSeat(int managerId, int theaterId, int roomId, int seatId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                ensureManagerCanManageSeat(conn, managerId, theaterId, roomId, seatId);
                ensureSeatHasNoRelevantBookings(conn, seatId);
                String sql = "UPDATE Seats SET isActive = 0, deleted_at = GETDATE() " +
                             "WHERE seat_id = ? AND room_id = ? AND deleted_at IS NULL";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, seatId);
                    stmt.setInt(2, roomId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Seat not found or already deleted.");
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                rollback(conn, e);
                throw e;
            } finally {
                restoreAutoCommit(conn, originalAutoCommit);
            }
        }
    }

    private String normalizeSeatRow(String seatRow) throws SQLException {
        if (seatRow == null) {
            throw new SQLException("Please provide a seat row from A to Z.");
        }
        String row = seatRow.trim().toUpperCase();
        if (row.length() != 1 || row.charAt(0) < 'A' || row.charAt(0) > 'Z') {
            throw new SQLException("Seat row must be a single letter from A to Z.");
        }
        return row;
    }

    private void validateSeatColumn(int seatCol) throws SQLException {
        if (seatCol < 1 || seatCol > MAX_COLUMNS) {
            throw new SQLException("Seat column must be between 1 and " + MAX_COLUMNS + ".");
        }
    }

    private void ensureSeatTypeExists(Connection conn, int seatTypeId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM Seat_types WHERE seat_type_id = ?")) {
            stmt.setInt(1, seatTypeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("The selected seat type does not exist.");
                }
            }
        }
    }

    private void ensureManagerCanManageRoom(Connection conn, int managerId, int theaterId, int roomId) throws SQLException {
        String sql =
            "SELECT 1 FROM Rooms r " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "INNER JOIN Managers m ON m.theater_id = t.theater_id " +
            "WHERE r.room_id = ? AND r.theater_id = ? AND m.user_id = ? " +
            "AND t.isActive = 1 AND t.deleted_at IS NULL AND r.deleted_at IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, theaterId);
            stmt.setInt(3, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Room not found or not managed by you.");
                }
            }
        }
    }

    private void ensureManagerCanManageSeat(Connection conn, int managerId, int theaterId, int roomId, int seatId) throws SQLException {
        String sql =
            "SELECT 1 FROM Seats s " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "INNER JOIN Managers m ON m.theater_id = t.theater_id " +
            "WHERE s.seat_id = ? AND s.room_id = ? AND r.theater_id = ? AND m.user_id = ? " +
            "AND t.isActive = 1 AND t.deleted_at IS NULL " +
            "AND r.deleted_at IS NULL AND s.deleted_at IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seatId);
            stmt.setInt(2, roomId);
            stmt.setInt(3, theaterId);
            stmt.setInt(4, managerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Seat not found, deleted, or not managed by you.");
                }
            }
        }
    }

    private void ensureSeatHasNoRelevantBookings(Connection conn, int seatId) throws SQLException {
        String sql =
            "SELECT TOP 1 bs.booking_id FROM Booking_seats bs " +
            "INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
            "INNER JOIN Showtimes st ON st.showtime_id = b.showtime_id " +
            "WHERE bs.seat_id = ? AND b.delete_at IS NULL AND st.deleted_at IS NULL " +
            "AND st.end_at >= GETDATE() " +
            "AND UPPER(st.status) NOT IN ('CANCELED', 'COMPLETED') " +
            "AND (b.status = 'CONFIRMED' OR (b.status = 'PENDING' " +
            "AND DATEADD(MINUTE, " + HOLD_MINUTES + ", b.book_at) > GETDATE()))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("This seat has a confirmed booking or an active hold for a future or ongoing showtime. It cannot be deactivated or deleted yet.");
                }
            }
        }
    }

    private void rollback(Connection conn, SQLException originalError) {
        try {
            conn.rollback();
        } 
        catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    private void restoreAutoCommit(Connection conn, boolean originalAutoCommit) {
        try {
            conn.setAutoCommit(originalAutoCommit);
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.ManagerTheaterOption;
import com.movieweb.model.ManagerTheaterRequest;
import com.movieweb.util.DBConnection;

/* SQL for manager theater assign/remove requests. Methods throw SQLException so pages can show an error. */
public class ManagerTheaterRequestsDAO {

    /* Theaters the manager can ask about: all live active theaters, plus assigned ones even if inactive. */
    public List<ManagerTheaterOption> getTheaterOptions(int userId) throws SQLException {
        String sql =
            "SELECT t.theater_id, t.theater_name, t.theater_address, t.isActive, " +
            "  CASE WHEN EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = ? AND m.theater_id = t.theater_id) " +
            "       THEN 1 ELSE 0 END AS assigned, " +
            "  (SELECT TOP 1 r.request_type FROM Manager_theater_requests r " +
            "    WHERE r.user_id = ? AND r.theater_id = t.theater_id AND r.status = 'PENDING') AS pending_type " +
            "FROM Theaters t " +
            "WHERE t.deleted_at IS NULL " +
            "AND (t.isActive = 1 OR EXISTS (SELECT 1 FROM Managers m2 WHERE m2.user_id = ? AND m2.theater_id = t.theater_id)) " +
            "ORDER BY t.theater_name ASC";
        List<ManagerTheaterOption> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ManagerTheaterOption o = new ManagerTheaterOption();
                    o.setTheaterId(rs.getInt("theater_id"));
                    o.setTheaterName(rs.getString("theater_name"));
                    o.setTheaterAddress(rs.getString("theater_address"));
                    o.setActive(rs.getBoolean("isActive"));
                    o.setAssigned(rs.getInt("assigned") == 1);
                    o.setPendingType(rs.getString("pending_type"));
                    list.add(o);
                }
            }
        }
        return list;
    }

    /* Latest requests of one manager, newest first. */
    public List<ManagerTheaterRequest> getRequestsByUser(int userId, int limit) throws SQLException {
        String sql =
            "SELECT TOP (?) r.request_id, r.theater_id, t.theater_name, r.request_type, r.status, " +
            "       r.note, r.created_at, r.reviewed_at, r.review_note " +
            "FROM Manager_theater_requests r " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "WHERE r.user_id = ? " +
            "ORDER BY r.created_at DESC, r.request_id DESC";
        List<ManagerTheaterRequest> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ManagerTheaterRequest r = new ManagerTheaterRequest();
                    r.setRequestId(rs.getInt("request_id"));
                    r.setTheaterId(rs.getInt("theater_id"));
                    r.setTheaterName(rs.getString("theater_name"));
                    r.setRequestType(rs.getString("request_type"));
                    r.setStatus(rs.getString("status"));
                    r.setNote(rs.getString("note"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setReviewedAt(rs.getTimestamp("reviewed_at"));
                    r.setReviewNote(rs.getString("review_note"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    /* Creates a PENDING request. The state is re-checked inside the INSERT, so a stale or repeated
     * submit inserts nothing. ASSIGN needs an active, live, not-yet-assigned theater;
     * REMOVE needs an existing assignment. Returns false when nothing was inserted.
     */
    public boolean create(int userId, int theaterId, String type, String note) throws SQLException {
        String sql =
            "INSERT INTO Manager_theater_requests (user_id, theater_id, request_type, note) " +
            "SELECT ?, t.theater_id, ?, ? FROM Theaters t " +
            "WHERE t.theater_id = ? AND t.deleted_at IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM Manager_theater_requests p " +
            "                WHERE p.user_id = ? AND p.theater_id = t.theater_id AND p.status = 'PENDING') " +
            "AND ( (? = 'ASSIGN' AND t.isActive = 1 " +
            "       AND NOT EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = ? AND m.theater_id = t.theater_id)) " +
            "   OR (? = 'REMOVE' " +
            "       AND EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = ? AND m.theater_id = t.theater_id)) )";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setString(3, note);
            stmt.setInt(4, theaterId);
            stmt.setInt(5, userId);
            stmt.setString(6, type);
            stmt.setInt(7, userId);
            stmt.setString(8, type);
            stmt.setInt(9, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 2601 / 2627 = unique index violation: a concurrent request already created the pending row.
            if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627) {
                return false;
            }
            throw e;
        }
    }

    /* Lets a manager withdraw their own PENDING request. */
    public boolean cancel(int requestId, int userId) throws SQLException {
        String sql =
            "UPDATE Manager_theater_requests SET status = 'CANCELLED', reviewed_at = SYSDATETIME() " +
            "WHERE request_id = ? AND user_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}

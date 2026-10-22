package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminTheaterRequestRow;
import com.movieweb.util.DBConnection;

/* Admin review of manager assign/remove requests.
 * Approval marks the request APPROVED and changes the Managers table in ONE transaction, so a request
 * can never be approved without its effect (or the reverse). Every state change re-checks
 * status = 'PENDING' in its WHERE clause, so a repeated or stale click changes nothing.
 */
public class AdminTheaterRequestsDAO {

    /** Result codes of approve(). */
    public static final int APPROVED = 1;
    public static final int NOT_PENDING = 0;
    public static final int CANNOT_APPLY = -1;

    private String statusClause(String status) {
        return ("PENDING".equals(status) || "APPROVED".equals(status)
                || "REJECTED".equals(status) || "CANCELLED".equals(status))
            ? " WHERE r.status = ? " : "";
    }

    private boolean hasStatus(String status) {
        return !statusClause(status).isEmpty();
    }

    public int count(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Manager_theater_requests r" + statusClause(status);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (hasStatus(status)) {
                ps.setString(1, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AdminTheaterRequestRow> list(String status, int offset, int limit) throws SQLException {
        String sql =
            "SELECT r.request_id, r.user_id, u.username, u.full_name, u.email, r.theater_id, t.theater_name, " +
            "       r.request_type, r.status, r.note, r.created_at, r.reviewed_at, r.review_note, " +
            "       a.full_name AS reviewer_name " +
            "FROM Manager_theater_requests r " +
            "INNER JOIN Users u ON u.user_id = r.user_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "LEFT JOIN Users a ON a.user_id = r.reviewed_by " +
            statusClause(status) +
            "ORDER BY CASE WHEN r.status = 'PENDING' THEN 0 ELSE 1 END, r.created_at DESC, r.request_id DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminTheaterRequestRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            if (hasStatus(status)) {
                ps.setString(i++, status);
            }
            ps.setInt(i++, offset);
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
        }
        return rows;
    }

    private AdminTheaterRequestRow map(ResultSet rs) throws SQLException {
        AdminTheaterRequestRow r = new AdminTheaterRequestRow();
        r.setRequestId(rs.getInt("request_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setUsername(rs.getString("username"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setTheaterId(rs.getInt("theater_id"));
        r.setTheaterName(rs.getString("theater_name"));
        r.setRequestType(rs.getString("request_type"));
        r.setStatus(rs.getString("status"));
        r.setNote(rs.getString("note"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setReviewedAt(rs.getTimestamp("reviewed_at"));
        r.setReviewNote(rs.getString("review_note"));
        r.setReviewerName(rs.getString("reviewer_name"));
        return r;
    }

    /** One request with its manager and theater (used to email the manager after a decision). */
    public AdminTheaterRequestRow getById(int requestId) throws SQLException {
        String sql =
            "SELECT r.request_id, r.user_id, u.username, u.full_name, u.email, r.theater_id, t.theater_name, " +
            "       r.request_type, r.status, r.note, r.created_at, r.reviewed_at, r.review_note, " +
            "       a.full_name AS reviewer_name " +
            "FROM Manager_theater_requests r " +
            "INNER JOIN Users u ON u.user_id = r.user_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "LEFT JOIN Users a ON a.user_id = r.reviewed_by " +
            "WHERE r.request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /* Approves a pending request and applies it to Managers, atomically.
     * Returns APPROVED, NOT_PENDING (already handled or cancelled), or CANNOT_APPLY
     * (the manager or theater changed since the request: nothing is changed, the request stays pending).
     */
    public int approve(int requestId, int adminId, String reviewNote) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String type;
                int userId;
                int theaterId;
                // Claim the request: only one admin click can move it out of PENDING.
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Manager_theater_requests SET status = 'APPROVED', reviewed_at = SYSDATETIME(), " +
                        "reviewed_by = ?, review_note = ? WHERE request_id = ? AND status = 'PENDING'")) {
                    ps.setInt(1, adminId);
                    ps.setString(2, reviewNote);
                    ps.setInt(3, requestId);
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return NOT_PENDING;
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT user_id, theater_id, request_type FROM Manager_theater_requests WHERE request_id = ?")) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return NOT_PENDING;
                        }
                        userId = rs.getInt("user_id");
                        theaterId = rs.getInt("theater_id");
                        type = rs.getString("request_type");
                    }
                }
                int changed;
                if ("ASSIGN".equals(type)) {
                    // Same rules as AdminManagersDAO.assign: active live manager, live theater, not yet linked.
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Managers (user_id, theater_id) " +
                            "SELECT u.user_id, t.theater_id FROM Users u, Theaters t " +
                            "WHERE u.user_id = ? AND u.role = 'MANAGER' AND u.deleted_at IS NULL AND u.isActive = 1 " +
                            "AND t.theater_id = ? AND t.deleted_at IS NULL " +
                            "AND NOT EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = u.user_id AND m.theater_id = t.theater_id)")) {
                        ps.setInt(1, userId);
                        ps.setInt(2, theaterId);
                        changed = ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM Managers WHERE user_id = ? AND theater_id = ?")) {
                        ps.setInt(1, userId);
                        ps.setInt(2, theaterId);
                        changed = ps.executeUpdate();
                    }
                }
                if (changed != 1) {
                    conn.rollback();
                    return CANNOT_APPLY;
                }
                conn.commit();
                return APPROVED;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /* Rejects a pending request. The Managers table is not touched. */
    public boolean reject(int requestId, int adminId, String reviewNote) throws SQLException {
        String sql =
            "UPDATE Manager_theater_requests SET status = 'REJECTED', reviewed_at = SYSDATETIME(), " +
            "reviewed_by = ?, review_note = ? WHERE request_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setString(2, reviewNote);
            ps.setInt(3, requestId);
            return ps.executeUpdate() == 1;
        }
    }
}

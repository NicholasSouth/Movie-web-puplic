package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminUserRow;
import com.movieweb.util.DBConnection;

/* Admin-side USER account administration (role = 'USER' only; managers and admins are never touched here).
 * States: ACTIVE (isActive = 1), BLOCKED (isActive = 0, deleted_at null), DELETED (deleted_at set,
 * i.e. closed by the user; shown read-only). Block/unblock re-check the state in their WHERE clause.
 */
public class AdminUsersDAO {

    private static final String SELECT =
        "SELECT u.user_id, u.username, u.full_name, u.email, u.phone, u.isActive, u.created_at, u.deleted_at, " +
        "(SELECT COUNT(*) FROM Comments c WHERE c.user_id = u.user_id) AS comment_count, " +
        "(SELECT COUNT(*) FROM Booking_showtimes b WHERE b.user_id = u.user_id AND b.delete_at IS NULL) AS booking_count, " +
        "(SELECT COUNT(*) FROM Reports r INNER JOIN Comments c2 ON c2.comment_id = r.comment_id " +
        "   WHERE c2.user_id = u.user_id) AS reports_received, " +
        "(SELECT COUNT(*) FROM Reports r2 WHERE r2.user_id = u.user_id) AS reports_filed " +
        "FROM Users u WHERE u.role = 'USER'";

    private String statusClause(String status) {
        if ("ACTIVE".equals(status)) return " AND u.isActive = 1 AND u.deleted_at IS NULL";
        if ("BLOCKED".equals(status)) return " AND u.isActive = 0 AND u.deleted_at IS NULL";
        if ("DELETED".equals(status)) return " AND u.deleted_at IS NOT NULL";
        return " AND u.deleted_at IS NULL"; // ALL = every account that has not been closed
    }

    private String searchClause(String search) {
        return (search == null || search.isEmpty())
            ? "" : " AND (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ? OR u.phone LIKE ?)";
    }

    private int bindSearch(PreparedStatement ps, int i, String search) throws SQLException {
        if (search != null && !search.isEmpty()) {
            String like = "%" + search + "%";
            for (int k = 0; k < 4; k++) {
                ps.setString(i++, like);
            }
        }
        return i;
    }

    public int count(String status, String search) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users u WHERE u.role = 'USER'" + statusClause(status) + searchClause(search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, 1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AdminUserRow> list(String status, String search, int offset, int limit) throws SQLException {
        String sql = SELECT + statusClause(status) + searchClause(search)
                   + " ORDER BY u.created_at DESC, u.user_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminUserRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = bindSearch(ps, 1, search);
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

    public AdminUserRow getById(int userId) throws SQLException {
        String sql = SELECT + " AND u.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /* Blocks login for an active, non-deleted USER. False if already blocked, deleted or not a USER. */
    public boolean block(int userId) throws SQLException {
        return update("UPDATE Users SET isActive = 0 WHERE user_id = ? AND role = 'USER' " +
                      "AND deleted_at IS NULL AND isActive = 1", userId);
    }

    /* Restores login for a blocked, non-deleted USER. */
    public boolean unblock(int userId) throws SQLException {
        return update("UPDATE Users SET isActive = 1 WHERE user_id = ? AND role = 'USER' " +
                      "AND deleted_at IS NULL AND isActive = 0", userId);
    }

    private boolean update(String sql, int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        }
    }

    private AdminUserRow map(ResultSet rs) throws SQLException {
        AdminUserRow r = new AdminUserRow();
        r.setUserId(rs.getInt("user_id"));
        r.setUsername(rs.getString("username"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setPhone(rs.getString("phone"));
        r.setActive(rs.getBoolean("isActive"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setDeletedAt(rs.getTimestamp("deleted_at"));
        r.setCommentCount(rs.getInt("comment_count"));
        r.setBookingCount(rs.getInt("booking_count"));
        r.setReportsReceived(rs.getInt("reports_received"));
        r.setReportsFiled(rs.getInt("reports_filed"));
        return r;
    }
}

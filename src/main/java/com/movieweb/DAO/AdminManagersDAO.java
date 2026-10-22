package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminManagerRow;
import com.movieweb.model.Theaters;
import com.movieweb.util.DBConnection;

/* Admin-side manager account administration.
 * A "new" (pending) manager is a MANAGER with isActive = 0 and no row in Managers.
 * Every mutating method re-checks the current state inside its WHERE clause and returns
 * whether exactly the expected row changed, so repeated or stale requests are harmless.
 */
public class AdminManagersDAO {

    private static final String BASE =
        "FROM Users u WHERE u.role = 'MANAGER' AND u.deleted_at IS NULL ";
    private static final String HAS_THEATER =
        "EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = u.user_id)";

    /* status: PENDING, ACTIVE, DEACTIVATED or anything else = all. */
    private String statusClause(String status) {
        if ("PENDING".equals(status)) return " AND u.isActive = 0 AND NOT " + HAS_THEATER;
        if ("ACTIVE".equals(status)) return " AND u.isActive = 1";
        if ("DEACTIVATED".equals(status)) return " AND u.isActive = 0 AND " + HAS_THEATER;
        return "";
    }

    private String searchClause(String search) {
        return (search == null || search.isEmpty())
            ? "" : " AND (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?)";
    }

    private int bindSearch(PreparedStatement ps, int i, String search) throws SQLException {
        if (search != null && !search.isEmpty()) {
            String like = "%" + search + "%";
            ps.setString(i++, like);
            ps.setString(i++, like);
            ps.setString(i++, like);
        }
        return i;
    }

    public int count(String status, String search) throws SQLException {
        String sql = "SELECT COUNT(*) " + BASE + statusClause(status) + searchClause(search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, 1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AdminManagerRow> list(String status, String search, int offset, int limit) throws SQLException {
        String sql =
            "SELECT u.user_id, u.username, u.full_name, u.email, u.phone, u.isActive, u.created_at, " +
            "(SELECT COUNT(*) FROM Managers m WHERE m.user_id = u.user_id) AS theater_count " +
            BASE + statusClause(status) + searchClause(search) +
            " ORDER BY u.created_at DESC, u.user_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminManagerRow> rows = new ArrayList<>();
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

    public AdminManagerRow getById(int userId) throws SQLException {
        String sql =
            "SELECT u.user_id, u.username, u.full_name, u.email, u.phone, u.isActive, u.created_at, " +
            "(SELECT COUNT(*) FROM Managers m WHERE m.user_id = u.user_id) AS theater_count " +
            BASE + " AND u.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private AdminManagerRow map(ResultSet rs) throws SQLException {
        AdminManagerRow r = new AdminManagerRow();
        r.setUserId(rs.getInt("user_id"));
        r.setUsername(rs.getString("username"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setPhone(rs.getString("phone"));
        r.setActive(rs.getBoolean("isActive"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setTheaterCount(rs.getInt("theater_count"));
        return r;
    }

    /* Theaters (active or not, not soft-deleted) a manager is assigned to. */
    public List<Theaters> getAssignedTheaters(int userId) throws SQLException {
        String sql =
            "SELECT t.theater_id, t.theater_name, t.isActive FROM Managers m " +
            "INNER JOIN Theaters t ON t.theater_id = m.theater_id " +
            "WHERE m.user_id = ? AND t.deleted_at IS NULL ORDER BY t.theater_name";
        List<Theaters> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Theaters t = new Theaters();
                    t.setTheater_id(rs.getInt("theater_id"));
                    t.setTheater_name(rs.getString("theater_name"));
                    t.setActive(rs.getBoolean("isActive"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    /* Approve a pending manager: activates the account only. Theater assignment is a separate step. */
    public boolean approve(int userId) throws SQLException {
        return update("UPDATE Users SET isActive = 1 WHERE user_id = ? AND role = 'MANAGER' " +
            "AND deleted_at IS NULL AND isActive = 0 AND NOT EXISTS " +
            "(SELECT 1 FROM Managers m WHERE m.user_id = Users.user_id)", userId);
    }

    /* Reject a pending manager: soft-deletes the account (the row is kept). */
    public boolean reject(int userId) throws SQLException {
        return update("UPDATE Users SET deleted_at = GETDATE() WHERE user_id = ? AND role = 'MANAGER' " +
            "AND deleted_at IS NULL AND isActive = 0 AND NOT EXISTS " +
            "(SELECT 1 FROM Managers m WHERE m.user_id = Users.user_id)", userId);
    }

    public boolean deactivate(int userId) throws SQLException {
        return update("UPDATE Users SET isActive = 0 WHERE user_id = ? AND role = 'MANAGER' " +
            "AND deleted_at IS NULL AND isActive = 1", userId);
    }

    /* Re-activate a deactivated manager (one that still has assignments). Pending ones must be approved. */
    public boolean activate(int userId) throws SQLException {
        return update("UPDATE Users SET isActive = 1 WHERE user_id = ? AND role = 'MANAGER' " +
            "AND deleted_at IS NULL AND isActive = 0 AND EXISTS " +
            "(SELECT 1 FROM Managers m WHERE m.user_id = Users.user_id)", userId);
    }

    /* Soft-delete a manager and drop their theater assignments, in one transaction. */
    public boolean delete(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int changed;
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Users SET deleted_at = GETDATE(), isActive = 0 " +
                        "WHERE user_id = ? AND role = 'MANAGER' AND deleted_at IS NULL")) {
                    ps.setInt(1, userId);
                    changed = ps.executeUpdate();
                }
                if (changed == 1) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Managers WHERE user_id = ?")) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return changed == 1;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /* Assign an approved (active) manager to a theater. False if invalid, not approved, or already assigned. */
    public boolean assign(int userId, int theaterId) throws SQLException {
        String sql =
            "INSERT INTO Managers (user_id, theater_id) " +
            "SELECT u.user_id, t.theater_id FROM Users u, Theaters t " +
            "WHERE u.user_id = ? AND u.role = 'MANAGER' AND u.deleted_at IS NULL AND u.isActive = 1 " +
            "AND t.theater_id = ? AND t.deleted_at IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = u.user_id AND m.theater_id = t.theater_id)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, theaterId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean unassign(int userId, int theaterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM Managers WHERE user_id = ? AND theater_id = ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, theaterId);
            return ps.executeUpdate() == 1;
        }
    }

    private boolean update(String sql, int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        }
    }
}

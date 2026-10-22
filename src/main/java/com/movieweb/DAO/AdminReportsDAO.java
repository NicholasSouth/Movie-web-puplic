package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminReportRow;
import com.movieweb.util.DBConnection;

/* Reported-comment moderation. The schema (Comments, Reports) has no hidden/status column, so:
 *    dismissReports deletes the Reports rows of a comment and keeps the comment;
 *    removeComment deletes the comment together with its Reports rows (permanent).
 * Comments are grouped through a derived table so comment_text is never used in GROUP BY.
 */
public class AdminReportsDAO {

    private static final String FROM =
        "FROM Comments c " +
        "INNER JOIN (SELECT comment_id, COUNT(*) AS report_count, MIN(created_at) AS first_at, MAX(created_at) AS last_at " +
        "            FROM Reports GROUP BY comment_id) rp ON rp.comment_id = c.comment_id " +
        "LEFT JOIN Movies m ON m.movie_id = c.movie_id " +
        "LEFT JOIN Users u ON u.user_id = c.user_id ";

    private String searchClause(String search) {
        return (search == null || search.isEmpty())
            ? "" : "WHERE (c.comment_text LIKE ? OR u.username LIKE ? OR m.movie_name LIKE ?) ";
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

    public int count(String search) throws SQLException {
        String sql = "SELECT COUNT(*) " + FROM + searchClause(search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, 1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* Reported comments, most recently reported first. */
    public List<AdminReportRow> list(String search, int offset, int limit) throws SQLException {
        String sql = select() + FROM + searchClause(search)
                   + "ORDER BY rp.last_at DESC, c.comment_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminReportRow> rows = new ArrayList<>();
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

    /* One reported comment, or null if it has no reports (any more) or no longer exists. */
    public AdminReportRow getById(int commentId) throws SQLException {
        String sql = select() + FROM + "WHERE c.comment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /* Who reported the comment and when: rows of {username, full name, reported-at text}. */
    public List<String[]> getReporters(int commentId) throws SQLException {
        String sql = "SELECT u.username, u.full_name, r.created_at FROM Reports r " +
                     "LEFT JOIN Users u ON u.user_id = r.user_id WHERE r.comment_id = ? ORDER BY r.created_at ASC";
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp at = rs.getTimestamp("created_at");
                    list.add(new String[] {rs.getString("username"), rs.getString("full_name"),
                                           at == null ? "" : at.toString()});
                }
            }
        }
        return list;
    }

    /* Rejects the reports: deletes the Reports rows, keeps the comment. False if there were none. */
    public boolean dismissReports(int commentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Reports WHERE comment_id = ?")) {
            ps.setInt(1, commentId);
            return ps.executeUpdate() > 0;
        }
    }

    /* Permanently removes a REPORTED comment and its reports in one transaction.
     * Refuses (false) when the comment has no reports, so an unreported comment can never be deleted
     * through a forged request and a second click cannot do anything.
     */
    public boolean removeComment(int commentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int reports;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Reports WHERE comment_id = ?")) {
                    ps.setInt(1, commentId);
                    reports = ps.executeUpdate();
                }
                if (reports == 0) {
                    conn.rollback();
                    return false;
                }
                int removed;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Comments WHERE comment_id = ?")) {
                    ps.setInt(1, commentId);
                    removed = ps.executeUpdate();
                }
                if (removed != 1) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /* Number of distinct reported comments (for the dashboard). */
    public int countReportedComments() throws SQLException {
        return count("");
    }

    private String select() {
        return "SELECT c.comment_id, c.comment_text, c.created_at, c.movie_id, m.movie_name, " +
               "c.user_id, u.username, u.isActive, u.deleted_at, rp.report_count, rp.first_at, rp.last_at ";
    }

    private AdminReportRow map(ResultSet rs) throws SQLException {
        AdminReportRow r = new AdminReportRow();
        r.setCommentId(rs.getInt("comment_id"));
        r.setCommentText(rs.getString("comment_text"));
        r.setCommentCreatedAt(rs.getTimestamp("created_at"));
        r.setMovieId(rs.getInt("movie_id"));
        r.setMovieName(rs.getString("movie_name"));
        r.setAuthorId(rs.getInt("user_id"));
        r.setAuthorUsername(rs.getString("username"));
        r.setAuthorActive(rs.getBoolean("isActive"));
        r.setAuthorDeleted(rs.getTimestamp("deleted_at") != null);
        r.setReportCount(rs.getInt("report_count"));
        r.setFirstReportedAt(rs.getTimestamp("first_at"));
        r.setLastReportedAt(rs.getTimestamp("last_at"));
        return r;
    }
}

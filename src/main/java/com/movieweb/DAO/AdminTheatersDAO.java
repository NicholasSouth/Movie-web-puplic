package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminManagerRow;
import com.movieweb.model.AdminTheaterRow;
import com.movieweb.model.Theaters;
import com.movieweb.util.DBConnection;

/* Admin-wide theater queries. Unlike ManagersDAO, nothing here depends on manager assignment. */
public class AdminTheatersDAO {

    /* Every theater that has not been soft-deleted (active AND inactive), for admin selectors. */
    public List<Theaters> getSelectableTheaters() throws SQLException {
        String sql = "SELECT theater_id, theater_name, isActive FROM Theaters "
                   + "WHERE deleted_at IS NULL ORDER BY theater_name ASC";
        List<Theaters> theaters = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Theaters t = new Theaters();
                t.setTheater_id(rs.getInt("theater_id"));
                t.setTheater_name(rs.getString("theater_name"));
                t.setActive(rs.getBoolean("isActive"));
                theaters.add(t);
            }
        }
        return theaters;
    }

    private static final String COUNTS =
        "(SELECT COUNT(*) FROM Rooms r WHERE r.theater_id = t.theater_id AND r.deleted_at IS NULL) AS room_count, " +
        "(SELECT COUNT(*) FROM Rooms r WHERE r.theater_id = t.theater_id AND r.deleted_at IS NULL AND r.isActive = 1) AS active_room_count, " +
        "(SELECT COUNT(*) FROM Managers m WHERE m.theater_id = t.theater_id) AS manager_count, " +
        "(SELECT COUNT(*) FROM Showtimes s INNER JOIN Rooms r ON r.room_id = s.room_id " +
        "  WHERE r.theater_id = t.theater_id AND s.deleted_at IS NULL AND s.start_at > GETDATE()) AS upcoming_showtimes ";

    private String statusClause(String status) {
        if ("ACTIVE".equals(status)) return " AND t.isActive = 1";
        if ("INACTIVE".equals(status)) return " AND t.isActive = 0";
        return "";
    }

    private String searchClause(String search) {
        return (search == null || search.isEmpty())
            ? "" : " AND (t.theater_name LIKE ? OR t.theater_address LIKE ?)";
    }

    private int bindSearch(PreparedStatement ps, int i, String search) throws SQLException {
        if (search != null && !search.isEmpty()) {
            String like = "%" + search + "%";
            ps.setString(i++, like);
            ps.setString(i++, like);
        }
        return i;
    }

    public int count(String status, String search) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Theaters t WHERE t.deleted_at IS NULL"
                   + statusClause(status) + searchClause(search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, 1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AdminTheaterRow> list(String status, String search, int offset, int limit) throws SQLException {
        String sql = "SELECT t.theater_id, t.theater_name, t.theater_address, t.isActive, t.open_time, t.closing_time, "
                   + COUNTS + "FROM Theaters t WHERE t.deleted_at IS NULL"
                   + statusClause(status) + searchClause(search)
                   + " ORDER BY t.theater_name ASC, t.theater_id ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminTheaterRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = bindSearch(ps, 1, search);
            ps.setInt(i++, offset);
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRow(rs));
                }
            }
        }
        return rows;
    }

    public AdminTheaterRow getRowById(int theaterId) throws SQLException {
        String sql = "SELECT t.theater_id, t.theater_name, t.theater_address, t.isActive, t.open_time, t.closing_time, "
                   + COUNTS + "FROM Theaters t WHERE t.deleted_at IS NULL AND t.theater_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theaterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    private AdminTheaterRow mapRow(ResultSet rs) throws SQLException {
        AdminTheaterRow r = new AdminTheaterRow();
        r.setTheaterId(rs.getInt("theater_id"));
        r.setTheaterName(rs.getString("theater_name"));
        r.setAddress(rs.getString("theater_address"));
        r.setActive(rs.getBoolean("isActive"));
        r.setOpenTime(rs.getTime("open_time"));
        r.setClosingTime(rs.getTime("closing_time"));
        r.setRoomCount(rs.getInt("room_count"));
        r.setActiveRoomCount(rs.getInt("active_room_count"));
        r.setManagerCount(rs.getInt("manager_count"));
        r.setUpcomingShowtimes(rs.getInt("upcoming_showtimes"));
        return r;
    }

    /* Full editable record (not soft-deleted), or null. */
    public Theaters getTheaterForEdit(int theaterId) throws SQLException {
        String sql = "SELECT theater_id, theater_name, theater_address, theater_image_path, description, "
                   + "latitude, longtitude, open_time, closing_time, isActive, deleted_at "
                   + "FROM Theaters WHERE theater_id = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theaterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Theaters t = new Theaters();
                t.setTheater_id(rs.getInt("theater_id"));
                t.setTheater_name(rs.getString("theater_name"));
                t.setTheater_address(rs.getString("theater_address"));
                t.setTheater_image_path(rs.getString("theater_image_path"));
                t.setDescription(rs.getString("description"));
                t.setLatitude(rs.getDouble("latitude"));
                t.setLongtitude(rs.getDouble("longtitude"));
                t.setOpen_time(rs.getTime("open_time"));
                t.setClosing_time(rs.getTime("closing_time"));
                t.setActive(rs.getBoolean("isActive"));
                t.setDeleted_at(rs.getTimestamp("deleted_at"));
                return t;
            }
        }
    }

    /* True if another non-deleted theater already uses this name (case-insensitive by collation). */
    public boolean nameExists(String name, int excludeTheaterId) throws SQLException {
        String sql = "SELECT 1 FROM Theaters WHERE deleted_at IS NULL AND theater_name = ? AND theater_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeTheaterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* Inserts a new, active theater and returns its generated id. */
    public int create(Theaters t) throws SQLException {
        String sql = "INSERT INTO Theaters (theater_name, theater_address, theater_image_path, description, "
                   + "latitude, longtitude, open_time, closing_time, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            bindFields(ps, t);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Theater was inserted but no generated id was returned.");
    }

    /* Updates the descriptive fields only; isActive is changed through setActive. */
    public boolean update(Theaters t) throws SQLException {
        String sql = "UPDATE Theaters SET theater_name = ?, theater_address = ?, theater_image_path = ?, "
                   + "description = ?, latitude = ?, longtitude = ?, open_time = ?, closing_time = ? "
                   + "WHERE theater_id = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindFields(ps, t);
            ps.setInt(9, t.getTheater_id());
            return ps.executeUpdate() == 1;
        }
    }

    private void bindFields(PreparedStatement ps, Theaters t) throws SQLException {
        ps.setString(1, t.getTheater_name());
        ps.setString(2, t.getTheater_address());
        ps.setString(3, t.getTheater_image_path());
        ps.setString(4, t.getDescription());
        ps.setDouble(5, t.getLatitude());
        ps.setDouble(6, t.getLongtitude());
        ps.setTime(7, t.getOpen_time());
        ps.setTime(8, t.getClosing_time());
    }

    /* Activates or deactivates a theater. Nothing is cascaded: rooms, seats, showtimes,
     * bookings and payments are left exactly as they are. False if it was already in that state.
     */
    public boolean setActive(int theaterId, boolean active) throws SQLException {
        String sql = "UPDATE Theaters SET isActive = ? WHERE theater_id = ? AND deleted_at IS NULL AND isActive = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, theaterId);
            ps.setBoolean(3, !active);
            return ps.executeUpdate() == 1;
        }
    }

    /* Managers assigned to this theater (any account state, not soft-deleted). */
    public List<AdminManagerRow> getAssignedManagers(int theaterId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, u.phone, u.isActive, u.created_at, "
                   + "(SELECT COUNT(*) FROM Managers m2 WHERE m2.user_id = u.user_id) AS theater_count "
                   + "FROM Managers m INNER JOIN Users u ON u.user_id = m.user_id "
                   + "WHERE m.theater_id = ? AND u.role = 'MANAGER' AND u.deleted_at IS NULL ORDER BY u.full_name";
        return queryManagers(sql, theaterId);
    }

    /* Approved (active), non-deleted MANAGER accounts not yet assigned to this theater. Pending managers must be approved first. */
    public List<AdminManagerRow> getAssignableManagers(int theaterId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, u.phone, u.isActive, u.created_at, "
                   + "(SELECT COUNT(*) FROM Managers m2 WHERE m2.user_id = u.user_id) AS theater_count "
                   + "FROM Users u WHERE u.role = 'MANAGER' AND u.deleted_at IS NULL AND u.isActive = 1 "
                   + "AND NOT EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = u.user_id AND m.theater_id = ?) "
                   + "ORDER BY u.full_name";
        return queryManagers(sql, theaterId);
    }

    private List<AdminManagerRow> queryManagers(String sql, int theaterId) throws SQLException {
        List<AdminManagerRow> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theaterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminManagerRow r = new AdminManagerRow();
                    r.setUserId(rs.getInt("user_id"));
                    r.setUsername(rs.getString("username"));
                    r.setFullName(rs.getString("full_name"));
                    r.setEmail(rs.getString("email"));
                    r.setPhone(rs.getString("phone"));
                    r.setActive(rs.getBoolean("isActive"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setTheaterCount(rs.getInt("theater_count"));
                    list.add(r);
                }
            }
        }
        return list;
    }
}

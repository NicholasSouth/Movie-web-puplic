package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminAdRow;
import com.movieweb.util.DBConnection;

/* Admin-side advertisement administration. The user pages read ads through AdsDAO.getActiveAdvertisements();
 * everything the admin does (list, create, edit, activate/deactivate, delete) lives here. Every method
 * propagates SQLException. Delete is a soft delete (deleted_at), like the rest of the project, and every
 * statement ignores already-deleted ads. Real column names are used (ad_id, ad_name, ad_image_path, ad_link).
 */
public class AdminAdsDAO {

    private static final String SELECT =
        "SELECT a.ad_id, a.ad_name, a.ad_image_path, a.ad_link, a.start_at, a.end_at, a.isActive " +
        "FROM Ads a WHERE a.deleted_at IS NULL";

    private String statusClause(String status) {
        if ("ACTIVE".equals(status)) return " AND a.isActive = 1";
        if ("INACTIVE".equals(status)) return " AND a.isActive = 0";
        return "";
    }

    private String searchClause(String search) {
        return (search == null || search.isEmpty()) ? "" : " AND a.ad_name LIKE ?";
    }

    private int bindSearch(PreparedStatement ps, int i, String search) throws SQLException {
        if (search != null && !search.isEmpty()) {
            ps.setString(i++, "%" + search + "%");
        }
        return i;
    }

    public int count(String status, String search) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ads a WHERE a.deleted_at IS NULL" + statusClause(status) + searchClause(search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, 1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AdminAdRow> list(String status, String search, int offset, int limit) throws SQLException {
        String sql = SELECT + statusClause(status) + searchClause(search)
                   + " ORDER BY a.ad_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminAdRow> rows = new ArrayList<>();
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

    public AdminAdRow getById(int adId) throws SQLException {
        String sql = SELECT + " AND a.ad_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private AdminAdRow map(ResultSet rs) throws SQLException {
        AdminAdRow r = new AdminAdRow();
        r.setAdId(rs.getInt("ad_id"));
        r.setName(rs.getString("ad_name"));
        r.setImagePath(rs.getString("ad_image_path"));
        r.setLink(rs.getString("ad_link"));
        r.setStartAt(rs.getTimestamp("start_at"));
        r.setEndAt(rs.getTimestamp("end_at"));
        r.setActive(rs.getBoolean("isActive"));
        return r;
    }

    private void bindDate(PreparedStatement ps, int index, Timestamp value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, value);
        }
    }

    public boolean create(String name, String imagePath, String link, Timestamp startAt, Timestamp endAt,
                          boolean active) throws SQLException {
        String sql = "INSERT INTO Ads (ad_name, ad_image_path, ad_link, start_at, end_at, isActive) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, imagePath);
            ps.setString(3, link);
            bindDate(ps, 4, startAt);
            bindDate(ps, 5, endAt);
            ps.setBoolean(6, active);
            return ps.executeUpdate() == 1;
        }
    }

    /* Updates an ad. A null imagePath keeps the current image. Returns false if the ad no longer exists. */
    public boolean update(int adId, String name, String imagePath, String link, Timestamp startAt, Timestamp endAt)
            throws SQLException {
        String sql = "UPDATE Ads SET ad_name = ?, ad_link = ?, start_at = ?, end_at = ?" +
                     (imagePath != null ? ", ad_image_path = ?" : "") +
                     " WHERE ad_id = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, name);
            ps.setString(i++, link);
            bindDate(ps, i++, startAt);
            bindDate(ps, i++, endAt);
            if (imagePath != null) {
                ps.setString(i++, imagePath);
            }
            ps.setInt(i, adId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Switches an ad on or off. Returns false if it is already in that state or no longer exists. */
    public boolean setActive(int adId, boolean active) throws SQLException {
        String sql = "UPDATE Ads SET isActive = ? WHERE ad_id = ? AND deleted_at IS NULL AND isActive <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, adId);
            ps.setBoolean(3, active);
            return ps.executeUpdate() == 1;
        }
    }

    /* Soft delete: the row is kept (deleted_at) and switched off, so it can never be shown again. */
    public boolean softDelete(int adId) throws SQLException {
        String sql = "UPDATE Ads SET deleted_at = GETDATE(), isActive = 0 " +
                     "WHERE ad_id = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adId);
            return ps.executeUpdate() == 1;
        }
    }
}

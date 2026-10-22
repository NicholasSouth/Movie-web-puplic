package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminPromotionRow;
import com.movieweb.model.Promotions;
import com.movieweb.util.DBConnection;

/* Admin-side promotion administration. Creating a promotion reuses PromotionsDAO.createPromotion;
 * everything else (list with usage, edit, activate/deactivate) lives here. Every method propagates
 * SQLException. Promotions are never deleted: bookings reference promotion_id, so a code is switched off
 * with isActive instead. Real column names are used (promotion_code, not "code").
 */
public class AdminPromotionsDAO {

    private static final String SELECT =
        "SELECT p.promotion_id, p.promotion_code, p.description, p.price_modify, p.start_at, p.end_at, " +
        "p.usage_limit, p.isActive, " +
        // Same rule as PromotionsDAO.getPromotionUsageCount.
        "(SELECT COUNT(*) FROM Booking_showtimes b WHERE b.promotion_id = p.promotion_id " +
        "   AND b.delete_at IS NULL AND b.status <> 'CANCELLED') AS used_count " +
        "FROM Promotions p WHERE 1 = 1";

    private String statusClause(String status) {
        if ("ACTIVE".equals(status)) return " AND p.isActive = 1";
        if ("INACTIVE".equals(status)) return " AND p.isActive = 0";
        return "";
    }

    private String searchClause(String search) {
        return (search == null || search.isEmpty()) ? "" : " AND p.promotion_code LIKE ?";
    }

    private int bindSearch(PreparedStatement ps, int i, String search) throws SQLException {
        if (search != null && !search.isEmpty()) {
            ps.setString(i++, "%" + search + "%");
        }
        return i;
    }

    public int count(String status, String search) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Promotions p WHERE 1 = 1" + statusClause(status) + searchClause(search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, 1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AdminPromotionRow> list(String status, String search, int offset, int limit) throws SQLException {
        String sql = SELECT + statusClause(status) + searchClause(search)
                   + " ORDER BY p.promotion_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminPromotionRow> rows = new ArrayList<>();
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

    public AdminPromotionRow getById(int promotionId) throws SQLException {
        String sql = SELECT + " AND p.promotion_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /* True if another promotion (not excludeId; pass 0 when creating) already uses this code. */
    public boolean codeExists(String code, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM Promotions WHERE promotion_code = ? AND promotion_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* Updates the editable fields. isActive is not touched (use setActive). Existing bookings keep the
     * price they were created with, because Booking_showtimes.price and Booking_seats.final_price are stored.
     */
    public boolean update(Promotions p) throws SQLException {
        String sql = "UPDATE Promotions SET promotion_code = ?, description = ?, price_modify = ?, " +
                     "start_at = ?, end_at = ?, usage_limit = ? WHERE promotion_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPromotion_code());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getPrice_modify());
            ps.setTimestamp(4, p.getStart_at());
            ps.setTimestamp(5, p.getEnd_at());
            ps.setInt(6, p.getUsage_limit());
            ps.setInt(7, p.getPromotion_id());
            return ps.executeUpdate() == 1;
        }
    }

    /* False when the promotion is already in that state or no longer exists. */
    public boolean setActive(int promotionId, boolean active) throws SQLException {
        String sql = "UPDATE Promotions SET isActive = ? WHERE promotion_id = ? AND isActive <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, promotionId);
            ps.setBoolean(3, active);
            return ps.executeUpdate() == 1;
        }
    }

    private AdminPromotionRow map(ResultSet rs) throws SQLException {
        AdminPromotionRow r = new AdminPromotionRow();
        r.setPromotionId(rs.getInt("promotion_id"));
        r.setCode(rs.getString("promotion_code"));
        r.setDescription(rs.getString("description"));
        r.setPriceModify(rs.getString("price_modify"));
        r.setStartAt(rs.getTimestamp("start_at"));
        r.setEndAt(rs.getTimestamp("end_at"));
        int limit = rs.getInt("usage_limit");
        r.setUsageLimit(rs.wasNull() ? 0 : limit); // same NULL -> 0 convention as PromotionsDAO.mapPromotion
        r.setActive(rs.getBoolean("isActive"));
        r.setUsedCount(rs.getInt("used_count"));
        return r;
    }
}

package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.movieweb.model.Promotions;
import com.movieweb.util.DBConnection;

public class PromotionsDAO {
    private static final int DEFAULT_PAGE_SIZE = 10;

    public Promotions getPromotionById(int promotion_id) {
        String sql = "SELECT * FROM Promotions WHERE promotion_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPromotion(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Promotions getPromotionByCode(String promotion_code) {
        if (promotion_code == null || promotion_code.trim().isEmpty()) {
            return null;
        }
        String sql =
                "SELECT * FROM Promotions " +
                "WHERE promotion_code = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, promotion_code.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPromotion(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPromotionUsageCount(int promotion_id) {
        String sql =
                "SELECT COUNT(*) " +
                "FROM Booking_showtimes " +
                "WHERE promotion_id = ? " +
                "AND delete_at IS NULL " +
                "AND status <> 'CANCELLED'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, promotion_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean promotionCodeExists(String promotion_code) throws SQLException {
        if (promotion_code == null || promotion_code.trim().isEmpty()) {
            return false;
        }
        String sql =
                "SELECT 1 FROM Promotions " +
                "WHERE promotion_code = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, promotion_code.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int createPromotion(Promotions promotion) throws SQLException {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion information is required.");
        }
        String code = promotion.getPromotion_code();
        String description = promotion.getDescription();
        String priceModify = promotion.getPrice_modify();
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion code is required.");
        }
        if (code.trim().length() > 20) {
            throw new IllegalArgumentException("Promotion code cannot exceed 20 characters.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion description is required.");
        }
        if (priceModify == null || priceModify.trim().isEmpty() || priceModify.trim().length() > 5) {
            throw new IllegalArgumentException("Promotion price modifier must contain 1 to 5 characters.");
        }
        if (promotion.getStart_at() == null || promotion.getEnd_at() == null || !promotion.getEnd_at().after(promotion.getStart_at())) {
            throw new IllegalArgumentException("Promotion end time must be after its start time.");
        }
        if (promotion.getUsage_limit() <= 0) {
            throw new IllegalArgumentException("Promotion usage limit must be greater than zero.");
        }
        String sql =
                "INSERT INTO Promotions " +
                "(promotion_code, description, price_modify, " +
                "start_at, end_at, usage_limit, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, code.trim());
            stmt.setString(2, description.trim());
            stmt.setString(3, priceModify.trim());
            stmt.setTimestamp(4, promotion.getStart_at());
            stmt.setTimestamp(5, promotion.getEnd_at());
            stmt.setInt(6, promotion.getUsage_limit());
            stmt.setBoolean(7, promotion.isActive());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new SQLException("Creating the promotion did not insert one record.");
            }
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("Promotion was inserted, but its generated ID could not be retrieved.");
        }
    }

    public List<Promotions> getPromotions(int page) throws SQLException {
        return getPromotions(page, DEFAULT_PAGE_SIZE);
    }

    public List<Promotions> getPromotions(int page, int pageSize) throws SQLException {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100.");
        }
        long offset = ((long) page - 1) * pageSize;
        String sql =
                "SELECT * FROM Promotions " +
                "ORDER BY promotion_id DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Promotions> promotions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, offset);
            stmt.setInt(2, pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    promotions.add(mapPromotion(rs));
                }
            }
        }
        return promotions;
    }

    public int getPromotionCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Promotions";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Promotions mapPromotion(ResultSet rs) throws SQLException {
        Promotions promotion = new Promotions();
        promotion.setPromotion_id(rs.getInt("promotion_id"));
        promotion.setPromotion_code(rs.getString("promotion_code"));
        promotion.setDescription(rs.getString("description"));
        promotion.setPrice_modify(rs.getString("price_modify"));
        promotion.setStart_at(rs.getTimestamp("start_at"));
        promotion.setEnd_at(rs.getTimestamp("end_at"));
        int usageLimit = rs.getInt("usage_limit");
        if (rs.wasNull()) {
            usageLimit = 0;
        }
        promotion.setUsage_limit(usageLimit);
        promotion.setActive(rs.getBoolean("isActive"));
        return promotion;
    }
}
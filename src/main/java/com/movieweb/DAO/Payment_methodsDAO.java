package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.movieweb.model.Payment_methods;
import com.movieweb.util.DBConnection;

public class Payment_methodsDAO {
    public Payment_methods getPaymentMethodById(int payment_method_id) {
        String sql = "SELECT * FROM Payment_methods WHERE payment_method_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, payment_method_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Payment_methods payment_method = new Payment_methods();
                payment_method.setPayment_method_id(rs.getInt("payment_method_id"));
                payment_method.setUser_id(rs.getInt("user_id"));
                payment_method.setMethod(rs.getString("method"));
                payment_method.setCard_number(rs.getString("card_number"));
                payment_method.setExpired_date(rs.getDate("expired_date"));
                payment_method.setCreated_at(rs.getTimestamp("created_at"));
                payment_method.setActive(rs.getBoolean("isActive"));
                return payment_method;
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Payment_methods> getPaymentMethodsByUserId(int user_id) {
        List<Payment_methods> paymentMethods = new ArrayList<>();
        String sql =
                "SELECT * FROM Payment_methods " +
                "WHERE user_id = ? " +
                "AND isActive = 1 " +
                "ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            try (ResultSet rs = stmt.executeQuery()) 
            {
                while (rs.next()) 
                {
                    Payment_methods payment_method = new Payment_methods();
                    payment_method.setPayment_method_id(rs.getInt("payment_method_id"));
                    payment_method.setUser_id(rs.getInt("user_id"));
                    payment_method.setMethod(rs.getString("method"));
                    payment_method.setCard_number(rs.getString("card_number"));
                    payment_method.setExpired_date(rs.getDate("expired_date"));
                    payment_method.setCreated_at(rs.getTimestamp("created_at"));
                    payment_method.setActive(rs.getBoolean("isActive"));
                    paymentMethods.add(payment_method);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return paymentMethods;
    }

    // Get active VISA cards belonging to a manager and registered for a theater.
    public List<Payment_methods> getPaymentMethodsByUserAndTheater(int user_id, int theater_id) {
        List<Payment_methods> paymentMethods = new ArrayList<>();
        String sql =
                "SELECT pm.* " +
                "FROM Payment_methods pm " +
                "INNER JOIN Theater_payment_methods tpm " +
                "ON pm.payment_method_id = tpm.payment_method_id " +
                "WHERE pm.user_id = ? " +
                "AND tpm.theater_id = ? " +
                "AND pm.method = 'VISA' " +
                "AND pm.isActive = 1 " +
                "ORDER BY pm.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, theater_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payment_methods payment_method = new Payment_methods();
                    payment_method.setPayment_method_id(rs.getInt("payment_method_id"));
                    payment_method.setUser_id(rs.getInt("user_id"));
                    payment_method.setMethod(rs.getString("method"));
                    payment_method.setCard_number(rs.getString("card_number"));
                    payment_method.setExpired_date(rs.getDate("expired_date"));
                    payment_method.setCreated_at(rs.getTimestamp("created_at"));
                    payment_method.setActive(rs.getBoolean("isActive"));
                    paymentMethods.add(payment_method);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return paymentMethods;
    }
    
    public boolean addPaymentMethod(Payment_methods payment_method) {
        String sql =
                "INSERT INTO Payment_methods " +
                "(user_id, method, card_number, expired_date, created_at, isActive) " +
                "VALUES (?, ?, ?, ?, GETDATE(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment_method.getUser_id());
            stmt.setString(2, payment_method.getMethod());
            stmt.setString(3, payment_method.getCard_number());
            stmt.setDate(4, payment_method.getExpired_date());
            stmt.setBoolean(5, payment_method.isActive());
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                return false;
            }

            // Get the generated payment_method_id.
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    payment_method.setPayment_method_id(rs.getInt(1));
                    return true;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deactivatePaymentMethod(int payment_method_id) {
        String sql = "UPDATE Payment_methods SET isActive = 0 WHERE payment_method_id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, payment_method_id);
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deactivatePaymentMethod(int payment_method_id, int user_id) {
        String sql =
                "UPDATE Payment_methods "
                + "SET isActive = 0 "
                + "WHERE payment_method_id = ? "
                + "AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, payment_method_id);
            stmt.setInt(2, user_id);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
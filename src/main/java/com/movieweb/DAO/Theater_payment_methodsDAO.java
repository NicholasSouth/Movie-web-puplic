package com.movieweb.DAO;

import com.movieweb.model.Theater_payment_methods;
import com.movieweb.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Theater_payment_methodsDAO {
    public boolean addTheaterPaymentMethod(Theater_payment_methods theaterPaymentMethod) {
        String sql = "INSERT INTO Theater_payment_methods "
                   + "(payment_method_id, theater_id) "
                   + "VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theaterPaymentMethod.getPayment_method_id());
            ps.setInt(2, theaterPaymentMethod.getTheater_id());
            return ps.executeUpdate() > 0;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTheaterPaymentMethod(int payment_method_id, int theater_id) {
        String sql = "DELETE FROM Theater_payment_methods "
                   + "WHERE payment_method_id = ? "
                   + "AND theater_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment_method_id);
            ps.setInt(2, theater_id);
            return ps.executeUpdate() > 0;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Theater_payment_methods> getByTheaterId(int theater_id) {
        List<Theater_payment_methods> list = new ArrayList<>();
        String sql = "SELECT payment_method_id, theater_id "
                   + "FROM Theater_payment_methods "
                   + "WHERE theater_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theater_id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Theater_payment_methods paymentMethod = new Theater_payment_methods();
                    paymentMethod.setPayment_method_id(rs.getInt("payment_method_id"));
                    paymentMethod.setTheater_id(rs.getInt("theater_id"));
                    list.add(paymentMethod);
                }
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean exists(int payment_method_id, int theater_id) {
        String sql = "SELECT 1 "
                   + "FROM Theater_payment_methods "
                   + "WHERE payment_method_id = ? "
                   + "AND theater_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment_method_id);
            ps.setInt(2, theater_id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
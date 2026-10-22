package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Seat_types;
import com.movieweb.util.DBConnection;

public class Seat_typesDAO {
    public Seat_types getSeatTypeById(int seat_type_id) {
        String sql = "SELECT * FROM Seat_types WHERE seat_type_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seat_type_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Seat_types seat_type = new Seat_types();
                    seat_type.setSeat_type_id(rs.getInt("seat_type_id"));
                    seat_type.setSeat_type_name(rs.getString("seat_type_name"));
                    seat_type.setPrice_modify(rs.getString("price_modify"));
                    return seat_type;
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retrieves all seat types for management dropdowns.
    public List<Seat_types> getAllSeatTypes() throws SQLException {
        List<Seat_types> seatTypes = new ArrayList<>();
        String sql =
            "SELECT seat_type_id, seat_type_name, price_modify " +
            "FROM Seat_types " +
            "ORDER BY seat_type_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Seat_types seatType = new Seat_types();
                seatType.setSeat_type_id(rs.getInt("seat_type_id"));
                seatType.setSeat_type_name(rs.getString("seat_type_name"));
                seatType.setPrice_modify(rs.getString("price_modify"));
                seatTypes.add(seatType);
            }
        }
        return seatTypes;
    }
}
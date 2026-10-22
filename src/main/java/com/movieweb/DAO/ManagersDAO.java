
package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Managers;
import com.movieweb.model.Theaters;
import com.movieweb.util.DBConnection;

public class ManagersDAO {

    // Get all active theaters assigned to a manager
    public List<Theaters> getTheatersByManagerId(int user_id) {
        List<Theaters> theaters = new ArrayList<>();
        String sql =
            "SELECT t.* " +
            "FROM Managers m " +
            "INNER JOIN Theaters t ON t.theater_id = m.theater_id " +
            "WHERE m.user_id = ? " +
            "AND t.isActive = 1 " +
            "AND t.deleted_at IS NULL " +
            "ORDER BY t.theater_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Theaters theater = new Theaters();
                    theater.setTheater_id(rs.getInt("theater_id"));
                    theater.setTheater_name(rs.getString("theater_name"));
                    theater.setTheater_address(rs.getString("theater_address"));
                    theater.setTheater_image_path(rs.getString("theater_image_path"));
                    theater.setDescription(rs.getString("description"));
                    theater.setLatitude(rs.getDouble("latitude"));
                    theater.setLongtitude(rs.getDouble("longtitude"));
                    theater.setOpen_time(rs.getTime("open_time"));
                    theater.setClosing_time(rs.getTime("closing_time"));
                    theater.setActive(rs.getBoolean("isActive"));
                    theater.setDeleted_at(rs.getTimestamp("deleted_at"));
                    theaters.add(theater);
                }
            }

        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return theaters;
    }

    // Get all managers assigned to a theater
    public List<Managers> getManagersByTheaterId(int theater_id) {
        List<Managers> managers = new ArrayList<>();
        String sql =
            "SELECT user_id, theater_id " +
            "FROM Managers " +
            "WHERE theater_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theater_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Managers manager = new Managers();
                    manager.setUser_id(rs.getInt("user_id"));
                    manager.setTheater_id(rs.getInt("theater_id"));
                    managers.add(manager);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return managers;
    }

    // Assign a manager to a theater
    public boolean assignManagerToTheater(int user_id, int theater_id) {
        String sql =
            "INSERT INTO Managers (user_id, theater_id) " +
            "VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, theater_id);
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check whether a manager is assigned to a theater
    public boolean isManagerAssignedToTheater(int user_id, int theater_id) {
        String sql =
            "SELECT 1 " +
            "FROM Managers " +
            "WHERE user_id = ? " +
            "AND theater_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, theater_id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
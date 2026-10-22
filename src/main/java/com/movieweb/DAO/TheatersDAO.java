package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Theaters;
import com.movieweb.util.DBConnection;

public class TheatersDAO {
    // Customer pages: only active, non-deleted theaters
    public Theaters getTheaterById(int theater_id) {
        return findTheaterById(theater_id, true);
    }

    // Unfiltered lookup (hidden/deleted theaters included) for admin use
    public Theaters getTheaterByIdIncludingHidden(int theater_id) {
        return findTheaterById(theater_id, false);
    }

    private Theaters findTheaterById(int theater_id, boolean publicOnly) {
        String sql = "SELECT * FROM Theaters WHERE theater_id = ?"
                + (publicOnly ? " AND isActive = 1 AND deleted_at IS NULL" : "");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theater_id);          
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTheater(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public List<Theaters> getAllTheaters() {
        List<Theaters> theaters = new ArrayList<>();
        String sql = "SELECT * FROM Theaters WHERE isActive = 1 AND deleted_at IS NULL";      
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {            
            while (rs.next()) {
                theaters.add(mapTheater(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return theaters;
    }

    private Theaters mapTheater(ResultSet rs) throws SQLException {
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
        
        return theater;
    }
}
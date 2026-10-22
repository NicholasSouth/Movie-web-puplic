package com.movieweb.DAO;

import com.movieweb.model.Ratings;
import com.movieweb.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RatingsDAO {
    public Ratings getRating(int user_id, int movie_id) {
        String sql =
                "SELECT * " +
                "FROM Ratings " +
                "WHERE user_id = ? AND movie_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, movie_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRating(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addRating(Ratings rating) {
        String sql =
                "INSERT INTO Ratings " +
                "(user_id, movie_id, rating, created_at) " +
                "VALUES (?, ?, ?, GETDATE())";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, rating.getUserId());
            stmt.setInt(2, rating.getMovieId());
            stmt.setInt(3, rating.getRating());
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRating(int user_id, int movie_id, int rating) {
        String sql =
                "UPDATE Ratings " +
                "SET rating = ? " +
                "WHERE user_id = ? AND movie_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, rating);
            stmt.setInt(2, user_id);
            stmt.setInt(3, movie_id);
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getAverageRating(int movie_id) {
        String sql =
                "SELECT AVG(CAST(rating AS DECIMAL(10,2))) AS avg_rating " +
                "FROM Ratings " +
                "WHERE movie_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, movie_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean updateMovieAverageRating(int movie_id, double avg_rating) {
        String sql =
                "UPDATE Movies " +
                "SET avg_rating = ? " +
                "WHERE movie_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setDouble(1, avg_rating);
            stmt.setInt(2, movie_id);
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Ratings mapRating(ResultSet rs) throws SQLException {
        Ratings rating = new Ratings();
        rating.setRatingId(rs.getInt("rating_id"));
        rating.setUserId(rs.getInt("user_id"));
        rating.setMovieId(rs.getInt("movie_id"));
        rating.setRating(rs.getInt("rating"));
        rating.setCreatedAt(rs.getTimestamp("created_at"));
        return rating;
    }
}
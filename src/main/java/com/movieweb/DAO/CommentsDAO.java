package com.movieweb.DAO;

import com.movieweb.model.Comments;
import com.movieweb.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import com.movieweb.model.Users;

public class CommentsDAO {
    public boolean addComment(Comments comment) {
        String sql =
                "INSERT INTO Comments " +
                "(user_id, movie_id, comment_text, created_at) " +
                "VALUES (?, ?, ?, GETDATE())";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, comment.getUserId());
            stmt.setInt(2, comment.getMovieId());
            stmt.setString(3, comment.getCommentText());
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Comments> getCommentsByMovieId(int movie_id) {
        List<Comments> comments = new ArrayList<>();
        String sql =
                "SELECT * " +
                "FROM Comments " +
                "WHERE movie_id = ? " +
                "ORDER BY created_at DESC";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, movie_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapComment(rs));
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public Comments getCommentById(int comment_id) {
        String sql =
                "SELECT * " +
                "FROM Comments " +
                "WHERE comment_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, comment_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapComment(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Map<Integer, Users> getCommentUsersByMovieId(int movie_id) {
        Map<Integer, Users> commentUsers = new LinkedHashMap<>();
        String sql =
                "SELECT DISTINCT u.* " +
                "FROM Users u " +
                "INNER JOIN Comments c " +
                "ON u.user_id = c.user_id " +
                "WHERE c.movie_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, movie_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Users user = new Users();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setAvtPath(rs.getString("avt_path"));
                    commentUsers.put(user.getUserId(), user);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return commentUsers;
    }
    
    private Comments mapComment(ResultSet rs) throws SQLException {
        Comments comment = new Comments();
        comment.setCommentId(rs.getInt("comment_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setMovieId(rs.getInt("movie_id"));
        comment.setCommentText(rs.getString("comment_text"));
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        return comment;
    }
}
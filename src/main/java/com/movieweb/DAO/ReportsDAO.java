package com.movieweb.DAO;

import com.movieweb.model.Reports;
import com.movieweb.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportsDAO {
    public boolean hasReported(int user_id, int comment_id) {
        String sql =
                "SELECT 1 " +
                "FROM Reports " +
                "WHERE user_id = ? AND comment_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, comment_id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addReport(Reports report) {
        String sql =
                "INSERT INTO Reports " +
                "(user_id, comment_id, created_at) " +
                "VALUES (?, ?, GETDATE())";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, report.getUserId());
            stmt.setInt(2, report.getCommentId());
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Reports getReport(int user_id, int comment_id) {
        String sql =
                "SELECT * " +
                "FROM Reports " +
                "WHERE user_id = ? AND comment_id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, comment_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapReport(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Reports mapReport(ResultSet rs)
            throws SQLException {
    	Reports report = new Reports();
        report.setReportId(rs.getInt("report_id"));
        report.setUserId(rs.getInt("user_id"));
        report.setCommentId(rs.getInt("comment_id"));
        report.setCreatedAt(rs.getTimestamp("created_at"));
        return report;
    }
}
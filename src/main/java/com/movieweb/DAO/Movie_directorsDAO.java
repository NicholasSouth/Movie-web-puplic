package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Directors;
import com.movieweb.util.DBConnection;

public class Movie_directorsDAO {
    public List<Directors> getDirectorsByMovieId(int movieId)
            throws SQLException {
        List<Directors> directors = new ArrayList<>();
        String sql = """
                SELECT d.director_id, d.director_name
                FROM Directors d
                INNER JOIN Movie_directors md
                    ON d.director_id = md.director_id
                WHERE md.movie_id = ?
                ORDER BY d.director_name
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Directors director = new Directors();
                    director.setDirector_id(rs.getInt("director_id"));
                    director.setDirector_name(rs.getString("director_name"));
                    directors.add(director);
                }
            }
        }
        return directors;
    }
}
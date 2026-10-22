package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Authors;
import com.movieweb.util.DBConnection;

public class Movie_authorsDAO {
    public List<Authors> getAuthorsByMovieId(int movieId)
            throws SQLException {
        List<Authors> authors = new ArrayList<>();
        String sql = """
                SELECT a.author_id, a.author_name
                FROM Authors a
                INNER JOIN Movie_authors ma
                    ON a.author_id = ma.author_id
                WHERE ma.movie_id = ?
                ORDER BY a.author_name
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Authors author = new Authors();
                    author.setAuthor_id(rs.getInt("author_id"));
                    author.setAuthor_name(rs.getString("author_name"));
                    authors.add(author);
                }
            }
        }
        return authors;
    }
}
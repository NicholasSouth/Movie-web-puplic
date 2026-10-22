package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Movie_genres;
import com.movieweb.util.DBConnection;

public class Movie_genresDAO 
{
    public Movie_genres getMovieGenre(int movie_id, int genre_id) 
    {
        String sql = "SELECT * FROM Movie_genres WHERE movie_id = ? AND genre_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, movie_id);
            stmt.setInt(2, genre_id);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) 
            {
                Movie_genres movie_genre = new Movie_genres();
                movie_genre.setMovie_id(rs.getInt("movie_id"));
                movie_genre.setGenre_id(rs.getInt("genre_id"));
                return movie_genre;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
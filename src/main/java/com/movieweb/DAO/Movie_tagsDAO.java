package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Movie_tags;
import com.movieweb.util.DBConnection;

public class Movie_tagsDAO 
{
    public Movie_tags getMovieTag(int movie_id, int tag_id) 
    {
        String sql = "SELECT * FROM Movie_tags WHERE movie_id = ? AND tag_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, movie_id);
            stmt.setInt(2, tag_id);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) 
            {
                Movie_tags movie_tag = new Movie_tags();
                movie_tag.setMovie_id(rs.getInt("movie_id"));
                movie_tag.setTag_id(rs.getInt("tag_id"));
                return movie_tag;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
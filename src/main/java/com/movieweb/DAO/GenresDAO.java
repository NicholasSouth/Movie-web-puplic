package com.movieweb.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Genres;
import com.movieweb.util.DBConnection;
import java.util.ArrayList;
import java.util.List;

public class GenresDAO 
{
    public Genres getGenreById(int genre_id) 
    {
        String sql = "SELECT * FROM Genres WHERE genre_id = ?";      
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, genre_id);
            ResultSet rs = stmt.executeQuery();           
            if (rs.next()) 
            {
                Genres genre = new Genres();
                genre.setGenre_id(rs.getInt("genre_id"));
                genre.setGenre_name(rs.getString("genre_name"));
                return genre;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }      
        return null;
    }
    
    public List<Genres> getAllGenres() {
        List<Genres> genres = new ArrayList<>();
        String sql = "SELECT genre_id, genre_name " +
                     "FROM Genres " +
                     "ORDER BY genre_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Genres genre = new Genres();
                genre.setGenre_id(rs.getInt("genre_id"));
                genre.setGenre_name(rs.getString("genre_name"));
                genres.add(genre);
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }
}
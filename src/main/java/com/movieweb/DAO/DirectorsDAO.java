package com.movieweb.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Directors;
import com.movieweb.util.DBConnection;

public class DirectorsDAO 
{
    public Directors getDirectorById(int director_id) 
    {
        String sql = "SELECT * FROM Directors WHERE director_id = ?";       
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, director_id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) 
            {
                Directors director = new Directors();
                director.setDirector_id(rs.getInt("director_id"));
                director.setDirector_name(rs.getString("director_name"));
                return director;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }        
        return null;
    }
}
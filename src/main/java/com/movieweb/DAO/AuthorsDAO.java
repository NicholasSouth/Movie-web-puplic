package com.movieweb.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Authors;
import com.movieweb.util.DBConnection;

public class AuthorsDAO 
{
    public Authors getAuthorById(int author_id) 
    {
        String sql = "SELECT * FROM Authors WHERE author_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, author_id);
            ResultSet rs = stmt.executeQuery();            
            if (rs.next()) 
            {
                Authors author = new Authors();
                author.setAuthor_id(rs.getInt("author_id"));
                author.setAuthor_name(rs.getString("author_name"));
                return author;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return null;
    }
}
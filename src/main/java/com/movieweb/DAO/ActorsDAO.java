package com.movieweb.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Actors;
import com.movieweb.util.DBConnection;

public class ActorsDAO 
{
    public Actors getActorById(int actor_id) 
    {
        String sql = "SELECT * FROM Actors WHERE actor_id = ?";    
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, actor_id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) 
            {
                Actors actor = new Actors();
                actor.setActor_id(rs.getInt("actor_id"));
                actor.setActor_name(rs.getString("actor_name"));
                return actor;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }        
        return null;
    }
}
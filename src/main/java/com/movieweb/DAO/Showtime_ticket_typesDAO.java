package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Showtime_ticket_types;
import com.movieweb.util.DBConnection;

public class Showtime_ticket_typesDAO 
{
    public Showtime_ticket_types getShowtimeTicketType(int showtime_id, int ticket_type_id) 
    {
        String sql = "SELECT * FROM Showtime_ticket_types WHERE showtime_id = ? AND ticket_type_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, showtime_id);
            stmt.setInt(2, ticket_type_id);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) 
            {
                Showtime_ticket_types showtime_ticket_type = new Showtime_ticket_types();
                showtime_ticket_type.setShowtime_id(rs.getInt("showtime_id"));
                showtime_ticket_type.setTicket_type_id(rs.getInt("ticket_type_id"));
                showtime_ticket_type.setPrice(rs.getInt("price"));
                
                return showtime_ticket_type;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
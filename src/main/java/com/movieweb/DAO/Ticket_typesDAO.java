package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Ticket_types;
import com.movieweb.util.DBConnection;

public class Ticket_typesDAO 
{
    public Ticket_types getTicketTypeById(int ticket_type_id) 
    {
        String sql = "SELECT * FROM Ticket_types WHERE ticket_type_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, ticket_type_id);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) 
            {
                Ticket_types ticket_type = new Ticket_types();
                ticket_type.setTicket_type_id(rs.getInt("ticket_type_id"));
                ticket_type.setTicket_type_name(rs.getString("ticket_type_name"));
                ticket_type.setPrice_modify(rs.getString("price_modify"));
                
                return ticket_type;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
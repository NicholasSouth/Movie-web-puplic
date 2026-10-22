package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Ads;
import com.movieweb.util.DBConnection;

import java.util.ArrayList; 
import java.util.List;

public class AdsDAO 
{
    public List<Ads> getActiveAdvertisements() 
    {
        List<Ads> advertisements = new ArrayList<>();
        String sql = "SELECT * FROM Ads " +
                     "WHERE isActive = 1 " +
                     "AND deleted_at IS NULL " +
                     "AND (start_at IS NULL OR start_at <= GETDATE()) " +
                     "AND (end_at IS NULL OR end_at >= GETDATE()) " +
                     "ORDER BY ad_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) 
            {
            	Ads ad = new Ads();
                ad.setAd_id(rs.getInt("ad_id"));
                ad.setAd_name(rs.getString("ad_name"));
                ad.setImage_path(rs.getString("ad_image_path"));
                ad.setLink(rs.getString("ad_link"));
                ad.setStart_at(rs.getString("start_at"));
                ad.setEnd_at(rs.getString("end_at"));
                ad.setActive(rs.getBoolean("isActive"));
                ad.setDeleted_at(rs.getString("deleted_at"));
                advertisements.add(ad);
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return advertisements;
    }
}
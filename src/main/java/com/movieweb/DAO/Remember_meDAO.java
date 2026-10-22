package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.movieweb.model.Remember_me;
import com.movieweb.util.DBConnection;

public class Remember_meDAO
{
    //Create token
    public boolean insertToken(Remember_me token)
    {
        String sql =
                "INSERT INTO Remember_me " +
                "(user_id, token_hash, expires_at, created_at) " +
                "VALUES (?, ?, ?, GETDATE())";
        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(
                    1,
                    token.getUserId());
            statement.setString(
                    2,
                    token.getTokenHash());
            statement.setTimestamp(
                    3,
                    token.getExpiresAt());
            return statement.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    //Find valid token
    public Remember_me getValidToken(String tokenHash)
    {
        String sql =
                "SELECT * " +
                "FROM Remember_me " +
                "WHERE token_hash = ? " +
                "AND expires_at > GETDATE()";
        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(
                    1,
                    tokenHash);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    return mapToken(result);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    //Delete 1 token
    public boolean deleteToken(String tokenHash)
    {
        String sql =
                "DELETE FROM Remember_me " +
                "WHERE token_hash = ?";
        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(
                    1,
                    tokenHash);
            return statement.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    //Delete all remember me token if user got deleted
    public boolean deleteTokensByUserId(int userId)
    {
        String sql =
                "DELETE FROM Remember_me " +
                "WHERE user_id = ?";
        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(
                    1,
                    userId);
            return statement.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    //Delete expires token
    public boolean deleteExpiredTokens()
    {
        String sql =
                "DELETE FROM Remember_me " +
                "WHERE expires_at <= GETDATE()";
        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql))
        {
            return statement.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private Remember_me mapToken(ResultSet result)
            throws SQLException
    {
        Remember_me token = new Remember_me();
        token.setTokenId(result.getInt("token_id"));
        token.setUserId(result.getInt("user_id"));
        token.setTokenHash(result.getString("token_hash"));
        token.setExpiresAt(result.getTimestamp("expires_at"));
        token.setCreatedAt(result.getTimestamp("created_at"));
        return token;
    }
}
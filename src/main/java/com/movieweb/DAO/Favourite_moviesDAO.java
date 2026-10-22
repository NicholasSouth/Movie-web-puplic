package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Movies;
import com.movieweb.util.DBConnection;

public class Favourite_moviesDAO
{
    public List<Movies> getFavouriteMovies(
            int userId)
    {
        List<Movies> movies = new ArrayList<>();

        String sql =
                "SELECT m.* " +
                "FROM Favourite_movies f " +
                "INNER JOIN Movies m " +
                "ON f.movie_id = m.movie_id " +
                "WHERE f.user_id = ? " +
                "AND m.deleted_at IS NULL " +
                "ORDER BY f.added_at ASC";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(
                    1,
                    userId);
            try (
                    ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    movies.add(mapMovie(result));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return movies;
    }

    public boolean addFavourite(
            int userId,
            int movieId)
    {
        String sql =
                "INSERT INTO Favourite_movies " +
                "(user_id, movie_id, added_at) " +
                "VALUES (?, ?, GETDATE())";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(
                    1,
                    userId);
            statement.setInt(
                    2,
                    movieId);
            int rows = statement.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    public boolean removeFavourite(
            int userId,
            int movieId)
    {
        String sql =
                "DELETE FROM Favourite_movies " +
                "WHERE user_id = ? " +
                "AND movie_id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(
                    1,
                    userId);
            statement.setInt(
                    2,
                    movieId);
            int rows = statement.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isFavourite(
            int userId,
            int movieId)
    {
        String sql =
                "SELECT 1 " +
                "FROM Favourite_movies " +
                "WHERE user_id = ? " +
                "AND movie_id = ?";

        try (
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(
                    1,
                    userId);
            statement.setInt(
                    2,
                    movieId);
            try (
                    ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private Movies mapMovie(
            ResultSet result)
            throws SQLException
    {
        Movies movie = new Movies();
        movie.setMovie_id(result.getInt("movie_id"));
        movie.setMovie_name(result.getString("movie_name"));
        movie.setDescription(result.getString("description"));
        movie.setAge_rating(result.getString("age_rating"));
        movie.setDuration_minute(result.getInt("duration_minute"));
        movie.setAvailable_from(result.getTimestamp("available_from"));
        movie.setAvailable_until(result.getTimestamp("available_until"));
        movie.setPoster_path(result.getString("poster_path"));
        movie.setTrailer_path(result.getString("trailer_path"));
        movie.setActive(result.getBoolean("isActive"));
        movie.setDeleted_at(result.getTimestamp("deleted_at"));
        return movie;
    }
}
package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.Actors;
import com.movieweb.util.DBConnection;

public class Movie_actorsDAO {
    public List<Actors> getActorsByMovieId(int movieId)
            throws SQLException {
        List<Actors> actors = new ArrayList<>();
        String sql = """
                SELECT a.actor_id, a.actor_name
                FROM Actors a
                INNER JOIN Movie_actors ma
                    ON a.actor_id = ma.actor_id
                WHERE ma.movie_id = ?
                ORDER BY a.actor_name
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Actors actor = new Actors();
                    actor.setActor_id(rs.getInt("actor_id"));
                    actor.setActor_name(rs.getString("actor_name"));
                    actors.add(actor);
                }
            }
        }
        return actors;
    }
}
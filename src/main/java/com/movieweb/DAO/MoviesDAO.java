package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.movieweb.model.Movies;
import com.movieweb.model.Genres;
import com.movieweb.model.Tags;
import com.movieweb.util.DBConnection;

public class MoviesDAO {
    private static final String VISIBLE = "m.isActive = 1 AND m.deleted_at IS NULL";
    private static final String NOW_SHOWING =
            "m.available_from <= GETDATE() " +
            "AND (m.available_until IS NULL " +
            "OR m.available_until >= CAST(GETDATE() AS date))";
    private static final String COMING_SOON = "m.available_from > GETDATE()";
    private static final String MOST_POPULAR = " ORDER BY m.avg_rating DESC, m.movie_id DESC";
    private static final String SHOWTIME_JOINS =
            " FROM Showtimes s " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id";
    private static final String AVAILABLE_SHOWTIME =
            "s.start_at > GETDATE() " +
            "AND UPPER(s.status) = 'SCHEDULED' " +
            "AND r.isActive = 1 " +
            "AND t.isActive = 1 " +
            "AND t.deleted_at IS NULL";
    
    //for movies.jsp
    public List<Movies> getNowShowingMovies() {
        String sql =
                "SELECT TOP (8) m.* " +
                "FROM Movies m " +
                "WHERE " + VISIBLE +
                " AND " + NOW_SHOWING +
                " ORDER BY m.available_from DESC, m.movie_id DESC";
        return fetchMovies(sql);
    }
    public List<Movies> getComingSoonMovies() {
        String sql =
                "SELECT TOP (8) m.* " +
                "FROM Movies m " +
                "WHERE " + VISIBLE +
                " AND " + COMING_SOON +
                " ORDER BY m.available_from ASC, m.movie_id ASC";
        return fetchMovies(sql);
    }
    public List<Movies> getPopularMovies() {
        String sql =
                "SELECT TOP (8) m.* " +
                "FROM Movies m " +
                "WHERE " + VISIBLE +
                " ORDER BY " +
                "(SELECT COUNT(*) " +
                "FROM Favourite_movies f " +
                "WHERE f.movie_id = m.movie_id) DESC, " +
                "m.avg_rating DESC, " +
                "m.movie_id DESC";
        return fetchMovies(sql);
    }
    
    //For main.jsp
    public List<Movies> getTop10Movies() {
        String sql =
                "SELECT TOP 10 m.* " +
                "FROM Movies m " +
                "WHERE " + VISIBLE +
                " ORDER BY m.avg_rating DESC, m.movie_id DESC";
        return fetchMovies(sql);
    }

    //Search and filter
    public List<Movies> searchAndFilterMovies(Movies filter) {        
        StringBuilder q = new StringBuilder(
                "SELECT DISTINCT m.* " +
                "FROM Movies m " +
                "WHERE "
        ).append(VISIBLE);
        List<Object> params = new ArrayList<>();

        // Search by movie, actor, director, or author
        if (filter.getSearch() != null
            && !filter.getSearch().isEmpty()) {
            String searchValue = "%" + escapeLike(filter.getSearch()) + "%";
            q.append(
                    " AND (" +
                    "m.movie_name LIKE ? ESCAPE '\\' " +

                    "OR EXISTS (" +
                    "SELECT 1 " +
                    "FROM Movie_actors ma " +
                    "INNER JOIN Actors a ON a.actor_id = ma.actor_id " +
                    "WHERE ma.movie_id = m.movie_id " +
                    "AND a.actor_name LIKE ? ESCAPE '\\'" +
                    ") " +

                    "OR EXISTS (" +
                    "SELECT 1 " +
                    "FROM Movie_directors md " +
                    "INNER JOIN Directors d ON d.director_id = md.director_id " +
                    "WHERE md.movie_id = m.movie_id " +
                    "AND d.director_name LIKE ? ESCAPE '\\'" +
                    ") " +

                    "OR EXISTS (" +
                    "SELECT 1 " +
                    "FROM Movie_authors mw " +
                    "INNER JOIN Authors w ON w.author_id = mw.author_id " +
                    "WHERE mw.movie_id = m.movie_id " +
                    "AND w.author_name LIKE ? ESCAPE '\\'" +
                    ")" +
                    ")"
            );
            for (int i = 0; i < 4; i++) {
                params.add(searchValue);
            }
        }

        // Genre filter
        if (filter.getGenreId() != null) {
            q.append(
                    " AND EXISTS (" +
                    "SELECT 1 " +
                    "FROM Movie_genres mg " +
                    "WHERE mg.movie_id = m.movie_id " +
                    "AND mg.genre_id = ?" +
                    ")"
            );
            params.add(filter.getGenreId());
        }

        // Tag filter
        if (filter.getTagId() != null) {
            q.append(
                    " AND EXISTS (" +
                    "SELECT 1 " +
                    "FROM Movie_tags mt " +
                    "WHERE mt.movie_id = m.movie_id " +
                    "AND mt.tag_id = ?" +
                    ")"
            );
            params.add(filter.getTagId());
        }

        // Age rating filter
        if (filter.getFilterAgeRating() != null
            && !filter.getFilterAgeRating().isEmpty()) {
            q.append(" AND m.age_rating = ?");
            params.add(filter.getFilterAgeRating());
        }

        // Minimum rating filter
        if (filter.getMinRating() != null) {
            q.append(" AND m.avg_rating >= ?");
            params.add(filter.getMinRating());
        }

        // Status filter
        if ("now-showing".equals(filter.getStatus())) {
            q.append(" AND ").append(NOW_SHOWING);
        } 
        else if ("coming-soon".equals(filter.getStatus())) {
            q.append(" AND ").append(COMING_SOON);
        }

        // Showtime-related filters
        if (filter.getTheaterId() != null
            || filter.getMaxPrice() != null) {
            q.append(" AND EXISTS (SELECT 1").append(SHOWTIME_JOINS).append(
                    " WHERE s.movie_id = m.movie_id " +
                    "AND ").append(AVAILABLE_SHOWTIME);

            // Theater filter
            if (filter.getTheaterId() != null) {
                q.append(" AND t.theater_id = ?");
                params.add(filter.getTheaterId());
            }          

            // Maximum ticket price filter
            if (filter.getMaxPrice() != null) {
                q.append(" AND EXISTS (SELECT 1 FROM Showtime_ticket_types stt " +
                        "WHERE stt.showtime_id = s.showtime_id " +
                        "AND stt.ticket_type_id = 1 " +
                        "AND stt.price <= ?)");
                params.add(filter.getMaxPrice());
            }
            q.append(")");
        }
        // Ordering
        if ("popular".equals(filter.getStatus())) {
            q.append(MOST_POPULAR);
        } 
        else {
            q.append(" ORDER BY m.movie_id DESC");
        }
        return fetchMovies(q.toString(), params);
    }

    // Customer pages: only active, non-deleted movies
    public Movies getMovieById(int movieId) {
        return findMovieById(movieId, true);
    }

    // Unfiltered lookup (hidden/deleted movies included) for admin use
    public Movies getMovieByIdIncludingHidden(int movieId) {
        return findMovieById(movieId, false);
    }

    private Movies findMovieById(int movieId, boolean publicOnly) {
        String sql =
                "SELECT * " +
                "FROM Movies " +
                "WHERE movie_id = ?" +
                (publicOnly ? " AND isActive = 1 AND deleted_at IS NULL" : "");
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Movies movie = mapMovie(rs);

                    // Load genres and tags for this individual movie
                    List<Movies> movies = new ArrayList<>();
                    movies.add(movie);
                    loadGenresAndTags(movies, conn);
                    return movie;
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setFavourite(
            int userId,
            int movieId,
            boolean add
    ) {

        String sql = add
                ? "IF NOT EXISTS (" +
                  "SELECT 1 FROM Favourite_movies " +
                  "WHERE user_id = ? AND movie_id = ?" +
                  ") " +
                  "INSERT INTO Favourite_movies " +
                  "(user_id, movie_id) VALUES (?, ?)"

                : "DELETE FROM Favourite_movies " +
                  "WHERE user_id = ? AND movie_id = ?";
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            if (add) {
                stmt.setInt(3, userId);
                stmt.setInt(4, movieId);
            }
            return stmt.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //To avoid N+1 query problem
    private List<Movies> fetchMovies(String sql) {
        List<Movies> movies = new ArrayList<>();
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                movies.add(mapMovie(rs));
            }

            // Only two additional queries:
            // one for all genres and one for all tags
            loadGenresAndTags(movies, conn);
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
    
    //Overload method cuz we need to pass parameters
    private List<Movies> fetchMovies(String sql, List<Object> params) {
        List<Movies> movies = new ArrayList<>();
        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            // Bind parameters in the same order they were added
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(mapMovie(rs));
                }
            }

            // Avoid N+1 query problem
            loadGenresAndTags(movies, conn);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
    
    //Batch load genres and tags
    private void loadGenresAndTags(
            List<Movies> movies,
            Connection conn
    ) throws SQLException {
        if (movies == null || movies.isEmpty()) {
            return;
        }
        Map<Integer, Movies> movieMap = new HashMap<>();
        for (Movies movie : movies) {
            movieMap.put(movie.getMovie_id(), movie);
        }
        loadGenres(movies, movieMap, conn);
        loadTags(movies, movieMap, conn);
    }
    private void loadGenres(
            List<Movies> movies,
            Map<Integer, Movies> movieMap,
            Connection conn
    ) throws SQLException {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < movies.size(); i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }
        String sql =
                "SELECT " +
                "mg.movie_id, " +
                "g.genre_id, " +
                "g.genre_name " +
                "FROM Movie_genres mg " +
                "INNER JOIN Genres g " +
                "ON g.genre_id = mg.genre_id " +
                "WHERE mg.movie_id IN (" +
                placeholders +
                ") " +
                "ORDER BY g.genre_name ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < movies.size(); i++) {
                stmt.setInt(
                        i + 1,
                        movies.get(i).getMovie_id()
                );
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movies movie = movieMap.get(rs.getInt("movie_id"));
                    if (movie == null) {
                        continue;
                    }
                    Genres genre = new Genres();
                    genre.setGenre_id(rs.getInt("genre_id"));
                    genre.setGenre_name(rs.getString("genre_name"));
                    movie.getGenres().add(genre);
                }
            }
        }
    }
    private void loadTags(
            List<Movies> movies,
            Map<Integer, Movies> movieMap,
            Connection conn
    ) throws SQLException {
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < movies.size(); i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }
        String sql =
                "SELECT " +
                "mt.movie_id, " +
                "t.tag_id, " +
                "t.tag_name " +
                "FROM Movie_tags mt " +
                "INNER JOIN Tags t " +
                "ON t.tag_id = mt.tag_id " +
                "WHERE mt.movie_id IN (" +
                placeholders +
                ") " +
                "ORDER BY t.tag_name ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < movies.size(); i++) {
                stmt.setInt(
                        i + 1,
                        movies.get(i).getMovie_id()
                );
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movies movie = movieMap.get(rs.getInt("movie_id"));
                    if (movie == null) {
                        continue;
                    }
                    Tags tag = new Tags();
                    tag.setTag_id(rs.getInt("tag_id"));
                    tag.setTag_name(rs.getString("tag_name"));
                    movie.getTags().add(tag);
                }
            }
        }
    }

    // for theater_details.jsp
    public List<Movies> getMoviesByIds(List<Integer> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < movieIds.size(); i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }
        String sql =
            "SELECT m.* " +
            "FROM Movies m " +
            "WHERE " + VISIBLE + " " +
            "AND m.movie_id IN (" + placeholders + ") " +
            "ORDER BY m.movie_id ASC";
        List<Object> params = new ArrayList<>();
        for (Integer movieId : movieIds) {
            params.add(movieId);
        }
        return fetchMovies(sql, params);
    }
    
    private Movies mapMovie(ResultSet rs)
            throws SQLException {
        Movies m = new Movies();
        m.setMovie_id(rs.getInt("movie_id"));
        m.setMovie_name(rs.getString("movie_name"));
        m.setDescription(rs.getString("description"));
        m.setAge_rating(rs.getString("age_rating"));
        m.setAvg_rating(rs.getDouble("avg_rating"));
        m.setDuration_minute(rs.getInt("duration_minute"));
        m.setAvailable_from(rs.getTimestamp("available_from"));
        m.setAvailable_until(rs.getTimestamp("available_until"));
        m.setPoster_path(rs.getString("poster_path"));
        m.setTrailer_path(rs.getString("trailer_path"));
        m.setTrailer_link(rs.getString("trailer_link"));
        m.setActive(rs.getBoolean("isActive"));
        m.setDeleted_at(rs.getTimestamp("deleted_at"));
        return m;
    }

    private String escapeLike(String val) {
        return val
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
    }

	 // For manager_showtimes: every movie that hasn't passed its available_until date.
	 public List<Movies> getSchedulableMovies() {
	     String sql =
	             "SELECT m.* " +
	             "FROM Movies m " +
	             "WHERE " + VISIBLE +
	             " AND (m.available_until IS NULL " +
	             "OR m.available_until >= CAST(GETDATE() AS date)) " +
	             "ORDER BY m.movie_name ASC";
	     return fetchMovies(sql);
	 }
}
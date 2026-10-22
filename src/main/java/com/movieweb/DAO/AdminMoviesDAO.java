package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.movieweb.model.AdminMovieInput;
import com.movieweb.model.AdminMovieRow;
import com.movieweb.util.DBConnection;

/* Admin-side movie administration (list, add and edit). The customer pages read movies through MoviesDAO.
 * Every method propagates SQLException. Only fixed table and column names are concatenated into the SQL;
 * every value from the form is bound as a parameter.
 * Adding a movie touches up to seven tables, so create() runs in ONE transaction:
 *   Movies, Movie_genres, Movie_tags, and for the cast Actors/Authors/Directors (found by name, or created)
 *   plus their link tables Movie_actors/Movie_authors/Movie_directors.
 * If anything fails, nothing is saved.
 */
public class AdminMoviesDAO {

    private static final String FROM_LIVE = " FROM Movies m WHERE m.deleted_at IS NULL";

    private String searchClause(String search) {
        return (search == null || search.isEmpty()) ? "" : " AND m.movie_name LIKE ?";
    }

    private int bindSearch(PreparedStatement ps, int i, String search) throws SQLException {
        if (search != null && !search.isEmpty()) {
            ps.setString(i++, "%" + search + "%");
        }
        return i;
    }

    public int count(String search) throws SQLException {
        String sql = "SELECT COUNT(*)" + FROM_LIVE + searchClause(search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSearch(ps, 1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* Newest movie first. */
    public List<AdminMovieRow> list(String search, int offset, int limit) throws SQLException {
        String sql = "SELECT m.movie_id, m.movie_name, m.age_rating, m.duration_minute, m.available_from, "
                   + "m.available_until, m.poster_path, m.isActive" + FROM_LIVE + searchClause(search)
                   + " ORDER BY m.movie_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminMovieRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = bindSearch(ps, 1, search);
            ps.setInt(i++, offset);
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminMovieRow r = new AdminMovieRow();
                    r.setMovieId(rs.getInt("movie_id"));
                    r.setName(rs.getString("movie_name"));
                    r.setAgeRating(rs.getString("age_rating"));
                    r.setDurationMinute(rs.getInt("duration_minute"));
                    r.setAvailableFrom(rs.getTimestamp("available_from"));
                    r.setAvailableUntil(rs.getTimestamp("available_until"));
                    r.setPosterPath(rs.getString("poster_path"));
                    r.setActive(rs.getBoolean("isActive"));
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    /* True when a live (not deleted) movie other than excludeMovieId already has this exact name
     * (the database collation decides case). Use excludeMovieId = 0 when adding. */
    public boolean nameExists(String name, int excludeMovieId) throws SQLException {
        String sql = "SELECT 1" + FROM_LIVE + " AND m.movie_name = ? AND m.movie_id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeMovieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* Loads one live movie with its genres, tags and cast names, to prefill the edit form. Null if it is gone. */
    public AdminMovieInput getById(int movieId) throws SQLException {
        String sql = "SELECT m.movie_name, m.description, m.age_rating, m.duration_minute, m.available_from, "
                   + "m.available_until, m.poster_path, m.trailer_link, m.isActive" + FROM_LIVE + " AND m.movie_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            AdminMovieInput in = new AdminMovieInput();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    in.movieId = movieId;
                    in.name = rs.getString("movie_name");
                    in.description = rs.getString("description");
                    in.ageRating = rs.getString("age_rating");
                    in.durationMinute = rs.getInt("duration_minute");
                    in.availableFrom = rs.getTimestamp("available_from");
                    in.availableUntil = rs.getTimestamp("available_until");
                    in.posterPath = rs.getString("poster_path");
                    in.trailerLink = rs.getString("trailer_link");
                    in.active = rs.getBoolean("isActive");
                }
            }
            in.genreIds = readIds(conn, "SELECT genre_id FROM Movie_genres WHERE movie_id = ?", movieId);
            in.tagIds = readIds(conn, "SELECT tag_id FROM Movie_tags WHERE movie_id = ?", movieId);
            in.actors = readNames(conn, "SELECT a.actor_name FROM Movie_actors l JOIN Actors a ON a.actor_id = l.actor_id "
                    + "WHERE l.movie_id = ? ORDER BY a.actor_name", movieId);
            in.directors = readNames(conn, "SELECT d.director_name FROM Movie_directors l JOIN Directors d ON d.director_id = l.director_id "
                    + "WHERE l.movie_id = ? ORDER BY d.director_name", movieId);
            in.authors = readNames(conn, "SELECT a.author_name FROM Movie_authors l JOIN Authors a ON a.author_id = l.author_id "
                    + "WHERE l.movie_id = ? ORDER BY a.author_name", movieId);
            return in;
        }
    }

    private List<Integer> readIds(Connection conn, String sql, int movieId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    private List<String> readNames(Connection conn, String sql, int movieId) throws SQLException {
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
            }
        }
        return names;
    }

    /* Saves the edited details of in.movieId. A null in.posterPath keeps the current poster.
     * The genre, tag and cast links are replaced by the submitted ones (delete, then insert) in the same
     * transaction as the movie row. Returns false (and changes nothing) if the movie no longer exists.
     * Existing showtimes are not touched: changing the duration does not move their end times.
     * avg_rating, comments, ratings and favourites are not touched either.
     */
    public boolean update(AdminMovieInput in) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!updateMovie(conn, in)) {
                    conn.rollback();
                    return false;
                }
                for (String link : new String[] {"Movie_genres", "Movie_tags", "Movie_actors", "Movie_directors", "Movie_authors"}) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + link + " WHERE movie_id = ?")) {
                        ps.setInt(1, in.movieId);
                        ps.executeUpdate();
                    }
                }
                linkExisting(conn, "Movie_genres", "genre_id", "Genres", in.movieId, in.genreIds);
                linkExisting(conn, "Movie_tags", "tag_id", "Tags", in.movieId, in.tagIds);
                linkPeople(conn, "Actors", "actor_id", "actor_name", "Movie_actors", in.movieId, in.actors);
                linkPeople(conn, "Directors", "director_id", "director_name", "Movie_directors", in.movieId, in.directors);
                linkPeople(conn, "Authors", "author_id", "author_name", "Movie_authors", in.movieId, in.authors);
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean updateMovie(Connection conn, AdminMovieInput in) throws SQLException {
        String sql = "UPDATE Movies SET movie_name = ?, description = ?, age_rating = ?, duration_minute = ?, "
                   + "available_from = ?, available_until = ?, poster_path = COALESCE(?, poster_path), "
                   + "trailer_link = ?, isActive = ? WHERE movie_id = ? AND deleted_at IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, in.name);
            setNullableString(ps, 2, in.description);
            setNullableString(ps, 3, in.ageRating);
            ps.setInt(4, in.durationMinute);
            ps.setTimestamp(5, in.availableFrom);
            if (in.availableUntil == null) {
                ps.setNull(6, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(6, in.availableUntil);
            }
            if (in.posterPath == null) {
                ps.setNull(7, Types.NVARCHAR);
            } else {
                ps.setString(7, in.posterPath);
            }
            setNullableString(ps, 8, in.trailerLink);
            ps.setBoolean(9, in.active);
            ps.setInt(10, in.movieId);
            return ps.executeUpdate() == 1;
        }
    }

    /* Saves the movie with its genres, tags and cast. Returns the new movie_id. */
    public int create(AdminMovieInput in) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int movieId = insertMovie(conn, in);
                linkExisting(conn, "Movie_genres", "genre_id", "Genres", movieId, in.genreIds);
                linkExisting(conn, "Movie_tags", "tag_id", "Tags", movieId, in.tagIds);
                linkPeople(conn, "Actors", "actor_id", "actor_name", "Movie_actors", movieId, in.actors);
                linkPeople(conn, "Directors", "director_id", "director_name", "Movie_directors", movieId, in.directors);
                linkPeople(conn, "Authors", "author_id", "author_name", "Movie_authors", movieId, in.authors);
                conn.commit();
                return movieId;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /* avg_rating is left out on purpose: a new movie has no ratings yet (RatingsService fills it in later). */
    private int insertMovie(Connection conn, AdminMovieInput in) throws SQLException {
        String sql = "INSERT INTO Movies (movie_name, description, age_rating, duration_minute, available_from, "
                   + "available_until, poster_path, trailer_link, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, in.name);
            setNullableString(ps, 2, in.description);
            setNullableString(ps, 3, in.ageRating);
            ps.setInt(4, in.durationMinute);
            ps.setTimestamp(5, in.availableFrom);
            if (in.availableUntil == null) {
                ps.setNull(6, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(6, in.availableUntil);
            }
            ps.setString(7, in.posterPath);
            setNullableString(ps, 8, in.trailerLink);
            ps.setBoolean(9, in.active);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("The movie row was not inserted.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("The new movie_id could not be read back (is Movies.movie_id an identity column?).");
    }

    /* Genres and tags: the admin picked existing rows. The SELECT only inserts the link when that id really exists. */
    private void linkExisting(Connection conn, String linkTable, String idColumn, String lookupTable,
                              int movieId, List<Integer> ids) throws SQLException {
        String sql = "INSERT INTO " + linkTable + " (movie_id, " + idColumn + ") SELECT ?, " + idColumn
                   + " FROM " + lookupTable + " WHERE " + idColumn + " = ?";
        Set<Integer> done = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer id : ids) {
                if (id == null || !done.add(id)) {
                    continue;
                }
                ps.setInt(1, movieId);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        }
    }

    /* Actors, directors, authors: typed as names. Reuse the existing row with that name, or create it. */
    private void linkPeople(Connection conn, String table, String idColumn, String nameColumn,
                            String linkTable, int movieId, List<String> names) throws SQLException {
        Set<Integer> linked = new HashSet<>(); // two spellings can map to one row under a case/accent-insensitive collation
        for (String name : names) {
            int id = findPerson(conn, table, idColumn, nameColumn, name);
            if (id == 0) {
                id = insertPerson(conn, table, nameColumn, name);
            }
            if (!linked.add(id)) {
                continue;
            }
            String sql = "INSERT INTO " + linkTable + " (movie_id, " + idColumn + ") VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, movieId);
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        }
    }

    private int findPerson(Connection conn, String table, String idColumn, String nameColumn, String name)
            throws SQLException {
        String sql = "SELECT TOP 1 " + idColumn + " FROM " + table + " WHERE " + nameColumn + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int insertPerson(Connection conn, String table, String nameColumn, String name) throws SQLException {
        String sql = "INSERT INTO " + table + " (" + nameColumn + ") VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("The new id in " + table + " could not be read back.");
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}

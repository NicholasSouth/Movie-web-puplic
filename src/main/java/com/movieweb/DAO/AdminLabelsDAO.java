package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminLabelRow;
import com.movieweb.util.DBConnection;

/* Admin-side management of the two label tables, Genres and Tags (searchable paged list, add, delete). Both have the same shape, so one DAO
 * serves both, selected by a fixed kind ("genre" or "tag"); the table and column names come from the
 * constants below, never from the request. Every method propagates SQLException.
 *   Genres(genre_id, genre_name) <- Movie_genres(movie_id, genre_id)
 *   Tags(tag_id, tag_name)       <- Movie_tags(movie_id, tag_id)
 * These tables have no deleted_at column, so delete is a real delete. Because the link tables reference them,
 * delete() first removes the links, in the same transaction.
 */
public class AdminLabelsDAO {
    public static final String GENRE = "genre";
    public static final String TAG = "tag";

    /* table, id column, name column, link table */
    private String[] spec(String kind) {
        if (GENRE.equals(kind)) {
            return new String[] {"Genres", "genre_id", "genre_name", "Movie_genres"};
        }
        if (TAG.equals(kind)) {
            return new String[] {"Tags", "tag_id", "tag_name", "Movie_tags"};
        }
        throw new IllegalArgumentException("Unknown kind: " + kind);
    }

    /* Search text -> LIKE pattern. The % _ [ characters typed by the admin are escaped so they match literally. */
    private String likePattern(String search) {
        return "%" + search.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_").replace("[", "\\[") + "%";
    }

    private String whereClause(String[] t, String search) {
        return (search == null || search.isEmpty()) ? "" : " WHERE x." + t[2] + " LIKE ? ESCAPE '\\'";
    }

    /* How many labels of this kind match the search (empty search = all), for paging. */
    public int count(String kind, String search) throws SQLException {
        String[] t = spec(kind);
        String sql = "SELECT COUNT(*) FROM " + t[0] + " x" + whereClause(t, search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (search != null && !search.isEmpty()) {
                ps.setString(1, likePattern(search));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* One page of labels whose name contains the search text, by name, each with the number of live
     * (not deleted) movies that use it. */
    public List<AdminLabelRow> list(String kind, String search, int offset, int limit) throws SQLException {
        String[] t = spec(kind);
        String sql = "SELECT x." + t[1] + ", x." + t[2] + ", COUNT(m.movie_id) AS movie_count "
                   + "FROM " + t[0] + " x "
                   + "LEFT JOIN " + t[3] + " l ON l." + t[1] + " = x." + t[1] + " "
                   + "LEFT JOIN Movies m ON m.movie_id = l.movie_id AND m.deleted_at IS NULL"
                   + whereClause(t, search)
                   + " GROUP BY x." + t[1] + ", x." + t[2] + " ORDER BY x." + t[2] + ", x." + t[1]
                   + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminLabelRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            if (search != null && !search.isEmpty()) {
                ps.setString(i++, likePattern(search));
            }
            ps.setInt(i++, offset);
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminLabelRow r = new AdminLabelRow();
                    r.setId(rs.getInt(1));
                    r.setName(rs.getString(2));
                    r.setMovieCount(rs.getInt(3));
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    /* Adds a label. Returns false when a label with this name already exists (the database collation decides
     * about upper/lower case). The existence check and the insert are one statement. */
    public boolean add(String kind, String name) throws SQLException {
        String[] t = spec(kind);
        String sql = "INSERT INTO " + t[0] + " (" + t[2] + ") SELECT ? WHERE NOT EXISTS "
                   + "(SELECT 1 FROM " + t[0] + " WHERE " + t[2] + " = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, name);
            return ps.executeUpdate() == 1;
        }
    }

    /* Deletes a label and removes it from every movie that used it. Returns how many movie links were removed,
     * or -1 when the label no longer exists (nothing changed). */
    public int delete(String kind, int id) throws SQLException {
        String[] t = spec(kind);
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int links;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + t[3] + " WHERE " + t[1] + " = ?")) {
                    ps.setInt(1, id);
                    links = ps.executeUpdate();
                }
                int removed;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + t[0] + " WHERE " + t[1] + " = ?")) {
                    ps.setInt(1, id);
                    removed = ps.executeUpdate();
                }
                if (removed != 1) {
                    conn.rollback();
                    return -1;
                }
                conn.commit();
                return links;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}

package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.movieweb.model.Showtimes;
import com.movieweb.model.Theaters;
import com.movieweb.util.DBConnection;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.sql.Statement;

import java.util.LinkedHashMap;

public class ShowtimesDAO {
    public Showtimes getShowtimeById(int showtime_id) {
        String sql = "SELECT * FROM Showtimes WHERE showtime_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showtime_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapShowtime(rs);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //for theater_details.jsp
    public List<Showtimes> getShowtimesByTheaterAndDate(
            int theaterId,
            LocalDate date) {
        List<Showtimes> showtimes = new ArrayList<>();
        String sql =
            "SELECT s.* " +
            "FROM Showtimes s " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "WHERE t.theater_id = ? " +
            "AND r.isActive = 1 " +
            "AND r.deleted_at IS NULL " +
            "AND s.deleted_at IS NULL " +
            "AND t.isActive = 1 " +
            "AND t.deleted_at IS NULL " +
            "AND UPPER(s.status) = 'SCHEDULED' " +
            "AND s.start_at >= ? " +
            "AND s.start_at < ? " +
            "ORDER BY s.start_at ASC";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            /*Start of selected day.*/
            stmt.setTimestamp(
                2,
                java.sql.Timestamp.valueOf(date.atStartOfDay())
            );

            /* Start of the next day.
             * Using < next day instead of <= 23:59:59 avoids problems with milliseconds.*/
            stmt.setTimestamp(
                3,
                java.sql.Timestamp.valueOf(
                    date.plusDays(1).atStartOfDay()
                )
            );
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    showtimes.add(mapShowtime(rs));
                }
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }
    
    //for movie_details.jsp
    public Map<Showtimes, Theaters> getTop5ShowtimesByMovieId(int movieId) {
        Map<Showtimes, Theaters> showtimes = new LinkedHashMap<>();
        String sql =
                "SELECT TOP 5 " +
                "s.showtime_id, " +
                "s.room_id, " +
                "s.movie_id, " +
                "s.start_at, " +
                "s.end_at, " +
                "s.status, " +
                "t.theater_id, " +
                "t.theater_name, " +
                "t.theater_address, " +
                "t.theater_image_path, " +
                "t.description, " +
                "t.latitude, " +
                "t.longtitude, " +
                "t.open_time, " +
                "t.closing_time, " +
                "t.isActive, " +
                "t.deleted_at " +
                "FROM Showtimes s " +
                "INNER JOIN Rooms r ON r.room_id = s.room_id " +
                "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
                "WHERE s.movie_id = ? " +
                "AND r.isActive = 1 " +
                "AND r.deleted_at IS NULL " +
                "AND s.deleted_at IS NULL " +
                "AND t.isActive = 1 " +
                "AND t.deleted_at IS NULL " +
                "AND UPPER(s.status) = 'SCHEDULED' " +
                "AND s.start_at >= GETDATE() " +
                "ORDER BY s.start_at ASC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Showtimes showtime = new Showtimes();
                    showtime.setShowtime_id(rs.getInt("showtime_id"));
                    showtime.setRoom_id(rs.getInt("room_id"));
                    showtime.setMovie_id(rs.getInt("movie_id"));
                    showtime.setStart_at(rs.getTimestamp("start_at"));
                    showtime.setEnd_at(rs.getTimestamp("end_at"));
                    Theaters theater = new Theaters();
                    theater.setTheater_id(rs.getInt("theater_id"));
                    theater.setTheater_name(rs.getString("theater_name"));
                    theater.setTheater_address(rs.getString("theater_address"));
                    theater.setTheater_image_path(rs.getString("theater_image_path"));
                    theater.setDescription(rs.getString("description"));
                    theater.setLatitude(rs.getDouble("latitude"));
                    theater.setLongtitude(rs.getDouble("longtitude"));
                    theater.setOpen_time(rs.getTime("open_time"));
                    theater.setClosing_time(rs.getTime("closing_time"));
                    theater.setActive(rs.getBoolean("isActive"));
                    theater.setDeleted_at(rs.getTimestamp("deleted_at"));
                    showtimes.put(showtime, theater);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }
    
    //for booking_showtimes.jsp
    public List<Theaters> getTheatersByMovieId(int movieId) {
        List<Theaters> theaters = new ArrayList<>();
        String sql =
            "SELECT DISTINCT t.* " +
            "FROM Theaters t " +
            "INNER JOIN Rooms r ON r.theater_id = t.theater_id " +
            "INNER JOIN Showtimes s ON s.room_id = r.room_id " +
            "WHERE s.movie_id = ? " +
            "AND r.isActive = 1 " +
            "AND r.deleted_at IS NULL " +
            "AND s.deleted_at IS NULL " +
            "AND t.isActive = 1 " +
            "AND t.deleted_at IS NULL " +
            "AND UPPER(s.status) = 'SCHEDULED' " +
            "AND s.start_at >= GETDATE() " +
            "AND s.start_at < DATEADD(DAY, 7, CONVERT(date, GETDATE())) " +
            "ORDER BY t.theater_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Theaters theater = new Theaters();
                    theater.setTheater_id(rs.getInt("theater_id"));
                    theater.setTheater_name(rs.getString("theater_name"));
                    theater.setTheater_address(rs.getString("theater_address"));
                    theater.setTheater_image_path(rs.getString("theater_image_path"));
                    theater.setDescription(rs.getString("description"));
                    theater.setLatitude(rs.getDouble("latitude"));
                    theater.setLongtitude(rs.getDouble("longtitude"));
                    theater.setOpen_time(rs.getTime("open_time"));
                    theater.setClosing_time(rs.getTime("closing_time"));
                    theater.setActive(rs.getBoolean("isActive"));
                    theater.setDeleted_at(rs.getTimestamp("deleted_at"));
                    theaters.add(theater);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return theaters;
    }
    public List<Showtimes> getShowtimesByMovieTheaterAndDate(int movieId, int theaterId, LocalDate date) {
        List<Showtimes> showtimes = new ArrayList<>();
        LocalDate today = LocalDate.now();
        java.time.LocalDateTime startDateTime = date.equals(today)
                ? java.time.LocalDateTime.now()
                : date.atStartOfDay();
        java.time.LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
        String sql =
            "SELECT s.*, " +
            "r.room_name, " +
            "rt.room_type_name " +
            "FROM Showtimes s " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Room_types rt ON rt.room_type_id = r.room_type_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "WHERE s.movie_id = ? " +
            "AND t.theater_id = ? " +
            "AND r.isActive = 1 " +
            "AND r.deleted_at IS NULL " +
            "AND s.deleted_at IS NULL " +
            "AND t.isActive = 1 " +
            "AND t.deleted_at IS NULL " +
            "AND UPPER(s.status) = 'SCHEDULED' " +
            "AND s.start_at >= ? " +
            "AND s.start_at < ? " +
            "ORDER BY s.start_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            stmt.setInt(2, theaterId);
            stmt.setTimestamp(3, Timestamp.valueOf(startDateTime));
            stmt.setTimestamp(4, Timestamp.valueOf(endDateTime));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Showtimes showtime = mapShowtime(rs);

                    // Attach room information to this showtime.
                    showtime.setRoom_name(rs.getString("room_name"));
                    showtime.setRoom_type_name(rs.getString("room_type_name"));
                    showtimes.add(showtime);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }
    
    private Showtimes mapShowtime(ResultSet rs) throws SQLException {
        Showtimes showtime = new Showtimes();
        showtime.setShowtime_id(rs.getInt("showtime_id"));
        showtime.setRoom_id(rs.getInt("room_id"));
        showtime.setMovie_id(rs.getInt("movie_id"));
        showtime.setStart_at(rs.getTimestamp("start_at"));
        showtime.setEnd_at(rs.getTimestamp("end_at"));
        showtime.setStatus(rs.getString("status"));
        showtime.setDeleted_at(rs.getTimestamp("deleted_at"));
        return showtime;
    }

    //for soft deleting showtimes
	public int softDeleteEligibleShowtimesByMovieAndTheater(int movieId, int theaterId) 
			throws SQLException {
	    String sql =
	        "UPDATE s " +
	        "SET s.deleted_at = GETDATE() " +
	        "FROM Showtimes s " +
	        "INNER JOIN Rooms r ON r.room_id = s.room_id " +
	        "WHERE s.movie_id = ? " +
	        "AND r.theater_id = ? " +
	        "AND s.deleted_at IS NULL " +
	        "AND s.end_at >= GETDATE() " +
	        "AND NOT EXISTS ( " +
	        "    SELECT 1 " +
	        "    FROM Booking_showtimes b " +
	        "    INNER JOIN Payments p " +
	        "        ON p.booking_id = b.booking_id " +
	        "    WHERE b.showtime_id = s.showtime_id " +
	        "    AND p.status = 'SUCCESS' " +
	        ")";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, movieId);
	        stmt.setInt(2, theaterId);
	        return stmt.executeUpdate();
	    }
	}

	public boolean softDeleteManagerShowtime(
	        int userId,
	        int theaterId,
	        int showtimeId) throws SQLException {
	    String sql =
	        "UPDATE s " +
	        "SET s.deleted_at = GETDATE() " +
	        "FROM Showtimes s " +
	        "INNER JOIN Rooms r ON r.room_id = s.room_id " +
	        "WHERE s.showtime_id = ? " +
	        "AND r.theater_id = ? " +
	        "AND s.deleted_at IS NULL " +
	        "AND EXISTS ( " +
	        "    SELECT 1 FROM Managers m " +
	        "    WHERE m.user_id = ? " +
	        "    AND m.theater_id = r.theater_id " +
	        ") " +
	        "AND NOT EXISTS ( " +
	        "    SELECT 1 " +
	        "    FROM Booking_showtimes b " +
	        "    INNER JOIN Payments p ON p.booking_id = b.booking_id " +
	        "    WHERE b.showtime_id = s.showtime_id " +
	        "    AND UPPER(p.status) = 'SUCCESS' " +
	        ")";
	    try (Connection conn = DBConnection.getConnection()) {
	        conn.setAutoCommit(false);
	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, showtimeId);
	            stmt.setInt(2, theaterId);
	            stmt.setInt(3, userId);
	            int affectedRows = stmt.executeUpdate();
	            conn.commit();
	
	            // true means exactly one showtime was soft-deleted.
	            return affectedRows == 1;
	        } 
	        catch (SQLException | RuntimeException e) {
	            conn.rollback();
	            throw e;
	        } 
	        finally {
	            conn.setAutoCommit(true);
	        }
	    }
	}
	
	//need seperate fetcher cuz manager needs different infor than theater_details.jsp
	public List<Showtimes> getManagerShowtimesByTheaterAndDate(int theaterId, LocalDate date) {
	    List<Showtimes> showtimes = new ArrayList<>();
	    String sql =
	        "SELECT s.* " +
	        "FROM Showtimes s " +
	        "INNER JOIN Rooms r ON r.room_id = s.room_id " +
	        "WHERE r.theater_id = ? " +
	        "AND r.deleted_at IS NULL " +
	        "AND s.deleted_at IS NULL " +
	        "AND s.start_at >= ? " +
	        "AND s.start_at < ? " +
	        "ORDER BY s.start_at ASC";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, theaterId);
	        stmt.setTimestamp(2, Timestamp.valueOf(date.atStartOfDay()));
	        stmt.setTimestamp(3, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                showtimes.add(mapShowtime(rs));
	            }
	        }
	    } 
	    catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return showtimes;
	}

	public List<Integer> getEligibleMovieIdsByTheater(int theaterId) {
	    List<Integer> movieIds = new ArrayList<>();
	    String sql =
	        "SELECT DISTINCT s.movie_id " +
	        "FROM Showtimes s " +
	        "INNER JOIN Rooms r ON r.room_id = s.room_id " +
	        "WHERE r.theater_id = ? " +
	        "AND r.deleted_at IS NULL " +
	        "AND s.deleted_at IS NULL " +
	        "AND s.end_at >= GETDATE() " +
	        "AND NOT EXISTS ( " +
	        "    SELECT 1 " +
	        "    FROM Booking_showtimes b " +
	        "    INNER JOIN Payments p " +
	        "        ON p.booking_id = b.booking_id " +
	        "    WHERE b.showtime_id = s.showtime_id " +
	        "    AND p.status = 'SUCCESS' " +
	        ")";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, theaterId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                movieIds.add(rs.getInt("movie_id"));
	            }
	        }
	    } 
	    catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return movieIds;
	}

	public boolean createManagerShowtime(
	        int userId,
	        int theaterId,
	        Showtimes showtime,
	        int price) throws SQLException {
	    if (showtime == null || price <= 0) {
	        return false;
	    }
	    Timestamp startAt = showtime.getStart_at();
	    Timestamp endAt = showtime.getEnd_at();
	    if (startAt == null || endAt == null
            || !endAt.after(startAt)
            || !startAt.after(new Timestamp(System.currentTimeMillis()))) {
	        return false;
	    }
	    String authorizationSql =
	        "SELECT 1 FROM Managers " +
	        "WHERE user_id = ? AND theater_id = ?";
	    String roomSql =
	        "SELECT 1 FROM Rooms " +
	        "WHERE room_id = ? AND theater_id = ? " +
	        "AND isActive = 1 AND deleted_at IS NULL";
	    String movieSql =
    		"SELECT 1 FROM Movies " +
		    "WHERE movie_id = ? " +
		    "AND isActive = 1 AND deleted_at IS NULL " +
		    "AND CAST(available_from AS date) <= CAST(? AS date) " +
		    "AND (available_until IS NULL " +
		    "     OR CAST(available_until AS date) >= CAST(? AS date))";
	    String conflictSql =
	        "SELECT 1 FROM Showtimes " +
	        "WHERE room_id = ? " +
	        "AND deleted_at IS NULL " +
	        "AND UPPER(status) <> 'CANCELED' " +
	        "AND start_at < ? AND end_at > ?";
	    String insertShowtimeSql =
	        "INSERT INTO Showtimes " +
	        "(room_id, movie_id, start_at, end_at, status) " +
	        "VALUES (?, ?, ?, ?, 'SCHEDULED')";
	    String insertPriceSql =
	        "INSERT INTO Showtime_ticket_types " +
	        "(showtime_id, ticket_type_id, price) " +
	        "VALUES (?, 1, ?)";
	    try (Connection conn = DBConnection.getConnection()) {
	        conn.setAutoCommit(false);
	        try {
	            // Verify that this manager is assigned to the theater.
	            try (PreparedStatement stmt = conn.prepareStatement(authorizationSql)) {
	                stmt.setInt(1, userId);
	                stmt.setInt(2, theaterId);                
	                try (ResultSet rs = stmt.executeQuery()) {
	                    if (!rs.next()) {
	                        conn.rollback();
	                        return false;
	                    }
	                }
	            }

	            // Verify that the selected room belongs to this theater.
	            try (PreparedStatement stmt = conn.prepareStatement(roomSql)) {
	                stmt.setInt(1, showtime.getRoom_id());
	                stmt.setInt(2, theaterId);	              
	                try (ResultSet rs = stmt.executeQuery()) {
	                    if (!rs.next()) {
	                        conn.rollback();
	                        return false;
	                    }
	                }
	            }

	            // Verify that the movie is active and available on the scheduled showtime date.
	            try (PreparedStatement stmt = conn.prepareStatement(movieSql)) {
	                stmt.setInt(1, showtime.getMovie_id());
	                stmt.setTimestamp(2, startAt);
	                stmt.setTimestamp(3, startAt);
	                try (ResultSet rs = stmt.executeQuery()) {
	                    if (!rs.next()) {
	                        conn.rollback();
	                        return false;
	                    }
	                }
	            }

	            // Reject overlapping showtimes in the same room.
	            try (PreparedStatement stmt = conn.prepareStatement(conflictSql)) {
	                stmt.setInt(1, showtime.getRoom_id());
	                stmt.setTimestamp(2, endAt);
	                stmt.setTimestamp(3, startAt);
	                try (ResultSet rs = stmt.executeQuery()) {
	                    if (rs.next()) {
	                        conn.rollback();
	                        return false;
	                    }
	                }
	            }

	            // Insert the showtime and retrieve its generated ID.
	            int showtimeId;
	            try (PreparedStatement stmt = conn.prepareStatement(
	                    insertShowtimeSql, Statement.RETURN_GENERATED_KEYS)) {
	                stmt.setInt(1, showtime.getRoom_id());
	                stmt.setInt(2, showtime.getMovie_id());
	                stmt.setTimestamp(3, startAt);
	                stmt.setTimestamp(4, endAt);
	                if (stmt.executeUpdate() != 1) {
	                    conn.rollback();
	                    return false;
	                }
	                try (ResultSet keys = stmt.getGeneratedKeys()) {
	                    if (!keys.next()) {
	                        conn.rollback();
	                        return false;
	                    }
	                    showtimeId = keys.getInt(1);
	                }
	            }

	            // Insert the single NORMAL ticket price.
	            try (PreparedStatement stmt = conn.prepareStatement(insertPriceSql)) {
	                stmt.setInt(1, showtimeId);
	                stmt.setInt(2, price);
	                if (stmt.executeUpdate() != 1) {
	                    conn.rollback();
	                    return false;
	                }
	            }
	            conn.commit();
	            return true;
	        } 
	        catch (SQLException | RuntimeException e) {
	            conn.rollback();
	            throw e;
	        } 
	        finally {
	            conn.setAutoCommit(true);
	        }
	    }
	}

	public List<Map<String, Object>> getDashboardShowtimesByTheaterAndDate(int theaterId, LocalDate date) {
        List<Map<String, Object>> showtimes = new ArrayList<>();
        String sql =
            "SELECT TOP 5 " +
            "s.showtime_id AS showtimeId, " +
            "m.movie_name AS movieName, " +
            "r.room_name AS roomName, " +
            "s.start_at AS startAt, " +
            "s.end_at AS endAt, " +
            "s.status AS status " +
            "FROM Showtimes s " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Movies m ON m.movie_id = s.movie_id " +
            "WHERE r.theater_id = ? " +
            "AND r.isActive = 1 " +
            "AND r.deleted_at IS NULL " +
            "AND s.deleted_at IS NULL " +
            "AND s.start_at >= GETDATE() " +
            "AND s.start_at < ? " +
            "AND UPPER(s.status) = 'SCHEDULED' " +
            "ORDER BY s.start_at ASC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            stmt.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("showtimeId", rs.getInt("showtimeId"));
                    row.put("movieName", rs.getString("movieName"));
                    row.put("roomName", rs.getString("roomName"));
                    row.put("startAt", rs.getTimestamp("startAt"));
                    row.put("endAt", rs.getTimestamp("endAt"));
                    row.put("status", rs.getString("status"));
                    showtimes.add(row);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }

    public int countDashboardShowtimesByTheaterAndDate(int theaterId, LocalDate date) {
        String sql =
            "SELECT COUNT(*) " +
            "FROM Showtimes s " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "WHERE r.theater_id = ? " +
            "AND r.isActive = 1 " +
            "AND r.deleted_at IS NULL " +
            "AND s.deleted_at IS NULL " +
            "AND s.start_at >= GETDATE() " +
            "AND s.start_at < ? " +
            "AND UPPER(s.status) = 'SCHEDULED'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            stmt.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
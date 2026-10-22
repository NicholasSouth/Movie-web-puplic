package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.movieweb.model.Booking_showtimes;
import com.movieweb.util.DBConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Booking_showtimesDAO 
{
    public Booking_showtimes getBookingById(int booking_id) 
    {
        String sql = "SELECT * FROM Booking_showtimes WHERE booking_id = ?";        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, booking_id);           
            ResultSet rs = stmt.executeQuery();        
            if (rs.next()) 
            {
                Booking_showtimes booking = new Booking_showtimes();
                booking.setBooking_id(rs.getInt("booking_id"));
                booking.setUser_id(rs.getInt("user_id"));
                booking.setShowtime_id(rs.getInt("showtime_id"));
                booking.setPromotion_id(rs.getInt("promotion_id"));
                booking.setBook_at(rs.getString("book_at"));
                booking.setStatus(rs.getString("status"));
                booking.setPrice(rs.getInt("price"));
                booking.setDelete_at(rs.getString("delete_at"));               
                return booking;
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }    
        return null;
    }
    
    //for booking history
    public List<Map<String, Object>> getBookingHistoryByUserId(int user_id) {
        List<Map<String, Object>> bookingHistory = new ArrayList<>();
        String sql =
            "SELECT " +
            "    b.booking_id, b.book_at, b.status AS booking_status, b.price, " +
            "    m.movie_name, m.poster_path, " +
            "    s.start_at, s.end_at, " +
            "    t.theater_name, r.room_name, " +
            "    COALESCE(p.status, 'NOT PAID') AS payment_status, " +
            "    p.method AS payment_method, p.amount AS payment_amount, " +
            "    STUFF(( " +
            "        SELECT ', ' + " +
            "            CAST(se.seat_row AS VARCHAR(10)) + " +
            "            CAST(se.seat_col AS VARCHAR(10)) + " +
            "            ' (' + tt.ticket_type_name + ')' " +
            "        FROM Booking_seats bs " +
            "        INNER JOIN Seats se ON bs.seat_id = se.seat_id " +
            "        INNER JOIN Ticket_types tt " +
            "            ON bs.ticket_type_id = tt.ticket_type_id " +
            "        WHERE bs.booking_id = b.booking_id " +
            "        FOR XML PATH(''), TYPE " +
            "    ).value('.', 'NVARCHAR(MAX)'), 1, 2, '') AS seat_summary " +
            "FROM Booking_showtimes b " +
            "INNER JOIN Showtimes s ON b.showtime_id = s.showtime_id " +
            "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
            "INNER JOIN Rooms r ON s.room_id = r.room_id " +
            "INNER JOIN Theaters t ON r.theater_id = t.theater_id " +
            "LEFT JOIN Payments p ON b.booking_id = p.booking_id " +
            "WHERE b.user_id = ? " +
            "AND b.delete_at IS NULL " +
            "ORDER BY b.book_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("booking_id", rs.getInt("booking_id"));
                    booking.put("book_at", rs.getTimestamp("book_at"));
                    booking.put("booking_status", rs.getString("booking_status"));
                    booking.put("price", rs.getInt("price"));
                    booking.put("movie_name", rs.getString("movie_name"));
                    booking.put("poster_path", rs.getString("poster_path"));
                    booking.put("start_at", rs.getTimestamp("start_at"));
                    booking.put("end_at", rs.getTimestamp("end_at"));
                    booking.put("theater_name", rs.getString("theater_name"));
                    booking.put("room_name", rs.getString("room_name"));
                    booking.put("seat_summary", rs.getString("seat_summary"));
                    booking.put("payment_status", rs.getString("payment_status"));
                    booking.put("payment_method", rs.getString("payment_method"));
                    booking.put("payment_amount", rs.getObject("payment_amount"));
                    bookingHistory.add(booking);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        return bookingHistory;
    }
    
    public int createPendingBooking(
            Connection conn,
            int user_id,
            int showtime_id,
            int promotion_id,
            int price) throws SQLException {
        String sql =
                "INSERT INTO Booking_showtimes " +
                "(user_id, showtime_id, promotion_id, book_at, status, price, delete_at) " +
                "VALUES (?, ?, ?, GETDATE(), 'PENDING', ?, NULL)";
        try (PreparedStatement stmt = conn.prepareStatement(
                sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, showtime_id);
            if (promotion_id > 0) {
                stmt.setInt(3, promotion_id);
            } 
            else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.setInt(4, price);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException("Failed to create booking.");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new SQLException("Could not retrieve generated booking_id.");
        }
    }

    //to put the booking infor into payment.jsp
    public Map<String, Object> getCheckoutBooking(int userId, int bookingId) 
    		throws SQLException {
        String sql =
                "SELECT b.booking_id, b.user_id, b.showtime_id, " +
                "       b.book_at, b.status AS booking_status, b.price, " +
                "       b.promotion_id, " +
                "       b.delete_at, " +
                "       CASE WHEN b.status = 'PENDING' " +
                "                 AND (b.delete_at IS NOT NULL " +
                "                      OR DATEADD(MINUTE, 15, b.book_at) <= GETDATE()) " +
                "            THEN 1 ELSE 0 END AS is_expired, " +
                "       m.movie_name, s.start_at, s.end_at, " +
                "       s.status AS showtime_status, " +
                "       r.room_name, t.theater_name, " +
                "       p.status AS payment_status, p.transaction_code " +
                "FROM Booking_showtimes b " +
                "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
                "INNER JOIN Movies m ON m.movie_id = s.movie_id " +
                "INNER JOIN Rooms r ON r.room_id = s.room_id " +
                "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
                "LEFT JOIN Payments p ON p.booking_id = b.booking_id " +
                "WHERE b.booking_id = ? AND b.user_id = ?";
        Map<String, Object> booking = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    booking = new HashMap<>();
                    booking.put("bookingId", rs.getInt("booking_id"));
                    booking.put("bookingStatus", rs.getString("booking_status"));
                    booking.put("bookingPrice", rs.getInt("price"));
                    booking.put("promotionId", rs.getInt("promotion_id"));
                    booking.put("isExpired", rs.getInt("is_expired") == 1);
                    booking.put("movieName", rs.getString("movie_name"));
                    booking.put("showtimeStart", rs.getTimestamp("start_at"));
                    booking.put("showtimeEnd", rs.getTimestamp("end_at"));
                    booking.put("showtimeStatus", rs.getString("showtime_status"));
                    booking.put("roomName", rs.getString("room_name"));
                    booking.put("theaterName", rs.getString("theater_name"));
                    booking.put("paymentStatus", rs.getString("payment_status"));
                    booking.put("transactionCode", rs.getString("transaction_code"));
                }
            }
        }
        return booking;
    }

    //get booking history for pos
    public List<Map<String, Object>> getBookingHistoryByTheaterId(
            int theater_id) throws SQLException {
        List<Map<String, Object>> bookingHistory = new ArrayList<>();
        String sql =
            "SELECT " +
            "    b.booking_id, " +
            "    b.showtime_id, " +
            "    b.book_at, " +
            "    b.status AS booking_status, " +
            "    b.price, " +
            "    m.movie_name, " +
            "    s.start_at, " +
            "    s.end_at, " +
            "    r.room_name, " +
            "    s.status AS showtime_status, " +
            "    t.theater_name, " +
            "    p.status AS payment_status, " +
            "    p.method AS payment_method, " +
            "    p.amount AS payment_amount, " +
            "    p.transaction_code " +
            "FROM Booking_showtimes b " +
            "INNER JOIN Showtimes s " +
            "    ON s.showtime_id = b.showtime_id " +
            "INNER JOIN Movies m " +
            "    ON m.movie_id = s.movie_id " +
            "INNER JOIN Rooms r " +
            "    ON r.room_id = s.room_id " +
            "INNER JOIN Theaters t " +
            "    ON t.theater_id = r.theater_id " +
            "LEFT JOIN Payments p " +
            "    ON p.booking_id = b.booking_id " +
            "WHERE b.delete_at IS NULL " +
            "AND t.theater_id = ? " +
            "ORDER BY b.book_at DESC, b.booking_id DESC";
        Booking_seatsDAO bookingSeatsDAO = new Booking_seatsDAO();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theater_id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> booking = new HashMap<>();
                    int bookingId = rs.getInt("booking_id");
                    booking.put("bookingId", bookingId);
                    booking.put("showtimeId", rs.getInt("showtime_id"));
                    booking.put("bookAt", rs.getTimestamp("book_at"));
                    booking.put("bookingStatus", rs.getString("booking_status"));
                    booking.put("price", rs.getInt("price"));
                    booking.put("movieName", rs.getString("movie_name"));
                    booking.put("startAt", rs.getTimestamp("start_at"));
                    booking.put("endAt", rs.getTimestamp("end_at"));
                    booking.put("showtimeStatus", rs.getString("showtime_status"));
                    booking.put("roomName", rs.getString("room_name"));
                    booking.put("theaterName", rs.getString("theater_name"));
                    booking.put("paymentStatus", rs.getString("payment_status"));
                    booking.put("paymentMethod", rs.getString("payment_method"));
                    booking.put("paymentAmount", rs.getObject("payment_amount"));
                    booking.put("transactionCode", rs.getString("transaction_code"));
                    booking.put("seats", bookingSeatsDAO.getBookedSeats(bookingId));
                    bookingHistory.add(booking);
                }
            }
        }
        return bookingHistory;
    }

    //REFUNDED -> CANCELLED, this method search CONFIRMED showtimes to set REFUNDED
    public boolean requestRefund(int booking_id, int theater_id)
        throws SQLException {
	    String sql =
	        "UPDATE b " +
	        "SET b.status = 'REFUNDED' " +
	        "FROM Booking_showtimes b " +
	        "WHERE b.booking_id = ? " +
	        "AND b.status = 'CONFIRMED' " +
	        "AND b.delete_at IS NULL " +
	        "AND EXISTS ( " +
	        "    SELECT 1 " +
	        "    FROM Showtimes s " +
	        "    INNER JOIN Rooms r ON s.room_id = r.room_id " +
	        "    WHERE s.showtime_id = b.showtime_id " +
	        "    AND r.theater_id = ? " +
	        ") " +
	        "AND EXISTS ( " +
	        "    SELECT 1 " +
	        "    FROM Payments p " +
	        "    WHERE p.booking_id = b.booking_id " +
	        "    AND p.status = 'SUCCESS' " +
	        ")";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, booking_id);
	        stmt.setInt(2, theater_id);
	        return stmt.executeUpdate() == 1;
	    }
    }

    //Get infor for refunding email to admin
	public Map<String, Object> getRefundEmailDetails(int booking_id, int theater_id) throws SQLException {
	    String sql =
	        "SELECT m.movie_name, t.theater_name, r.room_name " +
	        "FROM Booking_showtimes b " +
	        "INNER JOIN Showtimes s ON b.showtime_id = s.showtime_id " +
	        "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
	        "INNER JOIN Rooms r ON s.room_id = r.room_id " +
	        "INNER JOIN Theaters t ON r.theater_id = t.theater_id " +
	        "WHERE b.booking_id = ? " +
	        "AND t.theater_id = ? " +
	        "AND b.status = 'REFUNDED' " +
	        "AND b.delete_at IS NULL";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, booking_id);
	        stmt.setInt(2, theater_id);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                Map<String, Object> details = new HashMap<>();
	                details.put("movieName", rs.getString("movie_name"));
	                details.put("theaterName", rs.getString("theater_name"));
	                details.put("roomName", rs.getString("room_name"));
	                return details;
	            }
	        }
	    }
	    return null;
	}

	//Get refund history for the selected theater
	public List<Map<String, Object>> getRefundHistoryByTheaterId(int theater_id)
	        throws SQLException {
	    List<Map<String, Object>> refundHistory = new ArrayList<>();
	    String sql =
	        "SELECT b.booking_id, b.book_at, b.status AS booking_status, b.price, " +
	        "m.movie_name, s.start_at, s.end_at, " +
	        "r.room_name, t.theater_name, " +
	        "p.status AS payment_status, p.method AS payment_method, " +
	        "p.amount AS payment_amount, p.transaction_code " +
	        "FROM Booking_showtimes b " +
	        "INNER JOIN Showtimes s ON b.showtime_id = s.showtime_id " +
	        "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
	        "INNER JOIN Rooms r ON s.room_id = r.room_id " +
	        "INNER JOIN Theaters t ON r.theater_id = t.theater_id " +
	        "LEFT JOIN Payments p ON b.booking_id = p.booking_id " +
	        "WHERE b.delete_at IS NULL " +
	        "AND t.theater_id = ? " +
	        "AND b.status IN ('REFUNDED', 'CANCELLED') " +
	        "ORDER BY b.book_at DESC, b.booking_id DESC";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, theater_id);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> booking = new HashMap<>();
	                booking.put("bookingId", rs.getInt("booking_id"));
	                booking.put("bookAt", rs.getTimestamp("book_at"));
	                booking.put("bookingStatus", rs.getString("booking_status"));
	                booking.put("price", rs.getInt("price"));
	                booking.put("movieName", rs.getString("movie_name"));
	                booking.put("startAt", rs.getTimestamp("start_at"));
	                booking.put("endAt", rs.getTimestamp("end_at"));
	                booking.put("roomName", rs.getString("room_name"));
	                booking.put("theaterName", rs.getString("theater_name"));
	                booking.put("paymentStatus", rs.getString("payment_status"));
	                booking.put("paymentMethod", rs.getString("payment_method"));
	                booking.put("paymentAmount", rs.getObject("payment_amount"));
	                booking.put("transactionCode", rs.getString("transaction_code"));
	                refundHistory.add(booking);
	            }
	        }
	    }
	    return refundHistory;
	}


    // Get one page of POS booking history, optionally filtered by transaction code
    public List<Map<String, Object>> getBookingHistoryByTheaterId(
            int theater_id,
            String transactionCode,
            int offset,
            int pageSize) throws SQLException {
        List<Map<String, Object>> bookingHistory = new ArrayList<>();
        String search = transactionCode == null ? "" : transactionCode.trim();
        String sql =
            "SELECT " +
            "    b.booking_id, b.showtime_id, b.book_at, " +
            "    b.status AS booking_status, b.price, " +
            "    m.movie_name, s.start_at, s.end_at, " +
            "    r.room_name, s.status AS showtime_status, " +
            "    t.theater_name, p.status AS payment_status, " +
            "    p.method AS payment_method, p.amount AS payment_amount, " +
            "    p.transaction_code " +
            "FROM Booking_showtimes b " +
            "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "INNER JOIN Movies m ON m.movie_id = s.movie_id " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "LEFT JOIN Payments p ON p.booking_id = b.booking_id " +
            "WHERE b.delete_at IS NULL " +
            "AND t.theater_id = ? ";
        if (!search.isEmpty()) {
            sql += "AND p.transaction_code LIKE ? ";
        }
        sql +=
            "ORDER BY b.book_at DESC, b.booking_id DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        Booking_seatsDAO bookingSeatsDAO = new Booking_seatsDAO();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setInt(paramIndex++, theater_id);
            if (!search.isEmpty()) {
                stmt.setString(paramIndex++, "%" + search + "%");
            }
            stmt.setInt(paramIndex++, offset);
            stmt.setInt(paramIndex, pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> booking = new HashMap<>();
                    int bookingId = rs.getInt("booking_id");
                    booking.put("bookingId", bookingId);
                    booking.put("showtimeId", rs.getInt("showtime_id"));
                    booking.put("bookAt", rs.getTimestamp("book_at"));
                    booking.put("bookingStatus", rs.getString("booking_status"));
                    booking.put("price", rs.getInt("price"));
                    booking.put("movieName", rs.getString("movie_name"));
                    booking.put("startAt", rs.getTimestamp("start_at"));
                    booking.put("endAt", rs.getTimestamp("end_at"));
                    booking.put("showtimeStatus", rs.getString("showtime_status"));
                    booking.put("roomName", rs.getString("room_name"));
                    booking.put("theaterName", rs.getString("theater_name"));
                    booking.put("paymentStatus", rs.getString("payment_status"));
                    booking.put("paymentMethod", rs.getString("payment_method"));
                    booking.put("paymentAmount", rs.getObject("payment_amount"));
                    booking.put("transactionCode", rs.getString("transaction_code"));

                    // Only load seats for bookings on this page
                    booking.put("seats", bookingSeatsDAO.getBookedSeats(bookingId));
                    bookingHistory.add(booking);
                }
            }
        }
        return bookingHistory;
    }

    // Count all matching POS bookings for pagination
    public int countBookingHistoryByTheaterId(
            int theater_id,
            String transactionCode) throws SQLException {
        String search = transactionCode == null ? "" : transactionCode.trim();
        String sql =
            "SELECT COUNT(*) " +
            "FROM Booking_showtimes b " +
            "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
            "LEFT JOIN Payments p ON p.booking_id = b.booking_id " +
            "WHERE b.delete_at IS NULL " +
            "AND t.theater_id = ? ";
        if (!search.isEmpty()) {
            sql += "AND p.transaction_code LIKE ?";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theater_id);
            if (!search.isEmpty()) {
                stmt.setString(2, "%" + search + "%");
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    //For refund page to show 5 refund at a time, incase lagging, similar implementation as paging in pos.jsp
	public int countRefundHistoryByTheaterId(
	        int theater_id,
	        String status,
	        String transactionCode) throws SQLException {
	    String sql =
	        "SELECT COUNT(*) " +
	        "FROM Booking_showtimes b " +
	        "INNER JOIN Showtimes s ON b.showtime_id = s.showtime_id " +
	        "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
	        "INNER JOIN Rooms r ON s.room_id = r.room_id " +
	        "INNER JOIN Theaters t ON r.theater_id = t.theater_id " +
	        "LEFT JOIN Payments p ON b.booking_id = p.booking_id " +
	        "WHERE b.delete_at IS NULL " +
	        "AND t.theater_id = ? " +
	        "AND b.status IN ('REFUNDED', 'CANCELLED') " +
	        "AND (? = 'ALL' OR b.status = ?) " +
	        "AND (? = '' OR p.transaction_code LIKE ?)";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, theater_id);
	        stmt.setString(2, status);
	        stmt.setString(3, status);
	        stmt.setString(4, transactionCode);
	        stmt.setString(5, "%" + transactionCode + "%");
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1);
	            }
	        }
	    }
	    return 0;
	}

	public List<Map<String, Object>> getRefundHistoryByTheaterId(
	        int theater_id,
	        String status,
	        String transactionCode,
	        int offset,
	        int pageSize) throws SQLException {
	    List<Map<String, Object>> refundHistory = new ArrayList<>();
	    String sql =
	        "SELECT b.booking_id, b.book_at, " +
	        "b.status AS booking_status, b.price, " +
	        "m.movie_name, s.start_at, s.end_at, " +
	        "r.room_name, t.theater_name, " +
	        "p.status AS payment_status, " +
	        "p.method AS payment_method, " +
	        "p.amount AS payment_amount, " +
	        "p.transaction_code " +
	        "FROM Booking_showtimes b " +
	        "INNER JOIN Showtimes s ON b.showtime_id = s.showtime_id " +
	        "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
	        "INNER JOIN Rooms r ON s.room_id = r.room_id " +
	        "INNER JOIN Theaters t ON r.theater_id = t.theater_id " +
	        "LEFT JOIN Payments p ON b.booking_id = p.booking_id " +
	        "WHERE b.delete_at IS NULL " +
	        "AND t.theater_id = ? " +
	        "AND b.status IN ('REFUNDED', 'CANCELLED') " +
	        "AND (? = 'ALL' OR b.status = ?) " +
	        "AND (? = '' OR p.transaction_code LIKE ?) " +
	        "ORDER BY b.book_at DESC, b.booking_id DESC " +
	        "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, theater_id);
	        stmt.setString(2, status);
	        stmt.setString(3, status);
	        stmt.setString(4, transactionCode);
	        stmt.setString(5, "%" + transactionCode + "%");
	        stmt.setInt(6, offset);
	        stmt.setInt(7, pageSize);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Map<String, Object> booking = new HashMap<>();
	                booking.put("bookingId", rs.getInt("booking_id"));
	                booking.put("bookAt", rs.getTimestamp("book_at"));
	                booking.put("bookingStatus", rs.getString("booking_status"));
	                booking.put("price", rs.getInt("price"));
	                booking.put("movieName", rs.getString("movie_name"));
	                booking.put("startAt", rs.getTimestamp("start_at"));
	                booking.put("endAt", rs.getTimestamp("end_at"));
	                booking.put("roomName", rs.getString("room_name"));
	                booking.put("theaterName", rs.getString("theater_name"));
	                booking.put("paymentStatus", rs.getString("payment_status"));
	                booking.put("paymentMethod", rs.getString("payment_method"));
	                booking.put("paymentAmount", rs.getObject("payment_amount"));
	                booking.put("transactionCode", rs.getString("transaction_code"));
	                refundHistory.add(booking);
	            }
	        }
	    }
	    return refundHistory;
	}

    // Dashboard: count confirmed bookings made today for the selected theater.
    public int getTodayConfirmedBookingCountByTheaterId(int theaterId) {
        String sql =
            "SELECT COUNT(*) " +
            "FROM Booking_showtimes b " +
            "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "WHERE r.theater_id = ? " +
            "AND b.delete_at IS NULL " +
            "AND UPPER(b.status) = 'CONFIRMED' " +
            "AND CAST(b.book_at AS date) = CAST(GETDATE() AS date)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
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

    // Dashboard: five latest bookings, without loading each booking's seats.
    // Manager-created bookings are classified as POS using Users.role.
    public List<Map<String, Object>> getRecentBookingsByTheaterId(int theaterId) {
        List<Map<String, Object>> bookings = new ArrayList<>();
        String sql =
            "SELECT TOP 5 " +
            "b.booking_id AS bookingId, " +
            "b.book_at AS bookAt, " +
            "b.status AS bookingStatus, " +
            "m.movie_name AS movieName, " +
            "u.full_name AS customerName, " +
            "CASE WHEN UPPER(u.role) = 'MANAGER' " +
            "     THEN 'POS' ELSE 'Customer' END AS bookingSource, " +
            "p.transaction_code AS transactionCode, " +
            "p.status AS paymentStatus " +
            "FROM Booking_showtimes b " +
            "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "INNER JOIN Movies m ON m.movie_id = s.movie_id " +
            "LEFT JOIN Users u ON u.user_id = b.user_id " +
            "LEFT JOIN Payments p ON p.booking_id = b.booking_id " +
            "WHERE r.theater_id = ? " +
            "AND b.delete_at IS NULL " +
            "ORDER BY b.book_at DESC, b.booking_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("bookingId", rs.getInt("bookingId"));
                    row.put("bookAt", rs.getTimestamp("bookAt"));
                    row.put("bookingStatus", rs.getString("bookingStatus"));
                    row.put("movieName", rs.getString("movieName"));
                    row.put("customerName", rs.getString("customerName"));
                    row.put("bookingSource", rs.getString("bookingSource"));
                    row.put("transactionCode", rs.getString("transactionCode"));
                    row.put("paymentStatus", rs.getString("paymentStatus"));
                    bookings.add(row);
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }
}
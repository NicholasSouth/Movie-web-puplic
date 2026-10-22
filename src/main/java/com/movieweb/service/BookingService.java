
package com.movieweb.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.movieweb.DAO.Booking_seatsDAO;
import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.model.Booking_seats;
import com.movieweb.model.Promotions;
import com.movieweb.model.Seats;
import com.movieweb.model.Showtimes;
import com.movieweb.util.DBConnection;

public class BookingService {
	//User mode only choose default, theater manager and adin can choose fixed discount
    private static final int NORMAL_TICKET_TYPE_ID = 1;
    private static final int HOLD_MINUTES = 15;
    private final Booking_showtimesDAO bookingShowtimesDAO = new Booking_showtimesDAO();
    private final Booking_seatsDAO bookingSeatsDAO = new Booking_seatsDAO();
    private final PricingService pricingService = new PricingService();

    public int createBooking(
            int user_id,
            int showtime_id,
            int promotion_id,
            Promotions promotion,
            List<Integer> selectedSeatIds) throws SQLException {
        if (selectedSeatIds == null || selectedSeatIds.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one seat.");
        }
        if ((promotion_id > 0) != (promotion != null)) {
            throw new IllegalArgumentException("Invalid promotion selection.");
        }

        // Sort IDs so concurrent requests lock seats in the same order.
        List<Integer> seatIds = new ArrayList<>(selectedSeatIds);
        Collections.sort(seatIds);
        Set<Integer> uniqueSeatIds = new HashSet<>(seatIds);
        if (uniqueSeatIds.size() != seatIds.size()
                || uniqueSeatIds.contains(null)
                || uniqueSeatIds.contains(0)) {
            throw new IllegalArgumentException("Invalid or duplicate seat selection.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Showtimes showtime = lockAndGetBookableShowtime(conn, showtime_id);
                if (showtime == null) {
                    throw new IllegalArgumentException("This showtime is unavailable or has already started.");
                }

                // Expire pending bookings before checking seat availability.
                cancelExpiredBookings(conn, showtime_id);
                Map<Integer, Seats> seats = lockAndGetAvailableSeats(
                                conn, showtime.getRoom_id(), seatIds,
                                showtime_id);
                if (seats.size() != seatIds.size()) {
                    throw new IllegalArgumentException(
                            "One or more selected seats are unavailable. "
                            + "Please select your seats again.");
                }
                List<Booking_seats> bookingSeats = new ArrayList<>();
                long totalPrice = 0;
                for (int seatId : seatIds) {
                    Seats seat = seats.get(seatId);
                    int finalPrice = pricingService.calculatePrice(
                            showtime,
                            seat,
                            NORMAL_TICKET_TYPE_ID,
                            promotion);
                    if (finalPrice < 0) {
                        throw new SQLException("Could not calculate the ticket price.");
                    }
                    totalPrice += finalPrice;
                    if (totalPrice > Integer.MAX_VALUE) {
                        throw new SQLException("The booking total exceeds the supported amount.");
                    }
                    Booking_seats bookingSeat = new Booking_seats();
                    bookingSeat.setSeat_id(seatId);
                    bookingSeat.setTicket_type_id(NORMAL_TICKET_TYPE_ID);
                    bookingSeat.setFinal_price(finalPrice);
                    bookingSeats.add(bookingSeat);
                }
                int booking_id = bookingShowtimesDAO.createPendingBooking(
                                conn,
                                user_id,
                                showtime_id,
                                promotion_id,
                                (int) totalPrice);
                for (Booking_seats bookingSeat : bookingSeats) {
                    bookingSeat.setBooking_id(booking_id);
                    if (!bookingSeatsDAO.addBookingSeat(conn, bookingSeat)) {
                        throw new SQLException("Failed to save a selected seat.");
                    }
                }
                conn.commit();
                return booking_id;
            } 
            catch (Exception e) {
                try {
                    conn.rollback();
                } 
                catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }
                throw new SQLException("Failed to create booking.", e);
            } 
            finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Showtimes lockAndGetBookableShowtime(
            Connection conn, int showtime_id) throws SQLException {
        String sql =
                "SELECT showtime_id, room_id, start_at, end_at, status " +
                "FROM Showtimes WITH (UPDLOCK, HOLDLOCK) " +
                "WHERE showtime_id = ? " +
                "AND status = 'SCHEDULED' " +
                "AND start_at > GETDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showtime_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Showtimes showtime = new Showtimes();
                showtime.setShowtime_id(rs.getInt("showtime_id"));
                showtime.setRoom_id(rs.getInt("room_id"));
                showtime.setStart_at(rs.getTimestamp("start_at"));
                showtime.setEnd_at(rs.getTimestamp("end_at"));
                showtime.setStatus(rs.getString("status"));
                return showtime;
            }
        }
    }

    private void cancelExpiredBookings(
            Connection conn, int showtime_id) throws SQLException {
        String sql =
                "UPDATE Booking_showtimes " +
                "SET status = 'CANCELLED' " +
                "WHERE showtime_id = ? " +
                "AND status = 'PENDING' " +
                "AND delete_at IS NULL " +
                "AND DATEADD(MINUTE, ?, book_at) <= GETDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showtime_id);
            stmt.setInt(2, HOLD_MINUTES);
            stmt.executeUpdate();
        }
    }
    
    private Map<Integer, Seats> lockAndGetAvailableSeats(
            Connection conn,
            int room_id,
            List<Integer> seatIds,
            int showtime_id) throws SQLException {
        Map<Integer, Seats> availableSeats = new HashMap<>();
        String seatSql =
                "SELECT s.seat_id, s.room_id, s.seat_row, s.seat_col, " +
                "s.seat_type_id, s.isActive, " +
                "t.seat_type_name, t.price_modify AS seat_price_modify " +
                "FROM Seats s WITH (UPDLOCK, HOLDLOCK) " +
                "INNER JOIN Seat_types t " +
                "ON t.seat_type_id = s.seat_type_id " +
                "WHERE s.seat_id = ? " +
                "AND s.room_id = ? " +
                "AND s.isActive = 1";
        String bookedSql =
                "SELECT COUNT(*) " +
                "FROM Booking_seats bs " +
                "INNER JOIN Booking_showtimes b " +
                "ON b.booking_id = bs.booking_id " +
                "WHERE bs.seat_id = ? " +
                "AND b.showtime_id = ? " +
                "AND b.delete_at IS NULL " +
                "AND b.status IN ('PENDING', 'CONFIRMED') " +
                "AND (b.status = 'CONFIRMED' " +
                "OR DATEADD(MINUTE, ?, b.book_at) > GETDATE())";
        try (PreparedStatement seatStmt = conn.prepareStatement(seatSql);
             PreparedStatement bookedStmt = conn.prepareStatement(bookedSql)) {
            for (int seatId : seatIds) {
                seatStmt.setInt(1, seatId);
                seatStmt.setInt(2, room_id);
                try (ResultSet rs = seatStmt.executeQuery()) {
                    if (!rs.next()) {
                        continue;
                    }
                    Seats seat = new Seats();
                    seat.setSeat_id(rs.getInt("seat_id"));
                    seat.setRoom_id(rs.getInt("room_id"));
                    seat.setSeat_row(rs.getString("seat_row"));
                    seat.setSeat_col(rs.getInt("seat_col"));
                    seat.setSeat_type_id(rs.getInt("seat_type_id"));
                    seat.setSeat_type_name(rs.getString("seat_type_name"));
                    seat.setSeat_price_modify(rs.getString("seat_price_modify"));
                    seat.setActive(rs.getBoolean("isActive"));
                    bookedStmt.setInt(1, seatId);
                    bookedStmt.setInt(2, showtime_id);
                    bookedStmt.setInt(3, HOLD_MINUTES);
                    try (ResultSet bookedRs = bookedStmt.executeQuery()) {
                        if (bookedRs.next() && bookedRs.getInt(1) == 0) {
                            availableSeats.put(seatId, seat);
                        }
                    }
                }
            }
        }
        return availableSeats;
    }
}
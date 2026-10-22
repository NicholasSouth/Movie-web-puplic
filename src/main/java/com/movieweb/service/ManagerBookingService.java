package com.movieweb.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.movieweb.DAO.Booking_seatsDAO;
import com.movieweb.DAO.Booking_showtimesDAO;
import com.movieweb.DAO.PromotionsDAO;
import com.movieweb.DAO.SeatsDAO;
import com.movieweb.model.Booking_seats;
import com.movieweb.model.Promotions;
import com.movieweb.model.Seats;
import com.movieweb.model.Showtimes;
import com.movieweb.util.DBConnection;

public class ManagerBookingService {

    private static final int HOLD_MINUTES = 15;

    private final Booking_showtimesDAO bookingShowtimesDAO = new Booking_showtimesDAO();
    private final Booking_seatsDAO bookingSeatsDAO = new Booking_seatsDAO();
    private final SeatsDAO seatsDAO = new SeatsDAO();
    private final PromotionsDAO promotionsDAO = new PromotionsDAO();
    private final SeatSelectionService seatSelectionService = new SeatSelectionService();
    private final PricingService pricingService = new PricingService();

    public int createBooking(
            int manager_id,
            int showtime_id,
            int ticket_type_id,
            String promotion_code,
            List<Integer> selectedSeatIds)
            throws SQLException {
        if (selectedSeatIds == null || selectedSeatIds.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one seat.");
        }
        if (ticket_type_id <= 0) {
            throw new IllegalArgumentException("Invalid ticket type.");
        }
        Showtimes showtime = seatSelectionService.getBookableShowtime(showtime_id);
        if (showtime == null) {
            throw new IllegalArgumentException("This showtime is no longer available.");
        }
        if (!isManagerAssignedToShowtime(manager_id, showtime_id)) {
            throw new IllegalArgumentException("You are not assigned to this theater.");
        }
        Promotions promotion = null;
        if (promotion_code != null && !promotion_code.trim().isEmpty()) {
            promotion = promotionsDAO.getPromotionByCode(promotion_code.trim());
            if (promotion == null) {
                throw new IllegalArgumentException("Invalid promotion code.");
            }
            if (!promotion.isActive()) {
                throw new IllegalArgumentException("This promotion is no longer active.");
            }
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (promotion.getStart_at() != null && now.before(promotion.getStart_at())) {
                throw new IllegalArgumentException("This promotion is not available yet.");
            }
            if (promotion.getEnd_at() != null && now.after(promotion.getEnd_at())) {
                throw new IllegalArgumentException("This promotion has expired.");
            }
            if (promotion.getUsage_limit() > 0) {
                int usage = promotionsDAO.getPromotionUsageCount(promotion.getPromotion_id());
                if (usage >= promotion.getUsage_limit()) {
                    throw new IllegalArgumentException("This promotion has reached its usage limit.");
                }
            }
        }
        List<Seats> seats = seatsDAO.getSeatsWithStatusByShowtime(showtime_id);
        Map<Integer, Seats> seatMap = new HashMap<>();
        for (Seats seat : seats) {
            seatMap.put(seat.getSeat_id(), seat);
        }
        for (int i = 0; i < selectedSeatIds.size(); i++) {
            for (int j = i + 1; j < selectedSeatIds.size(); j++) {
                if (selectedSeatIds.get(i).equals(selectedSeatIds.get(j))) {
                    throw new IllegalArgumentException("A seat was selected more than once.");
                }
            }
        }
        Map<Integer, Integer> finalPrices = new HashMap<>();
        int totalPrice = 0;
        for (Integer seatId : selectedSeatIds) {
            if (seatId == null || seatId <= 0) {
                throw new IllegalArgumentException("Invalid seat.");
            }
            Seats seat = seatMap.get(seatId);
            if (seat == null) {
                throw new IllegalArgumentException("One of the selected seats is invalid.");
            }
            if (!seat.isActive()) {
                throw new IllegalArgumentException("One of the selected seats is unavailable.");
            }
            if (seat.isBooked()) {
                throw new IllegalArgumentException("One of the selected seats has already been booked.");
            }
            int finalPrice = pricingService.calculatePrice(
                    showtime,
                    seat,
                    ticket_type_id,
                    promotion);
            if (finalPrice < 0) {
                throw new IllegalArgumentException("Unable to calculate ticket price.");
            }
            finalPrices.put(seatId, finalPrice);
            totalPrice += finalPrice;
        }
        if (totalPrice <= 0) {
            throw new IllegalArgumentException("Invalid booking price.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                validateSeatsStillAvailable(conn, showtime_id, selectedSeatIds);
                int promotionId = promotion == null ? 0 : promotion.getPromotion_id();
                int bookingId = bookingShowtimesDAO.createPendingBooking(
                        conn,
                        manager_id,
                        showtime_id,
                        promotionId,
                        totalPrice);
                /*Insert every selected seat.*/
                for (Integer seatId : selectedSeatIds) {
                    Booking_seats bookingSeat = new Booking_seats();
                    bookingSeat.setBooking_id(bookingId);
                    bookingSeat.setSeat_id(seatId);
                    bookingSeat.setTicket_type_id(ticket_type_id);
                    bookingSeat.setFinal_price(finalPrices.get(seatId));
                    if (!bookingSeatsDAO.addBookingSeat(conn, bookingSeat)) {
                        throw new SQLException("Failed to create booking seat.");
                    }
                }
                conn.commit();
                return bookingId;
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
                throw new SQLException("Manager booking creation failed.", e);
            }
        }
    }

    private boolean isManagerAssignedToShowtime(int manager_id, int showtime_id) throws SQLException {
        String sql =
                "SELECT 1 " +
                "FROM Managers mg " +
                "INNER JOIN Theaters t " +
                "    ON t.theater_id = mg.theater_id " +
                "INNER JOIN Rooms r " +
                "    ON r.theater_id = t.theater_id " +
                "INNER JOIN Showtimes s " +
                "    ON s.room_id = r.room_id " +
                "WHERE mg.user_id = ? " +
                "AND s.showtime_id = ? " +
                "AND t.isActive = 1 " +
                "AND t.deleted_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, manager_id);
            stmt.setInt(2, showtime_id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void validateSeatsStillAvailable(Connection conn, int showtimeId, List<Integer> selectedSeatIds) 
            		throws SQLException {
        String sql =
            "SELECT s.seat_id " +
            "FROM Seats s WITH (UPDLOCK, HOLDLOCK) " +
            "INNER JOIN Showtimes st " +
            "    ON st.room_id = s.room_id " +
            "WHERE st.showtime_id = ? " +
            "AND st.deleted_at IS NULL " +
            "AND s.seat_id = ? " +
            "AND s.isActive = 1 " +
            "AND s.deleted_at IS NULL " +
            "AND NOT EXISTS (" +
            "    SELECT 1 " +
            "    FROM Booking_seats bs " +
            "    INNER JOIN Booking_showtimes b " +
            "        ON b.booking_id = bs.booking_id " +
            "    WHERE bs.seat_id = s.seat_id " +
            "    AND b.showtime_id = st.showtime_id " +
            "    AND b.delete_at IS NULL " +
            "    AND (" +
            "        b.status = 'CONFIRMED' " +
            "        OR (" +
            "            b.status = 'PENDING' " +
            "            AND DATEADD(MINUTE, 15, b.book_at) > GETDATE()" +
            "        )" +
            "    )" +
            ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Integer seatId : selectedSeatIds) {
                if (seatId == null || seatId <= 0) {
                    throw new SQLException("Invalid seat selection.");
                }
                stmt.setInt(1, showtimeId);
                stmt.setInt(2, seatId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                            "Seat " + seatId +
                            " is unavailable, inactive, deleted, " +
                            "or already booked. Please select your seats again."
                        );
                    }
                }
            }
        }
    }

}
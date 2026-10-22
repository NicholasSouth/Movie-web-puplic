package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.movieweb.model.AdminPaymentMethodStatistic;
import com.movieweb.model.AdminStatisticsSummary;
import com.movieweb.model.AdminTheaterStatistic;
import com.movieweb.model.ManagerDailyStatistic;
import com.movieweb.model.ManagerMovieStatistic;
import com.movieweb.util.DBConnection;

/* System-wide reports for ADMIN. theaterId == null means all theaters.
 * Same rules as ManagerStatisticsDAO: inclusive start date and exclusive next-day end boundary bound as
 * Timestamps; booking and ticket counts use Booking_showtimes.book_at, revenue uses Payments.paid_at;
 * Payments is never joined to Booking_seats before summing, so amounts are not duplicated;
 * soft-deleted/inactive theaters, rooms and movies are NOT excluded (history stays reportable);
 * every method propagates SQLException. Revenue is GROSS successful payments (refunds not deducted).
 */
public class AdminStatisticsDAO {

    private static final String BOOKING_JOIN =
        " FROM Booking_showtimes b " +
        " INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
        " INNER JOIN Rooms r ON r.room_id = s.room_id ";
    private static final String PAYMENT_JOIN =
        " FROM Payments p " +
        " INNER JOIN Booking_showtimes b ON b.booking_id = p.booking_id " +
        " INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
        " INNER JOIN Rooms r ON r.room_id = s.room_id ";
    private static final String SEAT_JOIN =
        " FROM Booking_seats bs " +
        " INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
        " INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
        " INNER JOIN Rooms r ON r.room_id = s.room_id ";

    private static Timestamp from(LocalDate d) {
        return Timestamp.valueOf(d.atStartOfDay());
    }

    private static String theaterClause(Integer theaterId) {
        return theaterId == null ? "" : " AND r.theater_id = ? ";
    }

    /* Binds start, exclusive end, then the optional theater id, starting at index i; returns the next index. */
    private static int bind(PreparedStatement ps, int i, Timestamp start, Timestamp end, Integer theaterId)
            throws SQLException {
        ps.setTimestamp(i++, start);
        ps.setTimestamp(i++, end);
        if (theaterId != null) {
            ps.setInt(i++, theaterId);
        }
        return i;
    }

    private long scalar(Connection conn, String sql, Timestamp start, Timestamp end, Integer theaterId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, 1, start, end, theaterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    public AdminStatisticsSummary getSummary(Integer theaterId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        Timestamp start = from(startDate);
        Timestamp end = from(endDate.plusDays(1));
        String t = theaterClause(theaterId);
        AdminStatisticsSummary s = new AdminStatisticsSummary();
        try (Connection conn = DBConnection.getConnection()) {
            s.setConfirmedBookings((int) scalar(conn,
                "SELECT COUNT(*)" + BOOKING_JOIN + "WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' " +
                "AND b.book_at >= ? AND b.book_at < ? " + t, start, end, theaterId));
            s.setTicketsSold((int) scalar(conn,
                "SELECT COUNT(*)" + SEAT_JOIN + "WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' " +
                "AND b.book_at >= ? AND b.book_at < ? " + t, start, end, theaterId));
            s.setGrossRevenue(scalar(conn,
                "SELECT COALESCE(SUM(p.amount), 0)" + PAYMENT_JOIN + "WHERE b.delete_at IS NULL " +
                "AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ? " + t, start, end, theaterId));
            s.setSuccessfulPayments((int) scalar(conn,
                "SELECT COUNT(*)" + PAYMENT_JOIN + "WHERE b.delete_at IS NULL " +
                "AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ? " + t, start, end, theaterId));
            s.setRefundedBookings((int) scalar(conn,
                "SELECT COUNT(*)" + BOOKING_JOIN + "WHERE b.delete_at IS NULL AND b.status = 'REFUNDED' " +
                "AND b.book_at >= ? AND b.book_at < ? " + t, start, end, theaterId));
            s.setRevenueOnRefundedBookings(scalar(conn,
                "SELECT COALESCE(SUM(p.amount), 0)" + PAYMENT_JOIN + "WHERE b.delete_at IS NULL " +
                "AND b.status = 'REFUNDED' AND p.status = 'SUCCESS' " +
                "AND p.paid_at >= ? AND p.paid_at < ? " + t, start, end, theaterId));
        }
        return s;
    }

    /* One row per day of the range (zeros filled in): revenue by paid_at, confirmed bookings by book_at. */
    public List<ManagerDailyStatistic> getDaily(Integer theaterId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        Timestamp start = from(startDate);
        Timestamp end = from(endDate.plusDays(1));
        String t = theaterClause(theaterId);
        Map<LocalDate, Long> revenue = new HashMap<>();
        Map<LocalDate, Integer> bookings = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CAST(p.paid_at AS date) AS d, SUM(p.amount) AS v" + PAYMENT_JOIN +
                    "WHERE b.delete_at IS NULL AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ? " + t +
                    "GROUP BY CAST(p.paid_at AS date)")) {
                bind(ps, 1, start, end, theaterId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        revenue.put(rs.getDate("d").toLocalDate(), rs.getLong("v"));
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CAST(b.book_at AS date) AS d, COUNT(*) AS v" + BOOKING_JOIN +
                    "WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' AND b.book_at >= ? AND b.book_at < ? " + t +
                    "GROUP BY CAST(b.book_at AS date)")) {
                bind(ps, 1, start, end, theaterId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        bookings.put(rs.getDate("d").toLocalDate(), rs.getInt("v"));
                    }
                }
            }
        }
        List<ManagerDailyStatistic> days = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            days.add(new ManagerDailyStatistic(d, revenue.getOrDefault(d, 0L), bookings.getOrDefault(d, 0)));
        }
        return days;
    }

    /* Top 10 movies by successful payment revenue; tickets = seats on bookings with a SUCCESS payment in the period. */
    public List<ManagerMovieStatistic> getTopMovies(Integer theaterId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        Timestamp start = from(startDate);
        Timestamp end = from(endDate.plusDays(1));
        String t = theaterClause(theaterId);
        String sql =
            "WITH movie_revenue AS ( " +
            "  SELECT s.movie_id, SUM(p.amount) AS revenue" + PAYMENT_JOIN +
            "  WHERE b.delete_at IS NULL AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ? " + t +
            "  GROUP BY s.movie_id " +
            "), movie_tickets AS ( " +
            "  SELECT s.movie_id, COUNT(*) AS tickets_sold" + SEAT_JOIN +
            "  INNER JOIN Payments p ON p.booking_id = b.booking_id AND p.status = 'SUCCESS' " +
            "  WHERE b.delete_at IS NULL AND p.paid_at >= ? AND p.paid_at < ? " + t +
            "  GROUP BY s.movie_id " +
            ") " +
            "SELECT TOP 10 m.movie_id, m.movie_name, mr.revenue, COALESCE(mt.tickets_sold, 0) AS tickets_sold " +
            "FROM movie_revenue mr INNER JOIN Movies m ON m.movie_id = mr.movie_id " +
            "LEFT JOIN movie_tickets mt ON mt.movie_id = mr.movie_id " +
            "ORDER BY mr.revenue DESC, m.movie_name ASC";
        List<ManagerMovieStatistic> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = bind(ps, 1, start, end, theaterId);
            bind(ps, i, start, end, theaterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ManagerMovieStatistic m = new ManagerMovieStatistic();
                    m.setMovieId(rs.getInt("movie_id"));
                    m.setMovieName(rs.getString("movie_name"));
                    m.setRevenue(rs.getLong("revenue"));
                    m.setTicketsSold(rs.getInt("tickets_sold"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    /* Every non-deleted theater (active or not) for the period; three separate aggregates, never mixed joins. */
    public List<AdminTheaterStatistic> getTheaterComparison(LocalDate startDate, LocalDate endDate)
            throws SQLException {
        Timestamp start = from(startDate);
        Timestamp end = from(endDate.plusDays(1));
        String sql =
            "SELECT t.theater_id, t.theater_name, t.isActive, " +
            " COALESCE(bk.bookings, 0) AS bookings, COALESCE(tk.tickets, 0) AS tickets, COALESCE(rv.revenue, 0) AS revenue " +
            "FROM Theaters t " +
            "LEFT JOIN (SELECT r.theater_id, COUNT(*) AS bookings" + BOOKING_JOIN +
            "   WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' AND b.book_at >= ? AND b.book_at < ? " +
            "   GROUP BY r.theater_id) bk ON bk.theater_id = t.theater_id " +
            "LEFT JOIN (SELECT r.theater_id, COUNT(*) AS tickets" + SEAT_JOIN +
            "   WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' AND b.book_at >= ? AND b.book_at < ? " +
            "   GROUP BY r.theater_id) tk ON tk.theater_id = t.theater_id " +
            "LEFT JOIN (SELECT r.theater_id, SUM(p.amount) AS revenue" + PAYMENT_JOIN +
            "   WHERE b.delete_at IS NULL AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ? " +
            "   GROUP BY r.theater_id) rv ON rv.theater_id = t.theater_id " +
            "WHERE t.deleted_at IS NULL ORDER BY revenue DESC, t.theater_name ASC";
        List<AdminTheaterStatistic> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int k = 0; k < 3; k++) {
                ps.setTimestamp(k * 2 + 1, start);
                ps.setTimestamp(k * 2 + 2, end);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new AdminTheaterStatistic(rs.getInt("theater_id"), rs.getString("theater_name"),
                        rs.getBoolean("isActive"), rs.getInt("bookings"), rs.getInt("tickets"), rs.getLong("revenue")));
                }
            }
        }
        return rows;
    }

    /* Successful payments by the Payments.method text stored with each payment. */
    public List<AdminPaymentMethodStatistic> getPaymentMethods(Integer theaterId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        Timestamp start = from(startDate);
        Timestamp end = from(endDate.plusDays(1));
        String sql =
            "SELECT COALESCE(p.method, 'Unknown') AS method, COUNT(*) AS payments, SUM(p.amount) AS revenue" +
            PAYMENT_JOIN + "WHERE b.delete_at IS NULL AND p.status = 'SUCCESS' " +
            "AND p.paid_at >= ? AND p.paid_at < ? " + theaterClause(theaterId) +
            "GROUP BY COALESCE(p.method, 'Unknown') ORDER BY revenue DESC";
        List<AdminPaymentMethodStatistic> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, 1, start, end, theaterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AdminPaymentMethodStatistic(rs.getString("method"), rs.getInt("payments"), rs.getLong("revenue")));
                }
            }
        }
        return list;
    }

    /* Bookings created in the period by current status (PENDING, CONFIRMED, REFUNDED, CANCELLED, ...). */
    public Map<String, Integer> getBookingStatusCounts(Integer theaterId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        Timestamp start = from(startDate);
        Timestamp end = from(endDate.plusDays(1));
        String sql = "SELECT b.status, COUNT(*) AS c" + BOOKING_JOIN +
            "WHERE b.delete_at IS NULL AND b.book_at >= ? AND b.book_at < ? " + theaterClause(theaterId) +
            "GROUP BY b.status ORDER BY c DESC";
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, 1, start, end, theaterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("status"), rs.getInt("c"));
                }
            }
        }
        return counts;
    }
}

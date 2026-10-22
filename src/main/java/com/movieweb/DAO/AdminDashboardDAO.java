package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminDashboardSummary;
import com.movieweb.model.AdminTheaterStatistic;
import com.movieweb.util.DBConnection;

/* System_wide dashboard queries. Every method propagates SQLException so a failure is
 * never shown as a misleading zero. theaterId == null means "all theaters".
 * Historical data is NOT filtered by Theaters/Rooms/Movies isActive or deleted_at.
 * Revenue is gross successful payment revenue (refunds are not deducted).
 * Booking and ticket counts use book_at; revenue uses paid_at.
 */
public class AdminDashboardDAO {

    public AdminDashboardSummary getSummary(Integer theaterId, LocalDate today) throws SQLException {
        AdminDashboardSummary s = new AdminDashboardSummary();
        Timestamp todayStart = Timestamp.valueOf(today.atStartOfDay());
        Timestamp tomorrowStart = Timestamp.valueOf(today.plusDays(1).atStartOfDay());
        Timestamp weekStart = Timestamp.valueOf(today.minusDays(6).atStartOfDay());

        try (Connection conn = DBConnection.getConnection()) {
            // Account / theater counts are always system-wide.
            s.setActiveTheaters(count(conn,
                "SELECT COUNT(*) FROM Theaters WHERE isActive = 1 AND deleted_at IS NULL"));
            s.setActiveUsers(count(conn,
                "SELECT COUNT(*) FROM Users WHERE role = 'USER' AND isActive = 1 AND deleted_at IS NULL"));
            s.setActiveManagers(count(conn,
                "SELECT COUNT(*) FROM Users WHERE role = 'MANAGER' AND isActive = 1 AND deleted_at IS NULL"));
            s.setReportedComments(count(conn,
                "SELECT COUNT(DISTINCT comment_id) FROM Reports"));
            // "New" manager = no row in Managers (never assigned to a theater).
            s.setPendingManagers(count(conn,
                "SELECT COUNT(*) FROM Users u WHERE u.role = 'MANAGER' AND u.isActive = 0 AND u.deleted_at IS NULL " +
                "AND NOT EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = u.user_id)"));
            s.setDeactivatedManagers(count(conn,
                "SELECT COUNT(*) FROM Users u WHERE u.role = 'MANAGER' AND u.isActive = 0 AND u.deleted_at IS NULL " +
                "AND EXISTS (SELECT 1 FROM Managers m WHERE m.user_id = u.user_id)"));

            String scope = theaterId == null ? "" : " AND r.theater_id = ? ";
            String from =
                " FROM Booking_showtimes b " +
                " INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
                " INNER JOIN Rooms r ON r.room_id = s.room_id ";
            String payFrom =
                " FROM Payments p " +
                " INNER JOIN Booking_showtimes b ON b.booking_id = p.booking_id " +
                " INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
                " INNER JOIN Rooms r ON r.room_id = s.room_id ";

            s.setConfirmedBookingsToday((int) scalar(conn,
                "SELECT COUNT(*)" + from + "WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' " +
                "AND b.book_at >= ? AND b.book_at < ?" + scope, theaterId, todayStart, tomorrowStart));
            s.setConfirmedBookings7Days((int) scalar(conn,
                "SELECT COUNT(*)" + from + "WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' " +
                "AND b.book_at >= ? AND b.book_at < ?" + scope, theaterId, weekStart, tomorrowStart));
            // Tickets = Booking_seats rows (a booking can hold several).
            s.setTicketsSold7Days((int) scalar(conn,
                "SELECT COUNT(*) FROM Booking_seats bs " +
                "INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
                "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
                "INNER JOIN Rooms r ON r.room_id = s.room_id " +
                "WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' " +
                "AND b.book_at >= ? AND b.book_at < ?" + scope, theaterId, weekStart, tomorrowStart));
            // Payments is joined to bookings only (never to Booking_seats), so amounts are not duplicated.
            s.setGrossRevenueToday(scalar(conn,
                "SELECT COALESCE(SUM(p.amount), 0)" + payFrom + "WHERE b.delete_at IS NULL " +
                "AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ?" + scope,
                theaterId, todayStart, tomorrowStart));
            s.setGrossRevenue7Days(scalar(conn,
                "SELECT COALESCE(SUM(p.amount), 0)" + payFrom + "WHERE b.delete_at IS NULL " +
                "AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ?" + scope,
                theaterId, weekStart, tomorrowStart));
            s.setSuccessfulPayments7Days((int) scalar(conn,
                "SELECT COUNT(*)" + payFrom + "WHERE b.delete_at IS NULL " +
                "AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ?" + scope,
                theaterId, weekStart, tomorrowStart));
            s.setRefundedBookings((int) scalar(conn,
                "SELECT COUNT(*)" + from + "WHERE b.delete_at IS NULL AND b.status = 'REFUNDED'" + scope,
                theaterId));
        }
        return s;
    }

    /* Last-7-days bookings, tickets and gross revenue per theater (all non-deleted theaters, incl. inactive). */
    public List<AdminTheaterStatistic> getTheaterBreakdown(LocalDate today) throws SQLException {
        Timestamp weekStart = Timestamp.valueOf(today.minusDays(6).atStartOfDay());
        Timestamp tomorrowStart = Timestamp.valueOf(today.plusDays(1).atStartOfDay());
        // Three separate aggregates keyed by theater: different join cardinalities must not be mixed.
        String sql =
            "SELECT t.theater_id, t.theater_name, t.isActive, " +
            " COALESCE(bk.bookings, 0) AS bookings, COALESCE(tk.tickets, 0) AS tickets, " +
            " COALESCE(rv.revenue, 0) AS revenue " +
            "FROM Theaters t " +
            "LEFT JOIN (SELECT r.theater_id, COUNT(*) AS bookings " +
            "   FROM Booking_showtimes b INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "   INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "   WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' AND b.book_at >= ? AND b.book_at < ? " +
            "   GROUP BY r.theater_id) bk ON bk.theater_id = t.theater_id " +
            "LEFT JOIN (SELECT r.theater_id, COUNT(*) AS tickets " +
            "   FROM Booking_seats bs INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
            "   INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "   INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "   WHERE b.delete_at IS NULL AND b.status = 'CONFIRMED' AND b.book_at >= ? AND b.book_at < ? " +
            "   GROUP BY r.theater_id) tk ON tk.theater_id = t.theater_id " +
            "LEFT JOIN (SELECT r.theater_id, SUM(p.amount) AS revenue " +
            "   FROM Payments p INNER JOIN Booking_showtimes b ON b.booking_id = p.booking_id " +
            "   INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "   INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "   WHERE b.delete_at IS NULL AND p.status = 'SUCCESS' AND p.paid_at >= ? AND p.paid_at < ? " +
            "   GROUP BY r.theater_id) rv ON rv.theater_id = t.theater_id " +
            "WHERE t.deleted_at IS NULL " +
            "ORDER BY revenue DESC, t.theater_name ASC";
        List<AdminTheaterStatistic> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < 3; i++) {
                ps.setTimestamp(i * 2 + 1, weekStart);
                ps.setTimestamp(i * 2 + 2, tomorrowStart);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new AdminTheaterStatistic(
                        rs.getInt("theater_id"), rs.getString("theater_name"),
                        rs.getBoolean("isActive"), rs.getInt("bookings"),
                        rs.getInt("tickets"), rs.getLong("revenue")));
                }
            }
        }
        return rows;
    }

    private int count(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /* Binds (timestamps...) then the optional theater id, and returns the first column as long. */
    private long scalar(Connection conn, String sql, Integer theaterId, Timestamp... range) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Timestamp t : range) {
                ps.setTimestamp(i++, t);
            }
            if (theaterId != null && sql.contains("r.theater_id = ?")) {
                ps.setInt(i, theaterId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    private long scalar(Connection conn, String sql, Integer theaterId) throws SQLException {
        return scalar(conn, sql, theaterId, new Timestamp[0]);
    }
}

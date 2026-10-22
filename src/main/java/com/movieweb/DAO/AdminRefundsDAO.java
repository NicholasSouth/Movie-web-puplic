package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.AdminRefundRow;
import com.movieweb.util.DBConnection;

/* Admin refund review. There is no separate refund-request table, so the request state is derived from
 * the two records that ManagerRefundServlet / Booking_showtimesDAO.requestRefund leave behind:
 *   PENDING  request : Booking_showtimes.status = 'REFUNDED'  AND  Payments.status = 'SUCCESS'
 *   APPROVED refund  : Booking_showtimes.status = 'CANCELLED' AND  Payments.status = 'REFUNDED'
 * Approve: booking REFUNDED -> CANCELLED and payment SUCCESS -> REFUNDED, in one transaction.
 * Reject : booking REFUNDED -> CONFIRMED (payment stays SUCCESS), refused if a seat was sold meanwhile.
 * Both decisions are guarded by the pending state in their WHERE clauses, and both lock the booking row
 * first, so a repeated, stale or concurrent decision changes nothing (Outcome.NOT_PENDING).
 * A rejected request leaves no trace: there is no audit table or request-status column to record it in.
 */
public class AdminRefundsDAO {

    /* Result of a decision. DB failures are thrown as SQLException, never reported as an outcome. */
    public enum Outcome { DONE, NOT_PENDING, SEAT_TAKEN }

    /* Same hold time as BookingService / PaymentService / ManagerBookingService (each keeps its own copy). */
    private static final int HOLD_MINUTES = 15;

    private static final String PENDING = "b.status = 'REFUNDED' AND p.status = 'SUCCESS'";
    private static final String APPROVED = "b.status = 'CANCELLED' AND p.status = 'REFUNDED'";

    private static final String SELECT_COLUMNS =
        "SELECT b.booking_id, b.book_at, b.status AS booking_status, b.price, " +
        "u.full_name, u.email, m.movie_name, s.start_at, t.theater_name, r.room_name, " +
        // Tickets are counted in a subquery so the payment row is never multiplied by seats.
        "(SELECT COUNT(*) FROM Booking_seats bs WHERE bs.booking_id = b.booking_id) AS ticket_count, " +
        "p.status AS payment_status, p.method AS payment_method, p.amount AS payment_amount, p.transaction_code";

    private static final String FROM =
        " FROM Booking_showtimes b " +
        "INNER JOIN Payments p ON p.booking_id = b.booking_id " +
        "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
        "INNER JOIN Movies m ON m.movie_id = s.movie_id " +
        "INNER JOIN Rooms r ON r.room_id = s.room_id " +
        "INNER JOIN Theaters t ON t.theater_id = r.theater_id " +
        "LEFT JOIN Users u ON u.user_id = b.user_id ";

    // Inactive / soft-deleted theaters, rooms and movies are deliberately not filtered out:
    // a refund must stay reviewable after the theater or movie is switched off.
    private String where(String view, Integer theaterId, String search) {
        StringBuilder sb = new StringBuilder("WHERE b.delete_at IS NULL AND ");
        sb.append("APPROVED".equals(view) ? APPROVED : PENDING);
        if (theaterId != null) sb.append(" AND t.theater_id = ?");
        if (search != null && !search.isEmpty()) {
            sb.append(" AND (p.transaction_code LIKE ? OR CAST(b.booking_id AS varchar(20)) = ?)");
        }
        return sb.toString();
    }

    private int bind(PreparedStatement ps, int i, Integer theaterId, String search) throws SQLException {
        if (theaterId != null) ps.setInt(i++, theaterId);
        if (search != null && !search.isEmpty()) {
            ps.setString(i++, "%" + search + "%");
            ps.setString(i++, search);
        }
        return i;
    }

    public int count(String view, Integer theaterId, String search) throws SQLException {
        String sql = "SELECT COUNT(*)" + FROM + where(view, theaterId, search);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, 1, theaterId, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<AdminRefundRow> list(String view, Integer theaterId, String search, int offset, int limit)
            throws SQLException {
        String sql =
            SELECT_COLUMNS +
            FROM + where(view, theaterId, search) +
            " ORDER BY b.book_at DESC, b.booking_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminRefundRow> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = bind(ps, 1, theaterId, search);
            ps.setInt(i++, offset);
            ps.setInt(i, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(map(rs));
                }
            }
        }
        return rows;
    }

    /* Approves a pending request: booking -> CANCELLED and payment -> REFUNDED, atomically. */
    public Outcome approve(int bookingId) throws SQLException {
        // The booking row is updated first (and locked); a concurrent decision on the same booking waits
        // here and then finds it no longer REFUNDED, so a refund can never be applied twice.
        String bookingSql =
            "UPDATE b SET b.status = 'CANCELLED' FROM Booking_showtimes b " +
            "WHERE b.booking_id = ? AND b.status = 'REFUNDED' AND b.delete_at IS NULL " +
            "AND EXISTS (SELECT 1 FROM Payments p WHERE p.booking_id = b.booking_id AND p.status = 'SUCCESS')";
        String paymentSql = "UPDATE Payments SET status = 'REFUNDED' WHERE booking_id = ? AND status = 'SUCCESS'";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (executeOne(conn, bookingSql, bookingId) != 1) {
                    conn.rollback();
                    return Outcome.NOT_PENDING;
                }
                if (executeOne(conn, paymentSql, bookingId) != 1) {
                    conn.rollback();
                    return Outcome.NOT_PENDING;
                }
                conn.commit();
                return Outcome.DONE;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /* Rejects a pending request: the booking goes back to CONFIRMED and the payment is untouched.
     * While a booking is REFUNDED its seats count as free (BookingService only blocks PENDING/CONFIRMED),
     * so another customer may have bought them; in that case nothing is changed and SEAT_TAKEN is returned.
     */
    public Outcome reject(int bookingId) throws SQLException {
        String pendingCheck =
            "b.booking_id = ? AND b.status = 'REFUNDED' AND b.delete_at IS NULL " +
            "AND EXISTS (SELECT 1 FROM Payments p WHERE p.booking_id = b.booking_id AND p.status = 'SUCCESS')";
        String restoreSql =
            "UPDATE b SET b.status = 'CONFIRMED' FROM Booking_showtimes b WHERE " + pendingCheck + " " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM Booking_seats mine " +
            "    INNER JOIN Booking_seats other ON other.seat_id = mine.seat_id " +
            "    INNER JOIN Booking_showtimes ob ON ob.booking_id = other.booking_id " +
            "    WHERE mine.booking_id = b.booking_id " +
            "    AND ob.booking_id <> b.booking_id " +
            "    AND ob.showtime_id = b.showtime_id " +
            "    AND ob.delete_at IS NULL " +
            "    AND ob.status IN ('PENDING', 'CONFIRMED') " +
            "    AND (ob.status = 'CONFIRMED' OR DATEADD(MINUTE, ?, ob.book_at) > GETDATE()) " +
            ")";
        String stillPendingSql = "SELECT 1 FROM Booking_showtimes b WHERE " + pendingCheck;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int changed;
                try (PreparedStatement ps = conn.prepareStatement(restoreSql)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, HOLD_MINUTES);
                    changed = ps.executeUpdate();
                }
                if (changed == 1) {
                    conn.commit();
                    return Outcome.DONE;
                }
                // Nothing changed: either it is no longer pending, or a seat has been taken.
                boolean stillPending;
                try (PreparedStatement ps = conn.prepareStatement(stillPendingSql)) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        stillPending = rs.next();
                    }
                }
                conn.rollback();
                return stillPending ? Outcome.SEAT_TAKEN : Outcome.NOT_PENDING;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }


    /* Booking, customer and payment details used for the decision email, whatever state the booking is in
     * now (call it after a decision). Null if the booking has no payment or was soft-deleted.
     * Bookings created at the counter (POS) belong to the manager's account, so that is who gets the email.
     */
    public AdminRefundRow getNotificationDetails(int bookingId) throws SQLException {
        String sql = SELECT_COLUMNS + FROM + "WHERE b.booking_id = ? AND b.delete_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private AdminRefundRow map(ResultSet rs) throws SQLException {
        AdminRefundRow r = new AdminRefundRow();
        r.setBookingId(rs.getInt("booking_id"));
        r.setBookAt(rs.getTimestamp("book_at"));
        r.setBookingStatus(rs.getString("booking_status"));
        r.setBookingPrice(rs.getInt("price"));
        r.setCustomerName(rs.getString("full_name"));
        r.setCustomerEmail(rs.getString("email"));
        r.setMovieName(rs.getString("movie_name"));
        r.setShowtimeStart(rs.getTimestamp("start_at"));
        r.setTheaterName(rs.getString("theater_name"));
        r.setRoomName(rs.getString("room_name"));
        r.setTicketCount(rs.getInt("ticket_count"));
        r.setPaymentStatus(rs.getString("payment_status"));
        r.setPaymentMethod(rs.getString("payment_method"));
        r.setPaymentAmount(rs.getInt("payment_amount"));
        r.setTransactionCode(rs.getString("transaction_code"));
        return r;
    }

    private int executeOne(Connection conn, String sql, int bookingId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate();
        }
    }
}

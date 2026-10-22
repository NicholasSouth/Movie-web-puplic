
package com.movieweb.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import com.movieweb.util.DBConnection;

public class PaymentService {

    private static final int HOLD_MINUTES = 15;

    //for USER
    public String payWithVisa(
            int user_id,
            int booking_id,
            int payment_method_id) throws SQLException {
        String transactionCode =
                "SIM-VISA-" + UUID.randomUUID()
                        .toString().replace("-", "")
                        .substring(0, 20).toUpperCase();
        String failureMessage = null;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Lock this booking so simultaneous payment requests
                // cannot both process it.
                String bookingSql =
                        "SELECT b.booking_id, b.user_id, b.showtime_id, " +
                        "b.status, b.price, b.book_at, b.delete_at, " +
                        "s.status AS showtime_status, s.start_at " +
                        "FROM Booking_showtimes b WITH (UPDLOCK, HOLDLOCK) " +
                        "INNER JOIN Showtimes s " +
                        "ON s.showtime_id = b.showtime_id " +
                        "WHERE b.booking_id = ? AND b.user_id = ?";
                int amount = 0;
                int showtime_id = 0;
                boolean bookingFound = false;
                boolean expired = false;
                try (PreparedStatement stmt = conn.prepareStatement(bookingSql)) {
                    stmt.setInt(1, booking_id);
                    stmt.setInt(2, user_id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            bookingFound = true;
                            String status = rs.getString("status");
                            String showtimeStatus = rs.getString("showtime_status");
                            java.sql.Timestamp bookAt = rs.getTimestamp("book_at");
                            java.sql.Timestamp startAt = rs.getTimestamp("start_at");
                            amount = rs.getInt("price");
                            showtime_id = rs.getInt("showtime_id");
                            if (!"PENDING".equalsIgnoreCase(status)) {
                                throw new IllegalArgumentException("This booking is not awaiting payment.");
                            }
                            if (rs.getTimestamp("delete_at") != null) {
                                throw new IllegalArgumentException("This booking is no longer valid.");
                            }

                            if (bookAt == null
                                || startAt == null
                                || !"SCHEDULED".equalsIgnoreCase(showtimeStatus)
                                || !startAt.after(new java.sql.Timestamp(System.currentTimeMillis()))) {
                                throw new IllegalArgumentException("This showtime is no longer available.");
                            }

                            // Use SQL Server time to check the hold expiry.
                            try (PreparedStatement expiryStmt =
                                    conn.prepareStatement(
                                            "SELECT CASE WHEN " +
                                            "DATEADD(MINUTE, ?, book_at) " +
                                            "<= GETDATE() THEN 1 ELSE 0 END " +
                                            "FROM Booking_showtimes " +
                                            "WHERE booking_id = ?")) {
                                expiryStmt.setInt(1, HOLD_MINUTES);
                                expiryStmt.setInt(2, booking_id);
                                try (ResultSet expiryRs = expiryStmt.executeQuery()) {
                                    expired = expiryRs.next() && expiryRs.getInt(1) == 1;
                                }
                            }
                        }
                    }
                }
                if (!bookingFound) {
                    throw new IllegalArgumentException("Booking not found.");
                }
                if (expired) {
                    // Persist the cancellation before returning an error.
                    try (PreparedStatement stmt = conn.prepareStatement(
                                    "UPDATE Booking_showtimes " +
                                    "SET status = 'CANCELLED' " +
                                    "WHERE booking_id = ? " +
                                    "AND status = 'PENDING'")) {
                        stmt.setInt(1, booking_id);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    failureMessage ="Your 15-minute payment hold has expired. " + "Please book your seats again.";

                } 
                else {
                    if (amount <= 0) {
                        throw new IllegalArgumentException("Invalid booking amount.");
                    }

                    // Validate that the selected card belongs to this user and is an active VISA card.
                    String cardSql =
                            "SELECT payment_method_id " +
                            "FROM Payment_methods WITH (UPDLOCK, HOLDLOCK) " +
                            "WHERE payment_method_id = ? " +
                            "AND user_id = ? " +
                            "AND method = 'VISA' " +
                            "AND isActive = 1";
                    boolean validCard = false;
                    try (PreparedStatement stmt = conn.prepareStatement(cardSql)) {
                        stmt.setInt(1, payment_method_id);
                        stmt.setInt(2, user_id);
                        try (ResultSet rs = stmt.executeQuery()) {
                            validCard = rs.next();
                        }
                    }
                    if (!validCard) {
                        throw new IllegalArgumentException("Please select one of your active VISA cards.");
                    }

                    // Require at least one seat and ensure every seat still belongs to this booking.
                    String seatSql =
                            "SELECT COUNT(*) " +
                            "FROM Booking_seats " +
                            "WHERE booking_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(seatSql)) {
                        stmt.setInt(1, booking_id);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (!rs.next() || rs.getInt(1) == 0) {
                                throw new IllegalArgumentException("This booking has no seats.");
                            }
                        }
                    }

                    // Confirm only if the booking is still pending and has not expired.
                    String confirmSql =
                            "UPDATE Booking_showtimes " +
                            "SET status = 'CONFIRMED' " +
                            "WHERE booking_id = ? " +
                            "AND user_id = ? " +
                            "AND status = 'PENDING' " +
                            "AND delete_at IS NULL " +
                            "AND DATEADD(MINUTE, ?, book_at) > GETDATE()";
                    try (PreparedStatement stmt = conn.prepareStatement(confirmSql)) {
                        stmt.setInt(1, booking_id);
                        stmt.setInt(2, user_id);
                        stmt.setInt(3, HOLD_MINUTES);
                        if (stmt.executeUpdate() != 1) {
                            throw new IllegalArgumentException("The booking expired or is no longer payable.");
                        }
                    }

                    // Insert the simulated successful payment.
                    String paymentSql =
                            "INSERT INTO Payments " +
                            "(payment_method_id, method, booking_id, " +
                            "amount, paid_at, status, transaction_code) " +
                            "VALUES (?, 'VISA', ?, ?, GETDATE(), ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                        stmt.setInt(1, payment_method_id);
                        stmt.setInt(2, booking_id);
                        stmt.setInt(3, amount);
                        stmt.setString(4, "SUCCESS");
                        stmt.setString(5, transactionCode);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Failed to create payment record.");
                        }
                    }

                    // Both confirmation and payment record commit together.
                    conn.commit();
                }
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
                throw new SQLException("Payment processing failed.", e);
            }
        }
        if (failureMessage != null) {
            throw new IllegalArgumentException(failureMessage);
        }
        return transactionCode;
    }

    //for MANAGER
    public String payWithManagerVisa(
            int manager_id,
            int booking_id,
            int payment_method_id) throws SQLException {
        return processManagerPayment(
                manager_id,
                booking_id,
                "VISA",
                payment_method_id
        );
    }
    public String payWithManagerCash(
            int manager_id,
            int booking_id) throws SQLException {
        return processManagerPayment(
                manager_id,
                booking_id,
                "CASH",
                null
        );
    }
    public String payWithManagerQR(
            int manager_id,
            int booking_id) throws SQLException {

        return processManagerPayment(
                manager_id,
                booking_id,
                "QR",
                null
        );
    }
    private String processManagerPayment(
            int manager_id,
            int booking_id,
            String method,
            Integer payment_method_id) throws SQLException {
        String transactionCode =
                "SIM-" + method + "-"
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase();
        String failureMessage = null;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String bookingSql =
                        "SELECT b.booking_id, " +
                        "       b.user_id, " +
                        "       b.showtime_id, " +
                        "       b.status, " +
                        "       b.price, " +
                        "       b.book_at, " +
                        "       b.delete_at, " +
                        "       s.status AS showtime_status, " +
                        "       s.start_at " +
                        "FROM Booking_showtimes b WITH (UPDLOCK, HOLDLOCK) " +
                        "INNER JOIN Showtimes s " +
                        "    ON s.showtime_id = b.showtime_id " +
                        "INNER JOIN Rooms r " +
                        "    ON r.room_id = s.room_id " +
                        "INNER JOIN Theaters t " +
                        "    ON t.theater_id = r.theater_id " +
                        "INNER JOIN Managers m " +
                        "    ON m.theater_id = t.theater_id " +
                        "WHERE b.booking_id = ? " +
                        "AND m.user_id = ?";
                int amount = 0;
                boolean bookingFound = false;
                boolean expired = false;
                try (PreparedStatement stmt = conn.prepareStatement(bookingSql)) {
                    stmt.setInt(1, booking_id);
                    stmt.setInt(2, manager_id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            bookingFound = true;
                            String status = rs.getString("status");
                            String showtimeStatus = rs.getString("showtime_status");
                            java.sql.Timestamp bookAt = rs.getTimestamp("book_at");
                            java.sql.Timestamp startAt = rs.getTimestamp("start_at");
                            amount = rs.getInt("price");
                            if (!"PENDING".equalsIgnoreCase(status)) {
                                throw new IllegalArgumentException("This booking is not awaiting payment.");
                            }
                            if (rs.getTimestamp("delete_at") != null) {
                                throw new IllegalArgumentException("This booking is no longer valid.");
                            }
                            if (bookAt == null
                                    || startAt == null
                                    || !"SCHEDULED".equalsIgnoreCase(showtimeStatus)
                                    || !startAt.after(new java.sql.Timestamp(System.currentTimeMillis()))) {
                                throw new IllegalArgumentException("This showtime is no longer available.");
                            }
                            try (PreparedStatement expiryStmt = conn.prepareStatement(
                                            "SELECT CASE WHEN " +
                                            "DATEADD(MINUTE, ?, book_at) " +
                                            "<= GETDATE() THEN 1 ELSE 0 END " +
                                            "FROM Booking_showtimes " +
                                            "WHERE booking_id = ?")) {
                                expiryStmt.setInt(
                                        1,
                                        HOLD_MINUTES);
                                expiryStmt.setInt(
                                        2,
                                        booking_id);
                                try (ResultSet expiryRs = expiryStmt.executeQuery()) {
                                    expired = expiryRs.next() && expiryRs.getInt(1) == 1;
                                }
                            }
                        }
                    }
                }
                if (!bookingFound) {
                    throw new IllegalArgumentException("Booking not found or this booking does not belong to your assigned theater.");
                }
                if (expired) {
                    try (PreparedStatement stmt =
                            conn.prepareStatement(
                                    "UPDATE Booking_showtimes " +
                                    "SET status = 'CANCELLED' " +
                                    "WHERE booking_id = ? " +
                                    "AND status = 'PENDING'")) {
                        stmt.setInt(1, booking_id);
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    failureMessage = "Your 15-minute payment hold has expired. "
                            + "Please create the booking again.";
                } 
                else {
                    if (amount <= 0) {
                        throw new IllegalArgumentException("Invalid booking amount.");
                    }
                    String seatSql =
                            "SELECT COUNT(*) " +
                            "FROM Booking_seats " +
                            "WHERE booking_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(seatSql)) {
                        stmt.setInt(1, booking_id);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (!rs.next()
                                || rs.getInt(1) == 0) {
                                throw new IllegalArgumentException("This booking has no seats.");
                            }
                        }
                    }
                    if ("VISA".equalsIgnoreCase(method)) {
                        if (payment_method_id == null || payment_method_id <= 0) {
                            throw new IllegalArgumentException("Please select one of your active VISA cards.");
                        }
                        String cardSql =
                                "SELECT payment_method_id " +
                                "FROM Payment_methods WITH (UPDLOCK, HOLDLOCK) " +
                                "WHERE payment_method_id = ? " +
                                "AND user_id = ? " +
                                "AND method = 'VISA' " +
                                "AND isActive = 1";
                        boolean validCard = false;
                        try (PreparedStatement stmt = conn.prepareStatement(cardSql)) {
                            stmt.setInt(
                                    1,
                                    payment_method_id);
                            stmt.setInt(
                                    2,
                                    manager_id);
                            try (ResultSet rs = stmt.executeQuery()) {
                                validCard = rs.next();
                            }
                        }
                        if (!validCard) {
                            throw new IllegalArgumentException("Please select one of your active VISA cards.");
                        }
                    }
                    String confirmSql =
                            "UPDATE Booking_showtimes " +
                            "SET status = 'CONFIRMED' " +
                            "WHERE booking_id = ? " +
                            "AND status = 'PENDING' " +
                            "AND delete_at IS NULL " +
                            "AND DATEADD(MINUTE, ?, book_at) > GETDATE()";
                    try (PreparedStatement stmt = conn.prepareStatement(confirmSql)) {
                        stmt.setInt(
                                1,
                                booking_id);
                        stmt.setInt(
                                2,
                                HOLD_MINUTES);
                        if (stmt.executeUpdate() != 1) {
                            throw new IllegalArgumentException("The booking expired or is no longer payable.");
                        }
                    }
                    String paymentSql =
                            "INSERT INTO Payments " +
                            "(payment_method_id, method, booking_id, " +
                            "amount, paid_at, status, transaction_code) " +
                            "VALUES (?, ?, ?, ?, GETDATE(), ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                        if (payment_method_id == null) {
                            stmt.setNull(
                                    1,
                                    Types.INTEGER);
                        } 
                        else {
                            stmt.setInt(
                                    1,
                                    payment_method_id);
                        }
                        stmt.setString(
                                2,
                                method.toUpperCase());
                        stmt.setInt(
                                3,
                                booking_id);
                        stmt.setInt(
                                4,
                                amount);
                        stmt.setString(
                                5,
                                "SUCCESS");
                        stmt.setString(
                                6,
                                transactionCode);
                        if (stmt.executeUpdate() != 1) {
                            throw new SQLException("Failed to create payment record.");
                        }
                    }
                    conn.commit();
                }
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
                throw new SQLException("Manager payment processing failed.", e);
            }
        }
        if (failureMessage != null) {
            throw new IllegalArgumentException(failureMessage);
        }
        return transactionCode;
    }

}
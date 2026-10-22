package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.movieweb.model.ManagerDailyStatistic;
import com.movieweb.model.ManagerMovieStatistic;
import com.movieweb.model.ManagerStatistics;
import com.movieweb.util.DBConnection;

public class ManagerStatisticsDAO {

    /*
     * Date range is inclusive at both ends. Booking counts and ticket counts use
     * booking creation date (book_at); revenue uses successful payment date (paid_at).
     * Gross revenue intentionally includes SUCCESS payments even if the booking has
     * since been marked REFUNDED, because admin refund approval/payment updates do not
     * exist yet in this project.
     */
    public ManagerStatistics getSummaryByTheaterAndDateRange(int theaterId, LocalDate startDate,
            LocalDate endDate) throws SQLException {
        String sql =
            "SELECT " +
            " (SELECT COUNT(*) " +
            "  FROM Booking_showtimes b " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL " +
            "    AND b.status = 'CONFIRMED' " +
            "    AND b.book_at >= ? AND b.book_at < ?) AS confirmed_bookings, " +
            " (SELECT COUNT(*) " +
            "  FROM Booking_seats bs " +
            "  INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL " +
            "    AND b.status = 'CONFIRMED' " +
            "    AND b.book_at >= ? AND b.book_at < ?) AS tickets_sold, " +
            " (SELECT COALESCE(SUM(p.amount), 0) " +
            "  FROM Payments p " +
            "  INNER JOIN Booking_showtimes b ON b.booking_id = p.booking_id " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL " +
            "    AND p.status = 'SUCCESS' " +
            "    AND p.paid_at >= ? AND p.paid_at < ?) AS gross_revenue, " +
            " (SELECT COUNT(*) " +
            "  FROM Payments p " +
            "  INNER JOIN Booking_showtimes b ON b.booking_id = p.booking_id " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL " +
            "    AND p.status = 'SUCCESS' " +
            "    AND p.paid_at >= ? AND p.paid_at < ?) AS successful_payments";

        LocalDate exclusiveEnd = endDate.plusDays(1);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            setRange(ps, i, theaterId, startDate, exclusiveEnd); i += 3;
            setRange(ps, i, theaterId, startDate, exclusiveEnd); i += 3;
            setRange(ps, i, theaterId, startDate, exclusiveEnd); i += 3;
            setRange(ps, i, theaterId, startDate, exclusiveEnd);
            try (ResultSet rs = ps.executeQuery()) {
                ManagerStatistics result = new ManagerStatistics();
                if (rs.next()) {
                    result.setConfirmedBookings(rs.getInt("confirmed_bookings"));
                    result.setTicketsSold(rs.getInt("tickets_sold"));
                    result.setGrossRevenue(rs.getLong("gross_revenue"));
                    result.setSuccessfulPayments(rs.getInt("successful_payments"));
                }
                return result;
            }
        }
    }

    private void setRange(PreparedStatement ps, int startIndex, int theaterId,
            LocalDate startDate, LocalDate exclusiveEnd) throws SQLException {
        ps.setInt(startIndex, theaterId);
        ps.setTimestamp(startIndex + 1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
        ps.setTimestamp(startIndex + 2, java.sql.Timestamp.valueOf(exclusiveEnd.atStartOfDay()));
    }

    public List<ManagerDailyStatistic> getDailyStatisticsByTheaterAndDateRange(
            int theaterId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql =
            "SELECT daily_stat.stat_date, COALESCE(revenue_data.revenue, 0) AS revenue, " +
            "       COALESCE(booking_data.bookings, 0) AS bookings " +
            "FROM ( " +
            "  SELECT DATEADD(day, number, CAST(? AS date)) AS stat_date " +
            "  FROM master..spt_values " +
            "  WHERE type = 'P' AND number BETWEEN 0 AND DATEDIFF(day, CAST(? AS date), CAST(? AS date)) " +
            ") daily_stat " +
            "LEFT JOIN ( " +
            "  SELECT CAST(p.paid_at AS date) AS stat_date, SUM(p.amount) AS revenue " +
            "  FROM Payments p " +
            "  INNER JOIN Booking_showtimes b ON b.booking_id = p.booking_id " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL AND p.status = 'SUCCESS' " +
            "    AND p.paid_at >= ? AND p.paid_at < ? " +
            "  GROUP BY CAST(p.paid_at AS date) " +
            ") revenue_data ON revenue_data.stat_date = daily_stat.stat_date " +
            "LEFT JOIN ( " +
            "  SELECT CAST(b.book_at AS date) AS stat_date, COUNT(*) AS bookings " +
            "  FROM Booking_showtimes b " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL AND b.status = 'CONFIRMED' " +
            "    AND b.book_at >= ? AND b.book_at < ? " +
            "  GROUP BY CAST(b.book_at AS date) " +
            ") booking_data ON booking_data.stat_date = daily_stat.stat_date " +
            "ORDER BY daily_stat.stat_date";

        LocalDate exclusiveEnd = endDate.plusDays(1);
        List<ManagerDailyStatistic> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            ps.setInt(4, theaterId);
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(exclusiveEnd.atStartOfDay()));
            ps.setInt(7, theaterId);
            ps.setTimestamp(8, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(9, java.sql.Timestamp.valueOf(exclusiveEnd.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new ManagerDailyStatistic(
                        rs.getDate("stat_date").toLocalDate(),
                        rs.getLong("revenue"),
                        rs.getInt("bookings")
                    ));
                }
            }
        }
        return results;
    }

    public List<ManagerMovieStatistic> getTopMoviesByTheaterAndDateRange(
            int theaterId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql =
            "WITH movie_revenue AS ( " +
            "  SELECT s.movie_id, SUM(p.amount) AS revenue " +
            "  FROM Payments p " +
            "  INNER JOIN Booking_showtimes b ON b.booking_id = p.booking_id " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL AND p.status = 'SUCCESS' " +
            "    AND p.paid_at >= ? AND p.paid_at < ? " +
            "  GROUP BY s.movie_id " +
            "), movie_tickets AS ( " +
            "  SELECT s.movie_id, COUNT(*) AS tickets_sold " +
            "  FROM Booking_seats bs " +
            "  INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
            "  INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
            "  INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "  INNER JOIN Payments p ON p.booking_id = b.booking_id AND p.status = 'SUCCESS' " +
            "  WHERE r.theater_id = ? AND b.delete_at IS NULL " +
            "    AND p.paid_at >= ? AND p.paid_at < ? " +
            "  GROUP BY s.movie_id " +
            ") " +
            "SELECT TOP 5 m.movie_id, m.movie_name, COALESCE(mr.revenue, 0) AS revenue, " +
            "       COALESCE(mt.tickets_sold, 0) AS tickets_sold " +
            "FROM movie_revenue mr " +
            "INNER JOIN Movies m ON m.movie_id = mr.movie_id " +
            "LEFT JOIN movie_tickets mt ON mt.movie_id = mr.movie_id " +
            "ORDER BY mr.revenue DESC, m.movie_name ASC";
        LocalDate exclusiveEnd = endDate.plusDays(1);
        List<ManagerMovieStatistic> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theaterId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(exclusiveEnd.atStartOfDay()));
            ps.setInt(4, theaterId);
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(exclusiveEnd.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ManagerMovieStatistic movie = new ManagerMovieStatistic();
                    movie.setMovieId(rs.getInt("movie_id"));
                    movie.setMovieName(rs.getString("movie_name"));
                    movie.setRevenue(rs.getLong("revenue"));
                    movie.setTicketsSold(rs.getInt("tickets_sold"));
                    results.add(movie);
                }
            }
        }
        return results;
    }
    
	public ManagerMovieStatistic getTopSellingMovieByTheaterAndDateRange(
	        int theaterId, LocalDate startDate, LocalDate endDate) throws SQLException {
	    String sql =
	        "SELECT TOP 1 m.movie_id, m.movie_name, COUNT(*) AS tickets_sold " +
	        "FROM Booking_seats bs " +
	        "INNER JOIN Booking_showtimes b ON b.booking_id = bs.booking_id " +
	        "INNER JOIN Showtimes s ON s.showtime_id = b.showtime_id " +
	        "INNER JOIN Rooms r ON r.room_id = s.room_id " +
	        "INNER JOIN Payments p ON p.booking_id = b.booking_id " +
	        "    AND p.status = 'SUCCESS' " +
	        "INNER JOIN Movies m ON m.movie_id = s.movie_id " +
	        "WHERE r.theater_id = ? " +
	        "  AND b.delete_at IS NULL " +
	        "  AND p.paid_at >= ? AND p.paid_at < ? " +
	        "GROUP BY m.movie_id, m.movie_name " +
	        "ORDER BY COUNT(*) DESC, m.movie_name ASC";
	    LocalDate exclusiveEnd = endDate.plusDays(1);
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setInt(1, theaterId);
	        ps.setTimestamp(2, java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
	        ps.setTimestamp(3, java.sql.Timestamp.valueOf(exclusiveEnd.atStartOfDay()));
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                ManagerMovieStatistic movie = new ManagerMovieStatistic();
	                movie.setMovieId(rs.getInt("movie_id"));
	                movie.setMovieName(rs.getString("movie_name"));
	                movie.setTicketsSold(rs.getInt("tickets_sold"));
	                return movie;
	            }
	        }
	    }
	    return null;
	}

}

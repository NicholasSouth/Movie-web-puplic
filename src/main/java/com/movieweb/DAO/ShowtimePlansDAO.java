package com.movieweb.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.movieweb.model.Showtime_plan_items;
import com.movieweb.model.Showtime_plans;
import com.movieweb.service.ShowtimePlanDraft;
import com.movieweb.service.ShowtimePlannerService;
import com.movieweb.service.ShowtimePlannerService.Slot;
import com.movieweb.util.DBConnection;

public class ShowtimePlansDAO {

    /** True when the showtime (alias s) has at least one successful payment. */
    private static final String PAID_EXISTS =
        "EXISTS (SELECT 1 FROM Booking_showtimes b " +
        "        INNER JOIN Payments p ON p.booking_id = b.booking_id " +
        "        WHERE b.showtime_id = s.showtime_id AND UPPER(p.status) = 'SUCCESS')";

    /** Outcome of {@link #confirmPlan}. */
    public static class ConfirmResult {
        public int planId;
        public int created;
        public int skipped;
        public long totalCost;
        public long profit;
    }

    // ------------------------------------------------------------ planning

    /** room_id -> number of usable seats, for every open room of the theater. */
    public Map<Integer, Integer> getActiveSeatCountsByTheater(int theaterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return seatCounts(conn, theaterId);
        }
    }

    private Map<Integer, Integer> seatCounts(Connection conn, int theaterId) throws SQLException {
        String sql =
            "SELECT r.room_id, COUNT(s.seat_id) AS seat_count " +
            "FROM Rooms r " +
            "LEFT JOIN Seats s ON s.room_id = r.room_id " +
            "    AND s.isActive = 1 AND s.deleted_at IS NULL " +
            "WHERE r.theater_id = ? AND r.isActive = 1 AND r.deleted_at IS NULL " +
            "GROUP BY r.room_id";
        Map<Integer, Integer> map = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("room_id"), rs.getInt("seat_count"));
                }
            }
        }
        return map;
    }

    /** Existing (not deleted, not canceled) showtimes of the rooms that touch [from, to]. */
    public Map<Integer, List<LocalDateTime[]>> getBusyIntervals(
            Collection<Integer> roomIds, LocalDateTime from, LocalDateTime to) throws SQLException {
        Map<Integer, List<LocalDateTime[]>> map = new HashMap<>();
        if (roomIds == null || roomIds.isEmpty()) {
            return map;
        }
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < roomIds.size(); i++) {
            marks.append(i == 0 ? "?" : ",?");
        }
        String sql =
            "SELECT room_id, start_at, end_at FROM Showtimes " +
            "WHERE room_id IN (" + marks + ") " +
            "AND deleted_at IS NULL AND UPPER(status) <> 'CANCELED' " +
            "AND end_at > ? AND start_at < ? " +
            "ORDER BY start_at";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int i = 1;
            for (Integer id : roomIds) {
                stmt.setInt(i++, id);
            }
            stmt.setTimestamp(i++, Timestamp.valueOf(from));
            stmt.setTimestamp(i, Timestamp.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.computeIfAbsent(rs.getInt("room_id"), k -> new ArrayList<>())
                       .add(new LocalDateTime[] {
                           rs.getTimestamp("start_at").toLocalDateTime(),
                           rs.getTimestamp("end_at").toLocalDateTime() });
                }
            }
        }
        return map;
    }

    // -------------------------------------------------------------- confirm

    /**
     * Creates the chosen showtimes and the history rows in ONE transaction.
     * Everything the browser could have tampered with is checked again here:
     * manager/theater, movie, rooms, seat counts, cost, budget and room conflicts.
     *
     * @throws IllegalArgumentException with a message that can be shown to the manager
     */
    public ConfirmResult confirmPlan(int userId, ShowtimePlanDraft draft, Set<Integer> chosenIds) throws SQLException {
        ShowtimePlannerService.Params p = draft.getParams();
        int theaterId = draft.getTheaterId();

        List<Slot> chosen = new ArrayList<>();
        for (Slot s : draft.getSlots()) {
            if (chosenIds.contains(s.getId())) {
                chosen.add(s);
            }
        }
        chosen.sort(Comparator.comparing(Slot::getStartAt).thenComparingInt(Slot::getRoomId));

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. The manager runs this theater.
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT 1 FROM Managers WHERE user_id = ? AND theater_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, theaterId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("You are not assigned to this theater.");
                        }
                    }
                }

                // 2. The movie is still active and its availability dates.
                LocalDate movieFrom = null;
                LocalDate movieUntil = null;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT available_from, available_until FROM Movies " +
                        "WHERE movie_id = ? AND isActive = 1 AND deleted_at IS NULL")) {
                    stmt.setInt(1, p.movieId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("The movie is no longer available.");
                        }
                        Date from = rs.getDate("available_from");
                        Date until = rs.getDate("available_until");
                        movieFrom = from == null ? null : from.toLocalDate();
                        movieUntil = until == null ? null : until.toLocalDate();
                    }
                }

                // 3. Fresh seat counts decide the real cost, not the preview page.
                Map<Integer, Integer> seats = seatCounts(conn, theaterId);
                List<Slot> usable = new ArrayList<>();
                int skipped = 0;
                long totalCost = 0;
                LocalDateTime now = LocalDateTime.now();
                for (Slot s : chosen) {
                    Integer seatCount = seats.get(s.getRoomId());
                    LocalDate day = s.getStartAt().toLocalDate();
                    boolean movieOk = (movieFrom == null || !day.isBefore(movieFrom))
                            && (movieUntil == null || !day.isAfter(movieUntil));
                    if (seatCount == null || seatCount <= 0 || !movieOk || !s.getStartAt().isAfter(now)) {
                        skipped++;
                        continue;
                    }
                    usable.add(s);
                    totalCost += ShowtimePlannerService.slotCost(seatCount, p.basePrice);
                }
                if (usable.isEmpty()) {
                    throw new IllegalArgumentException("None of the selected showtimes can be created any more. Please generate a new plan.");
                }
                if (totalCost > p.budget) {
                    throw new IllegalArgumentException("The selected showtimes cost more than the budget.");
                }

                // 4. Plan header (numbers are filled in at the end).
                int planId;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO Showtime_plans " +
                        "(theater_id, manager_id, movie_id, client_name, budget, base_price, " +
                        " period_start, period_end, gap_minutes, showtime_count, total_cost, profit) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, theaterId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, p.movieId);
                    stmt.setString(4, p.clientName);
                    stmt.setLong(5, p.budget);
                    stmt.setInt(6, p.basePrice);
                    stmt.setDate(7, Date.valueOf(p.dateFrom));
                    stmt.setDate(8, Date.valueOf(p.dateTo));
                    stmt.setInt(9, p.gapMinutes);
                    stmt.setLong(10, p.budget);
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Could not read the new plan id.");
                        }
                        planId = keys.getInt(1);
                    }
                }

                // 5. Showtimes, price rows and plan items.
                String conflictSql =
                    "SELECT 1 FROM Showtimes WHERE room_id = ? AND deleted_at IS NULL " +
                    "AND UPPER(status) <> 'CANCELED' AND start_at < ? AND end_at > ?";
                String insertShowtimeSql =
                    "INSERT INTO Showtimes (room_id, movie_id, start_at, end_at, status) " +
                    "VALUES (?, ?, ?, ?, 'SCHEDULED')";
                String insertPriceSql =
                    "INSERT INTO Showtime_ticket_types (showtime_id, ticket_type_id, price) VALUES (?, 1, ?)";
                String insertItemSql =
                    "INSERT INTO Showtime_plan_items (plan_id, showtime_id, cost) VALUES (?, ?, ?)";

                int created = 0;
                long createdCost = 0;
                try (PreparedStatement conflict = conn.prepareStatement(conflictSql);
                     PreparedStatement insertShowtime = conn.prepareStatement(insertShowtimeSql, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement insertPrice = conn.prepareStatement(insertPriceSql);
                     PreparedStatement insertItem = conn.prepareStatement(insertItemSql)) {
                    for (Slot s : usable) {
                        Timestamp start = Timestamp.valueOf(s.getStartAt());
                        Timestamp end = Timestamp.valueOf(s.getEndAt());

                        conflict.setInt(1, s.getRoomId());
                        conflict.setTimestamp(2, end);
                        conflict.setTimestamp(3, start);
                        try (ResultSet rs = conflict.executeQuery()) {
                            if (rs.next()) {            // somebody scheduled this room meanwhile
                                skipped++;
                                continue;
                            }
                        }

                        insertShowtime.setInt(1, s.getRoomId());
                        insertShowtime.setInt(2, p.movieId);
                        insertShowtime.setTimestamp(3, start);
                        insertShowtime.setTimestamp(4, end);
                        insertShowtime.executeUpdate();
                        int showtimeId;
                        try (ResultSet keys = insertShowtime.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Could not read the new showtime id.");
                            }
                            showtimeId = keys.getInt(1);
                        }

                        insertPrice.setInt(1, showtimeId);
                        insertPrice.setInt(2, p.basePrice);
                        insertPrice.executeUpdate();

                        long cost = ShowtimePlannerService.slotCost(seats.get(s.getRoomId()), p.basePrice);
                        insertItem.setInt(1, planId);
                        insertItem.setInt(2, showtimeId);
                        insertItem.setLong(3, cost);
                        insertItem.executeUpdate();

                        created++;
                        createdCost += cost;
                    }
                }
                if (created == 0) {
                    throw new IllegalArgumentException("Every selected showtime now conflicts with another one. Please generate a new plan.");
                }

                // 6. Final numbers of the plan.
                long profit = p.budget - createdCost;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Showtime_plans SET showtime_count = ?, total_cost = ?, profit = ? WHERE plan_id = ?")) {
                    stmt.setInt(1, created);
                    stmt.setLong(2, createdCost);
                    stmt.setLong(3, profit);
                    stmt.setInt(4, planId);
                    stmt.executeUpdate();
                }
                conn.commit();

                ConfirmResult result = new ConfirmResult();
                result.planId = planId;
                result.created = created;
                result.skipped = skipped;
                result.totalCost = createdCost;
                result.profit = profit;
                return result;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // --------------------------------------------------------------- delete

    /** Outcome of {@link #deletePlan}. */
    public static class DeleteResult {
        public int removedNow;       // showtimes this call deleted
        public int alreadyRemoved;   // picked, but somebody had removed them already
        public int keptPaid;         // picked, but kept because they have a successful payment
        public int keptByChoice;     // not picked by the manager, so they stay as normal showtimes
    }

    /**
     * Deletes a plan. The showtimes the manager PICKED are deleted together with it;
     * the others stay in Showtimes Management as ordinary showtimes. All in ONE transaction.
     * Everything is decided from the database state at this moment, not from what the page showed:
     * - showtimes somebody already removed one by one are skipped (and counted);
     * - showtimes with a successful payment are never deleted (and counted), even if picked;
     * - ids that do not belong to the plan are ignored;
     * - picking nothing is allowed: the plan is deleted and every showtime stays.
     *
     * @throws IllegalArgumentException with a message that can be shown to the manager
     */
    public DeleteResult deletePlan(int userId, int theaterId, int planId, Set<Integer> pickedIds) throws SQLException {
        if (pickedIds == null) {
            pickedIds = new HashSet<>();
        }
        StringBuilder marks = new StringBuilder();
        for (int i = 0; i < pickedIds.size(); i++) {
            marks.append(i == 0 ? "?" : ",?");
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. The manager runs this theater.
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT 1 FROM Managers WHERE user_id = ? AND theater_id = ?")) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, theaterId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("You are not assigned to this theater.");
                        }
                    }
                }

                // 2. Lock the plan row so two managers cannot delete from it at the same time.
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT 1 FROM Showtime_plans WITH (UPDLOCK, ROWLOCK) " +
                        "WHERE plan_id = ? AND theater_id = ? AND deleted_at IS NULL")) {
                    stmt.setInt(1, planId);
                    stmt.setInt(2, theaterId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("This plan was not found or is already deleted.");
                        }
                    }
                }

                // 3. How many of the picked showtimes really belong to the plan, and how many are still alive?
                int inPlan = 0;
                int live = 0;
                if (!pickedIds.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) AS in_plan, " +
                        "  SUM(CASE WHEN s.deleted_at IS NULL THEN 1 ELSE 0 END) AS live " +
                        "FROM Showtime_plan_items i " +
                        "INNER JOIN Showtimes s ON s.showtime_id = i.showtime_id " +
                        "WHERE i.plan_id = ? AND i.showtime_id IN (" + marks + ")")) {
                    int k = 1;
                    stmt.setInt(k++, planId);
                    for (Integer id : pickedIds) {
                        stmt.setInt(k++, id);
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            inPlan = rs.getInt("in_plan");
                            live = rs.getInt("live");
                        }
                    }
                }
                if (inPlan == 0) {
                    throw new IllegalArgumentException("The selected showtimes do not belong to this plan.");
                }
                }

                // 4. Soft-delete the live, unpaid ones. The conditions are repeated inside the UPDATE,
                //    so a showtime removed or paid a moment ago is simply not touched.
                int removedNow = 0;
                if (live > 0) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE s SET s.deleted_at = GETDATE() " +
                            "FROM Showtimes s " +
                            "INNER JOIN Showtime_plan_items i ON i.showtime_id = s.showtime_id " +
                            "WHERE i.plan_id = ? AND i.showtime_id IN (" + marks + ") " +
                            "AND s.deleted_at IS NULL AND NOT " + PAID_EXISTS)) {
                        int k = 1;
                        stmt.setInt(k++, planId);
                        for (Integer id : pickedIds) {
                            stmt.setInt(k++, id);
                        }
                        removedNow = stmt.executeUpdate();
                    }
                }

                // 5. How many live showtimes stay, then delete the plan itself.
                int remainingLive = 0;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Showtime_plan_items i " +
                        "INNER JOIN Showtimes s ON s.showtime_id = i.showtime_id " +
                        "WHERE i.plan_id = ? AND s.deleted_at IS NULL")) {
                    stmt.setInt(1, planId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            remainingLive = rs.getInt(1);
                        }
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE Showtime_plans SET deleted_at = GETDATE() WHERE plan_id = ?")) {
                    stmt.setInt(1, planId);
                    stmt.executeUpdate();
                }
                conn.commit();

                DeleteResult result = new DeleteResult();
                result.removedNow = removedNow;
                result.alreadyRemoved = inPlan - live;
                result.keptPaid = live - removedNow;
                result.keptByChoice = remainingLive - result.keptPaid;
                return result;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------------- history

    private static final String PLAN_SELECT =
        "SELECT p.*, m.movie_name, u.full_name AS manager_name, " +
        "  (SELECT COUNT(*) FROM Showtime_plan_items i " +
        "   INNER JOIN Showtimes s ON s.showtime_id = i.showtime_id " +
        "   WHERE i.plan_id = p.plan_id AND s.deleted_at IS NULL) AS live_count " +
        "FROM Showtime_plans p " +
        "INNER JOIN Movies m ON m.movie_id = p.movie_id " +
        "INNER JOIN Users u ON u.user_id = p.manager_id ";

    public List<Showtime_plans> getPlansByTheater(int theaterId) throws SQLException {
        List<Showtime_plans> plans = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 PLAN_SELECT + "WHERE p.theater_id = ? ORDER BY p.created_at DESC")) {
            stmt.setInt(1, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    plans.add(mapPlan(rs));
                }
            }
        }
        return plans;
    }

    /** Only returns the plan when it belongs to the given theater. */
    public Showtime_plans getPlan(int planId, int theaterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 PLAN_SELECT + "WHERE p.plan_id = ? AND p.theater_id = ?")) {
            stmt.setInt(1, planId);
            stmt.setInt(2, theaterId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapPlan(rs) : null;
            }
        }
    }

    public List<Showtime_plan_items> getPlanItems(int planId) throws SQLException {
        List<Showtime_plan_items> items = new ArrayList<>();
        String sql =
            "SELECT i.plan_id, i.showtime_id, i.cost, s.room_id, r.room_name, " +
            "       s.start_at, s.end_at, s.deleted_at, " +
            "       CASE WHEN s.deleted_at IS NULL AND " + PAID_EXISTS + " THEN 1 ELSE 0 END AS has_paid " +
            "FROM Showtime_plan_items i " +
            "INNER JOIN Showtimes s ON s.showtime_id = i.showtime_id " +
            "INNER JOIN Rooms r ON r.room_id = s.room_id " +
            "WHERE i.plan_id = ? ORDER BY s.start_at, r.room_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Showtime_plan_items item = new Showtime_plan_items();
                    item.setPlan_id(rs.getInt("plan_id"));
                    item.setShowtime_id(rs.getInt("showtime_id"));
                    item.setCost(rs.getLong("cost"));
                    item.setRoom_id(rs.getInt("room_id"));
                    item.setRoom_name(rs.getString("room_name"));
                    item.setStart_at(rs.getTimestamp("start_at"));
                    item.setEnd_at(rs.getTimestamp("end_at"));
                    item.setDeleted_at(rs.getTimestamp("deleted_at"));
                    item.setHas_paid(rs.getInt("has_paid") == 1);
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Showtime_plans mapPlan(ResultSet rs) throws SQLException {
        Showtime_plans p = new Showtime_plans();
        p.setPlan_id(rs.getInt("plan_id"));
        p.setTheater_id(rs.getInt("theater_id"));
        p.setManager_id(rs.getInt("manager_id"));
        p.setMovie_id(rs.getInt("movie_id"));
        p.setClient_name(rs.getString("client_name"));
        p.setBudget(rs.getLong("budget"));
        p.setBase_price(rs.getInt("base_price"));
        p.setPeriod_start(rs.getDate("period_start"));
        p.setPeriod_end(rs.getDate("period_end"));
        p.setGap_minutes(rs.getInt("gap_minutes"));
        p.setShowtime_count(rs.getInt("showtime_count"));
        p.setTotal_cost(rs.getLong("total_cost"));
        p.setProfit(rs.getLong("profit"));
        p.setCreated_at(rs.getTimestamp("created_at"));
        p.setDeleted_at(rs.getTimestamp("deleted_at"));
        p.setMovie_name(rs.getString("movie_name"));
        p.setManager_name(rs.getString("manager_name"));
        p.setLive_count(rs.getInt("live_count"));
        return p;
    }
}

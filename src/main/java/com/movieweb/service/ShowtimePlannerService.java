package com.movieweb.service;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Pure planning logic (no database, no servlet), so it is easy to test.
 *
 * Money model: a showtime is "bought" at its full-house value,
 *     cost = active seats of the room x original ticket price.
 * The author's budget pays for showtimes; whatever is left over is profit.
 * Change {@link #slotCost(int, int)} if your business rule is different.
 */
public class ShowtimePlannerService {

    public static final int GRID_MINUTES = 5;        // start times are snapped to :00, :05, :10 ...
    public static final int MAX_DAYS = 31;
    public static final int MAX_SLOTS = 3000;        // keeps the preview page usable
    public static final int LEAD_MINUTES = 60;       // a show cannot start sooner than this from now
    public static final int STAGGER_MINUTES = 10;    // neighbouring rooms open 0 / 10 / 20 min apart
    private static final LocalTime PRIME_TIME = LocalTime.of(19, 30);

    // ---------------------------------------------------------------- types

    public static class RoomInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int roomId;
        private final String roomName;
        private final int seatCount;

        public RoomInfo(int roomId, String roomName, int seatCount) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.seatCount = seatCount;
        }
        public int getRoomId() { return roomId; }
        public String getRoomName() { return roomName; }
        public int getSeatCount() { return seatCount; }
    }

    public static class Params implements Serializable {
        private static final long serialVersionUID = 1L;
        public int movieId;
        public String movieName;
        public int durationMinutes;
        public LocalDate movieFrom;        // may be null
        public LocalDate movieUntil;       // null = no end date
        public long budget;
        public int basePrice;
        public LocalDate dateFrom;
        public LocalDate dateTo;
        public int gapMinutes;
        public LocalTime earliest;         // may be null = theater opening time
        public LocalTime latest;           // may be null = theater closing time
        public String clientName;
        public List<Integer> roomIds = new ArrayList<>();
    }

    public static class Slot implements Serializable {
        private static final long serialVersionUID = 1L;
        private int id;                    // position in the final priority order
        private int roomId;
        private String roomName;
        private int seatCount;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private long cost;
        private boolean selected;
        // sort keys, only used while planning
        private int rank;
        private int dayIndex;
        private int roomIndex;

        public int getId() { return id; }
        public int getRoomId() { return roomId; }
        public String getRoomName() { return roomName; }
        public int getSeatCount() { return seatCount; }
        public LocalDateTime getStartAt() { return startAt; }
        public LocalDateTime getEndAt() { return endAt; }
        public long getCost() { return cost; }
        public boolean isSelected() { return selected; }
    }

    public static class Result {
        public List<Slot> slots = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
    }

    // ------------------------------------------------------------- planning

    public static long slotCost(int seatCount, int basePrice) {
        return (long) seatCount * (long) basePrice;
    }

    /**
     * @param rooms        rooms the manager ticked (already filtered to this theater)
     * @param busy         room_id -> existing showtimes [start, end] of that room
     * @param theaterOpen  Theaters.open_time
     * @param theaterClose Theaters.closing_time (<= open means "after midnight")
     */
    public Result generate(Params p,
                           List<RoomInfo> rooms,
                           Map<Integer, List<LocalDateTime[]>> busy,
                           LocalDateTime now,
                           LocalTime theaterOpen,
                           LocalTime theaterClose) {
        Result result = new Result();
        validate(p);

        boolean overnight = !theaterClose.isAfter(theaterOpen);
        LocalTime open = theaterOpen;
        if (p.earliest != null && p.earliest.isAfter(theaterOpen)) {
            open = p.earliest;
        }
        LocalTime close = p.latest != null ? p.latest : theaterClose;
        if (!overnight && close.isAfter(theaterClose)) {
            close = theaterClose;
        }
        if (!overnight && !close.isAfter(open)) {
            throw new IllegalArgumentException("The latest end time must be after the earliest start time.");
        }

        LocalDateTime notBefore = snapUp(now.plusMinutes(LEAD_MINUTES));
        List<Slot> candidates = new ArrayList<>();
        int skippedDays = 0;
        int dayIndex = 0;

        for (LocalDate day = p.dateFrom; !day.isAfter(p.dateTo); day = day.plusDays(1), dayIndex++) {
            if ((p.movieFrom != null && day.isBefore(p.movieFrom))
                    || (p.movieUntil != null && day.isAfter(p.movieUntil))) {
                skippedDays++;
                continue;
            }
            LocalDateTime windowStart = day.atTime(open);
            LocalDateTime windowEnd = day.atTime(close);
            if (!close.isAfter(open)) {
                windowEnd = windowEnd.plusDays(1);
            }
            LocalDateTime primeTime = day.atTime(PRIME_TIME);

            for (int ri = 0; ri < rooms.size(); ri++) {
                RoomInfo room = rooms.get(ri);
                if (room.getSeatCount() <= 0) {
                    continue;
                }
                List<LocalDateTime[]> roomBusy = busy.get(room.getRoomId());
                LocalDateTime cursor = snapUp(windowStart.plusMinutes((ri % 3) * STAGGER_MINUTES));
                if (cursor.isBefore(notBefore)) {
                    cursor = notBefore;
                }
                List<Slot> roomDay = new ArrayList<>();
                while (true) {
                    LocalDateTime start = cursor;
                    LocalDateTime end = start.plusMinutes(p.durationMinutes);
                    if (end.isAfter(windowEnd)) {
                        break;
                    }
                    LocalDateTime blockedUntil = blockedUntil(roomBusy, start, end, p.gapMinutes);
                    if (blockedUntil != null) {
                        cursor = snapUp(blockedUntil.plusMinutes(p.gapMinutes));
                        continue;
                    }
                    Slot s = new Slot();
                    s.roomId = room.getRoomId();
                    s.roomName = room.getRoomName();
                    s.seatCount = room.getSeatCount();
                    s.startAt = start;
                    s.endAt = end;
                    s.cost = slotCost(room.getSeatCount(), p.basePrice);
                    s.dayIndex = dayIndex;
                    s.roomIndex = ri;
                    roomDay.add(s);
                    cursor = snapUp(end.plusMinutes(p.gapMinutes));
                }
                // Within a room-day the shows closest to prime time are the most valuable.
                final LocalDateTime prime = primeTime;
                roomDay.sort(Comparator
                        .comparingLong((Slot s) -> Math.abs(Duration.between(prime, s.startAt).toMinutes()))
                        .thenComparing(s -> s.startAt));
                for (int r = 0; r < roomDay.size(); r++) {
                    roomDay.get(r).rank = r;
                }
                candidates.addAll(roomDay);
            }
        }

        if (skippedDays > 0) {
            result.warnings.add(skippedDays + " day(s) of the period were skipped because the movie is not available on them.");
        }

        // Priority: first the best show of every room on every day, then the second best, ...
        candidates.sort(Comparator
                .comparingInt((Slot s) -> s.rank)
                .thenComparingInt(s -> s.dayIndex)
                .thenComparingInt(s -> s.roomIndex));

        if (candidates.size() > MAX_SLOTS) {
            candidates = new ArrayList<>(candidates.subList(0, MAX_SLOTS));
            result.warnings.add("The period offers more than " + MAX_SLOTS
                    + " possible showtimes. Only the " + MAX_SLOTS + " best are shown; use a shorter period for the rest.");
        }

        // Walk the priority list and keep every slot that still fits the budget.
        // Slots that do not fit are skipped, so cheaper (smaller-room) slots further down
        // can still use up the leftover money.
        long spent = 0;
        List<Slot> selected = new ArrayList<>();
        List<Slot> rest = new ArrayList<>();
        for (Slot s : candidates) {
            if (spent + s.cost <= p.budget) {
                s.selected = true;
                spent += s.cost;
                selected.add(s);
            } else {
                rest.add(s);
            }
        }
        List<Slot> ordered = new ArrayList<>(selected);
        ordered.addAll(rest);
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).id = i;
        }
        result.slots = ordered;

        if (candidates.isEmpty()) {
            result.warnings.add("No free time was found in the chosen rooms and period. "
                    + "Try more rooms, a longer period, or a shorter gap.");
        } else if (selected.isEmpty()) {
            result.warnings.add("The budget is too small to pay for even one showtime.");
        }
        return result;
    }

    // -------------------------------------------------------------- helpers

    private void validate(Params p) {
        if (p.budget <= 0) {
            throw new IllegalArgumentException("Budget must be greater than zero.");
        }
        if (p.basePrice <= 0) {
            throw new IllegalArgumentException("Ticket price must be greater than zero.");
        }
        if (p.durationMinutes <= 0) {
            throw new IllegalArgumentException("The selected movie has an invalid duration.");
        }
        if (p.dateFrom == null || p.dateTo == null || p.dateTo.isBefore(p.dateFrom)) {
            throw new IllegalArgumentException("Please choose a valid period.");
        }
        if (ChronoUnit.DAYS.between(p.dateFrom, p.dateTo) + 1 > MAX_DAYS) {
            throw new IllegalArgumentException("The period can be at most " + MAX_DAYS + " days.");
        }
        if (p.gapMinutes < 0 || p.gapMinutes > 120) {
            throw new IllegalArgumentException("The gap between showtimes must be between 0 and 120 minutes.");
        }
    }

    /** Returns the end of the blocking showtime, or null if [start, end] (plus gap) is free. */
    private LocalDateTime blockedUntil(List<LocalDateTime[]> busy, LocalDateTime start, LocalDateTime end, int gap) {
        if (busy == null) {
            return null;
        }
        LocalDateTime latestBlock = null;
        for (LocalDateTime[] b : busy) {
            boolean overlaps = b[0].isBefore(end.plusMinutes(gap)) && b[1].plusMinutes(gap).isAfter(start);
            if (overlaps && (latestBlock == null || b[1].isAfter(latestBlock))) {
                latestBlock = b[1];
            }
        }
        return latestBlock;
    }

    /** Rounds up to the next multiple of GRID_MINUTES (seconds are dropped upward). */
    static LocalDateTime snapUp(LocalDateTime t) {
        LocalDateTime m = t.truncatedTo(ChronoUnit.MINUTES);
        if (m.isBefore(t)) {
            m = m.plusMinutes(1);
        }
        int rem = m.getMinute() % GRID_MINUTES;
        return rem == 0 ? m : m.plusMinutes(GRID_MINUTES - rem);
    }
}

package com.movieweb.model;

/* System-wide (or single-theater) figures shown on the admin dashboard. */
public class AdminDashboardSummary {
    private int activeTheaters;
    private int activeUsers;
    private int activeManagers;
    /* MANAGER, isActive = 0 and no row in Managers: a newly registered account awaiting approval. */
    private int pendingManagers;
    /* MANAGER, isActive = 0 but with at least one Managers row: a previously approved, now deactivated manager. */
    private int deactivatedManagers;
    private int confirmedBookingsToday;
    private long grossRevenueToday;
    private int confirmedBookings7Days;
    private int ticketsSold7Days;
    private long grossRevenue7Days;
    private int successfulPayments7Days;
    /* Bookings currently in REFUNDED status (refund requested by a manager, not yet reviewed). */
    private int refundedBookings;
    /* Distinct comments with at least one report (system-wide). */
    private int reportedComments;

    public AdminDashboardSummary() {
    }

    public int getActiveTheaters() {
        return activeTheaters;
    }
    public void setActiveTheaters(int v) {
        this.activeTheaters = v;
    }

    public int getActiveUsers() {
        return activeUsers;
    }
    public void setActiveUsers(int v) {
        this.activeUsers = v;
    }

    public int getActiveManagers() {
        return activeManagers;
    }
    public void setActiveManagers(int v) {
        this.activeManagers = v;
    }

    public int getPendingManagers() {
        return pendingManagers;
    }
    public void setPendingManagers(int v) {
        this.pendingManagers = v;
    }

    public int getDeactivatedManagers() {
        return deactivatedManagers;
    }
    public void setDeactivatedManagers(int v) {
        this.deactivatedManagers = v;
    }

    public int getConfirmedBookingsToday() {
        return confirmedBookingsToday;
    }
    public void setConfirmedBookingsToday(int v) {
        this.confirmedBookingsToday = v;
    }

    public long getGrossRevenueToday() {
        return grossRevenueToday;
    }
    public void setGrossRevenueToday(long v) {
        this.grossRevenueToday = v;
    }

    public int getConfirmedBookings7Days() {
        return confirmedBookings7Days;
    }
    public void setConfirmedBookings7Days(int v) {
        this.confirmedBookings7Days = v;
    }

    public int getTicketsSold7Days() {
        return ticketsSold7Days;
    }
    public void setTicketsSold7Days(int v) {
        this.ticketsSold7Days = v;
    }

    public long getGrossRevenue7Days() {
        return grossRevenue7Days;
    }
    public void setGrossRevenue7Days(long v) {
        this.grossRevenue7Days = v;
    }

    public int getSuccessfulPayments7Days() {
        return successfulPayments7Days;
    }
    public void setSuccessfulPayments7Days(int v) {
        this.successfulPayments7Days = v;
    }

    public int getRefundedBookings() {
        return refundedBookings;
    }
    public void setRefundedBookings(int v) {
        this.refundedBookings = v;
    }

    public int getReportedComments() {
        return reportedComments;
    }
    public void setReportedComments(int v) {
        this.reportedComments = v;
    }
}
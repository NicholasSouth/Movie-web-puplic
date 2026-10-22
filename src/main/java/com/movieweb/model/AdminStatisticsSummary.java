package com.movieweb.model;

/* System-wide (or single-theater) report summary.
 * Extends the manager summary with two figures that make the refund limitation visible.
 */
public class AdminStatisticsSummary extends ManagerStatistics {
    /* Bookings created in the period that are currently in REFUNDED status (refund requested). */
    private int refundedBookings;
    /* Gross SUCCESS payments received in the period on bookings that are now REFUNDED. Included in gross revenue. */
    private long revenueOnRefundedBookings;

    public AdminStatisticsSummary() {
    }

    public int getRefundedBookings() {
        return refundedBookings;
    }
    public void setRefundedBookings(int v) {
        this.refundedBookings = v;
    }

    public long getRevenueOnRefundedBookings() {
        return revenueOnRefundedBookings;
    }
    public void setRevenueOnRefundedBookings(long v) {
        this.revenueOnRefundedBookings = v;
    }
}
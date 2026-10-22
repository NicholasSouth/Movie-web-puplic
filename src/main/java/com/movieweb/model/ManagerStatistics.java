package com.movieweb.model;

public class ManagerStatistics {
    private int confirmedBookings;
    private int ticketsSold;
    private long grossRevenue;
    private int successfulPayments;

    public int getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(int confirmedBookings) { this.confirmedBookings = confirmedBookings; }

    public int getTicketsSold() { return ticketsSold; }
    public void setTicketsSold(int ticketsSold) { this.ticketsSold = ticketsSold; }

    public long getGrossRevenue() { return grossRevenue; }
    public void setGrossRevenue(long grossRevenue) { this.grossRevenue = grossRevenue; }

    public int getSuccessfulPayments() { return successfulPayments; }
    public void setSuccessfulPayments(int successfulPayments) { this.successfulPayments = successfulPayments; }

    public long getAveragePaidBookingValue() {
        return successfulPayments <= 0 ? 0 : Math.round((double) grossRevenue / successfulPayments);
    }
}

package com.movieweb.model;

/* One row of the admin "revenue by theater" table. */
public class AdminTheaterStatistic {
    private final int theaterId;
    private final String theaterName;
    private final boolean active;
    private final int confirmedBookings;
    private final int ticketsSold;
    private final long grossRevenue;

    public AdminTheaterStatistic(
            int theaterId, 
            String theaterName, 
            boolean active,
            int confirmedBookings, 
            int ticketsSold, 
            long grossRevenue) {
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.active = active;
        this.confirmedBookings = confirmedBookings;
        this.ticketsSold = ticketsSold;
        this.grossRevenue = grossRevenue;
    }

    public int getTheaterId() {
        return theaterId;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public boolean isActive() {
        return active;
    }

    public int getConfirmedBookings() {
        return confirmedBookings;
    }

    public int getTicketsSold() {
        return ticketsSold;
    }

    public long getGrossRevenue() {
        return grossRevenue;
    }
}
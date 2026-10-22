package com.movieweb.model;

import java.time.LocalDate;

public class ManagerDailyStatistic {
    private LocalDate date;
    private long revenue;
    private int bookings;

    public ManagerDailyStatistic() {
    }

    public ManagerDailyStatistic(LocalDate date, long revenue, int bookings) {
        this.date = date;
        this.revenue = revenue;
        this.bookings = bookings;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public long getRevenue() { return revenue; }
    public void setRevenue(long revenue) { this.revenue = revenue; }

    public int getBookings() { return bookings; }
    public void setBookings(int bookings) { this.bookings = bookings; }
}

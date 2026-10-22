package com.movieweb.model;

import java.sql.Timestamp;

/* One refund request (or decided refund) on the admin refunds page. Read-only view of booking + payment data. */
public class AdminRefundRow {
    private int bookingId;
    private Timestamp bookAt;
    private String bookingStatus;
    private int bookingPrice;
    private String customerName;
    private String customerEmail;
    private String movieName;
    private Timestamp showtimeStart;
    private String theaterName;
    private String roomName;
    private int ticketCount;
    private String paymentStatus;
    private String paymentMethod;
    private int paymentAmount;
    private String transactionCode;

    public AdminRefundRow() {
    }

    public int getBookingId() {
        return bookingId;
    }
    public void setBookingId(int v) {
        this.bookingId = v;
    }

    public Timestamp getBookAt() {
        return bookAt;
    }
    public void setBookAt(Timestamp v) {
        this.bookAt = v;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }
    public void setBookingStatus(String v) {
        this.bookingStatus = v;
    }

    public int getBookingPrice() {
        return bookingPrice;
    }
    public void setBookingPrice(int v) {
        this.bookingPrice = v;
    }

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String v) {
        this.customerName = v;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
    public void setCustomerEmail(String v) {
        this.customerEmail = v;
    }

    public String getMovieName() {
        return movieName;
    }
    public void setMovieName(String v) {
        this.movieName = v;
    }

    public Timestamp getShowtimeStart() {
        return showtimeStart;
    }
    public void setShowtimeStart(Timestamp v) {
        this.showtimeStart = v;
    }

    public String getTheaterName() {
        return theaterName;
    }
    public void setTheaterName(String v) {
        this.theaterName = v;
    }

    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String v) {
        this.roomName = v;
    }

    public int getTicketCount() {
        return ticketCount;
    }
    public void setTicketCount(int v) {
        this.ticketCount = v;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String v) {
        this.paymentStatus = v;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String v) {
        this.paymentMethod = v;
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }
    public void setPaymentAmount(int v) {
        this.paymentAmount = v;
    }

    public String getTransactionCode() {
        return transactionCode;
    }
    public void setTransactionCode(String v) {
        this.transactionCode = v;
    }
}
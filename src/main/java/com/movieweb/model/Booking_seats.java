package com.movieweb.model;

public class Booking_seats {
	private int booking_id;
    private int seat_id;
    private int ticket_type_id;
    private int final_price;
    public Booking_seats() {
    }
    public int getBooking_id() {
        return booking_id;
    }
    public void setBooking_id(int booking_id) {
        this.booking_id = booking_id;
    }
    public int getSeat_id() {
        return seat_id;
    }
    public void setSeat_id(int seat_id) {
        this.seat_id = seat_id;
    }
    public int getTicket_type_id() {
        return ticket_type_id;
    }
    public void setTicket_type_id(int ticket_type_id) {
        this.ticket_type_id = ticket_type_id;
    }
    public int getFinal_price() {
        return final_price;
    }
    public void setFinal_price(int final_price) {
        this.final_price = final_price;
    }
}

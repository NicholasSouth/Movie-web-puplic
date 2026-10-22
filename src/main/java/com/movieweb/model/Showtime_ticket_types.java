package com.movieweb.model;

public class Showtime_ticket_types {
	private int showtime_id;
    private int ticket_type_id;
    private int price;
    //
    public Showtime_ticket_types() {

    }
    public int getShowtime_id() {
        return showtime_id;
    }
    public void setShowtime_id(int showtime_id) {
        this.showtime_id = showtime_id;
    }
    public int getTicket_type_id() {
        return ticket_type_id;
    }
    public void setTicket_type_id(int ticket_type_id) {
        this.ticket_type_id = ticket_type_id;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
}

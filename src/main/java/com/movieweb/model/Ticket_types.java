package com.movieweb.model;

public class Ticket_types {
	private int ticket_type_id;
    private String ticket_type_name;
    private String price_modify;
    //
    public Ticket_types() {
    }
    public int getTicket_type_id() {
        return ticket_type_id;
    }
    public void setTicket_type_id(int ticket_type_id) {
        this.ticket_type_id = ticket_type_id;
    }
    public String getTicket_type_name() {
        return ticket_type_name;
    }
    public void setTicket_type_name(String ticket_type_name) {
        this.ticket_type_name = ticket_type_name;
    }
    public String getPrice_modify() {
        return price_modify;
    }
    public void setPrice_modify(String price_modify) {
        this.price_modify = price_modify;
    }
}

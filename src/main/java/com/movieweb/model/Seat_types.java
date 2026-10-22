package com.movieweb.model;

public class Seat_types {
	private int seat_type_id;
    private String seat_type_name;
    private String price_modify;
    //
    public Seat_types() {
    }
    public int getSeat_type_id() {
        return seat_type_id;
    }
    public void setSeat_type_id(int seat_type_id) {
        this.seat_type_id = seat_type_id;
    }
    public String getSeat_type_name() {
        return seat_type_name;
    }
    public void setSeat_type_name(String seat_type_name) {
        this.seat_type_name = seat_type_name;
    }
    public String getPrice_modify() {
        return price_modify;
    }
    public void setPrice_modify(String price_modify) {
        this.price_modify = price_modify;
    }
}

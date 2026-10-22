package com.movieweb.model;

import java.sql.Timestamp;

public class Seats {
	private int seat_id;
    private int room_id;
    private String seat_row;
    private int seat_col;
    private int seat_type_id;
    private boolean isActive;
    private boolean booked;
    private String seat_type_name;
    private String seat_price_modify;
    private int price;
    private Timestamp deleted_at;
    //
    public Seats() {
    	
    }
    public int getSeat_id() {
        return seat_id;
    }
    public void setSeat_id(int seat_id) {
        this.seat_id = seat_id;
    }
    public int getRoom_id() {
        return room_id;
    }
    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }
    public String getSeat_row() {
        return seat_row;
    }
    public void setSeat_row(String seat_row) {
        this.seat_row = seat_row;
    }
    public int getSeat_col() {
        return seat_col;
    }
    public void setSeat_col(int seat_col) {
        this.seat_col = seat_col;
    }
    public int getSeat_type_id() {
    	return seat_type_id;
    }
    public void setSeat_type_id(int seat_type_id) {
        this.seat_type_id = seat_type_id;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public boolean isBooked() {return booked;}
    public void setBooked(boolean booked) {this.booked = booked;}
    public String getSeat_type_name() { return seat_type_name; }
    public void setSeat_type_name(String seat_type_name) { this.seat_type_name = seat_type_name; }
    public String getSeat_price_modify() { return seat_price_modify; }
    public void setSeat_price_modify(String seat_price_modify) { this.seat_price_modify = seat_price_modify; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public void setDeleted_at(Timestamp deleted_at) {
        this.deleted_at = deleted_at;
    }
    public Timestamp getDeleted_at() {
        return deleted_at;
    }
}
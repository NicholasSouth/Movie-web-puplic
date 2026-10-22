package com.movieweb.model;

public class Room_types {
	private int room_type_id;
    private String room_type_name;
    private String price_modify;
    //
    public Room_types() {
    }
    public int getRoom_type_id() {
        return room_type_id;
    }
    public void setRoom_type_id(int room_type_id) {
        this.room_type_id = room_type_id;
    }
    public String getRoom_type_name() {
        return room_type_name;
    }
    public void setRoom_type_name(String room_type_name) {
        this.room_type_name = room_type_name;
    }
    public String getPrice_modify() {
        return price_modify;
    }
    public void setPrice_modify(String price_modify) {
        this.price_modify = price_modify;
    }
}

package com.movieweb.model;

import java.sql.Timestamp;

public class Rooms {
    private int room_id;
    private int theater_id;
    private String room_name;
    private int room_type_id;
    private String room_type_name;
    private boolean isActive;
    private Room_types roomType;
    private Timestamp deleted_at;

    public Rooms() {
    }
    public int getRoom_id() {
        return room_id;
    }
    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }
    public int getTheater_id() {
        return theater_id;
    }
    public void setTheater_id(int theater_id) {
        this.theater_id = theater_id;
    }
    public String getRoom_name() {
        return room_name;
    }
    public void setRoom_name(String room_name) {
        this.room_name = room_name;
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
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public Room_types getRoomType() {
        return roomType;
    }
    public void setRoomType(Room_types roomType) {
        this.roomType = roomType;
    }
    public void setDeleted_at(Timestamp deleted_at) {
        this.deleted_at = deleted_at;
    }
    public Timestamp getDeleted_at() {
        return deleted_at;
    }
}
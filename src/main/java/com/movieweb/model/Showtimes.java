package com.movieweb.model;

import java.sql.Timestamp;

public class Showtimes 
{
    private int showtime_id;
    private int room_id;
    private int movie_id;
    private Timestamp start_at;
    private Timestamp end_at;
    private String status;
    private Timestamp deleted_at;
    private String room_name;
    private String room_type_name;

    public Showtimes() {
    }
    public int getShowtime_id() {
        return showtime_id;
    }
    public void setShowtime_id(int showtime_id) {
        this.showtime_id = showtime_id;
    }
    public int getRoom_id() {
        return room_id;
    }
    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }
    public int getMovie_id() {
        return movie_id;
    }
    public void setMovie_id(int movie_id) {
        this.movie_id = movie_id;
    }
    public Timestamp getStart_at() {
        return start_at;
    }
    public void setStart_at(Timestamp start_at) {
        this.start_at = start_at;
    }
    public Timestamp getEnd_at() {
        return end_at;
    }
    public void setEnd_at(Timestamp end_at) {
        this.end_at = end_at;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setDeleted_at(Timestamp deleted_at) {
        this.deleted_at = deleted_at;
    }
    public Timestamp getDeleted_at() {
        return deleted_at;
    }
    public String getRoom_name() {
        return room_name;
    }
    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }
    public String getRoom_type_name() {
        return room_type_name;
    }
    public void setRoom_type_name(String room_type_name) {
        this.room_type_name = room_type_name;
    }
}
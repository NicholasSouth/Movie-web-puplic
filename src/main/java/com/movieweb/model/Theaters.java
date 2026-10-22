package com.movieweb.model;

import java.sql.Time;
import java.sql.Timestamp;

public class Theaters {
    private int theater_id;
    private String theater_name;
    private String theater_address;
    private String theater_image_path;
    private String description;
    private double latitude;
    private double longtitude;
    private Time open_time;
    private Time closing_time;
    private boolean isActive;
    private Timestamp deleted_at;

    public Theaters() {
    }
    
    public int getTheater_id() {
        return theater_id;
    }
    public void setTheater_id(int theater_id) {
        this.theater_id = theater_id;
    }
    public String getTheater_name() {
        return theater_name;
    }
    public void setTheater_name(String theater_name) {
        this.theater_name = theater_name;
    }
    public String getTheater_address() {
        return theater_address;
    }
    public void setTheater_address(String theater_address) {
        this.theater_address = theater_address;
    }
    public String getTheater_image_path() {
        return theater_image_path;
    }
    public void setTheater_image_path(String theater_image_path) {
        this.theater_image_path = theater_image_path;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongtitude() {
        return longtitude;
    }
    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }
    public Time getOpen_time() {
        return open_time;
    }
    public void setOpen_time(Time open_time) {
        this.open_time = open_time;
    }
    public Time getClosing_time() {
        return closing_time;
    }
    public void setClosing_time(Time closing_time) {
        this.closing_time = closing_time;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public Timestamp getDeleted_at() {
        return deleted_at;
    }
    public void setDeleted_at(Timestamp deleted_at) {
        this.deleted_at = deleted_at;
    }
}
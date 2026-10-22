package com.movieweb.model;

import java.sql.Time;

/* One theater on the admin Theaters page, with headline counts. */
public class AdminTheaterRow {
    private int theaterId;
    private String theaterName;
    private String address;
    private boolean active;
    private Time openTime;
    private Time closingTime;
    private int roomCount;          // rooms not soft-deleted
    private int activeRoomCount;    // of those, isActive = 1
    private int managerCount;       // rows in Managers
    private int upcomingShowtimes;  // showtimes starting after now, not soft-deleted

    public AdminTheaterRow() {
    }

    public int getTheaterId() {
        return theaterId;
    }
    public void setTheaterId(int v) {
        this.theaterId = v;
    }

    public String getTheaterName() {
        return theaterName;
    }
    public void setTheaterName(String v) {
        this.theaterName = v;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String v) {
        this.address = v;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean v) {
        this.active = v;
    }

    public Time getOpenTime() {
        return openTime;
    }
    public void setOpenTime(Time v) {
        this.openTime = v;
    }

    public Time getClosingTime() {
        return closingTime;
    }
    public void setClosingTime(Time v) {
        this.closingTime = v;
    }

    public int getRoomCount() {
        return roomCount;
    }
    public void setRoomCount(int v) {
        this.roomCount = v;
    }

    public int getActiveRoomCount() {
        return activeRoomCount;
    }
    public void setActiveRoomCount(int v) {
        this.activeRoomCount = v;
    }

    public int getManagerCount() {
        return managerCount;
    }
    public void setManagerCount(int v) {
        this.managerCount = v;
    }

    public int getUpcomingShowtimes() {
        return upcomingShowtimes;
    }
    public void setUpcomingShowtimes(int v) {
        this.upcomingShowtimes = v;
    }
}
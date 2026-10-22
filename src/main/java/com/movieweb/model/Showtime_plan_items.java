package com.movieweb.model;

import java.sql.Timestamp;

public class Showtime_plan_items {
    private int plan_id;
    private int showtime_id;
    private long cost;
    // Joined for display
    private int room_id;
    private String room_name;
    private Timestamp start_at;
    private Timestamp end_at;
    private Timestamp deleted_at;   // not null = the showtime was removed later
    private boolean has_paid;       // live showtime with a successful payment: cannot be deleted

    public Showtime_plan_items() {}

    public int getPlan_id() { return plan_id; }
    public void setPlan_id(int v) { this.plan_id = v; }
    public int getShowtime_id() { return showtime_id; }
    public void setShowtime_id(int v) { this.showtime_id = v; }
    public long getCost() { return cost; }
    public void setCost(long v) { this.cost = v; }
    public int getRoom_id() { return room_id; }
    public void setRoom_id(int v) { this.room_id = v; }
    public String getRoom_name() { return room_name; }
    public void setRoom_name(String v) { this.room_name = v; }
    public Timestamp getStart_at() { return start_at; }
    public void setStart_at(Timestamp v) { this.start_at = v; }
    public Timestamp getEnd_at() { return end_at; }
    public void setEnd_at(Timestamp v) { this.end_at = v; }
    public boolean isHas_paid() { return has_paid; }
    public void setHas_paid(boolean v) { this.has_paid = v; }
    public Timestamp getDeleted_at() { return deleted_at; }
    public void setDeleted_at(Timestamp v) { this.deleted_at = v; }
}

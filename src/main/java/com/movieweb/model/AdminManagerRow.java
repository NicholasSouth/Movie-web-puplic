package com.movieweb.model;

import java.sql.Timestamp;

/* One manager account as listed on the admin Managers page. */
public class AdminManagerRow {
    private int userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private boolean active;
    private Timestamp createdAt;
    private int theaterCount;

    public AdminManagerRow() {
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int v) {
        this.userId = v;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String v) {
        this.username = v;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String v) {
        this.fullName = v;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String v) {
        this.email = v;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String v) {
        this.phone = v;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean v) {
        this.active = v;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp v) {
        this.createdAt = v;
    }

    public int getTheaterCount() {
        return theaterCount;
    }
    public void setTheaterCount(int v) {
        this.theaterCount = v;
    }

    /* pending = inactive and never assigned; deactivated = inactive but has assignments. */
    public String getState() {
        if (active) {
            return "ACTIVE";
        }
        return theaterCount == 0 ? "PENDING" : "DEACTIVATED";
    }
}
package com.movieweb.model;

import java.sql.Timestamp;

/* One USER account on the admin Users page, with activity counts. */
public class AdminUserRow {
    private int userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp deletedAt;
    private int commentCount;
    private int bookingCount;
    private int reportsReceived; // reports filed against this user's comments
    private int reportsFiled;    // reports this user filed

    public AdminUserRow() {
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

    public Timestamp getDeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(Timestamp v) {
        this.deletedAt = v;
    }

    public int getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(int v) {
        this.commentCount = v;
    }

    public int getBookingCount() {
        return bookingCount;
    }
    public void setBookingCount(int v) {
        this.bookingCount = v;
    }

    public int getReportsReceived() {
        return reportsReceived;
    }
    public void setReportsReceived(int v) {
        this.reportsReceived = v;
    }

    public int getReportsFiled() {
        return reportsFiled;
    }
    public void setReportsFiled(int v) {
        this.reportsFiled = v;
    }

    /* ACTIVE, BLOCKED (isActive = 0 by an admin) or DELETED (closed by the user; deleted_at set). */
    public String getState() {
        if (deletedAt != null) {
            return "DELETED";
        }
        return active ? "ACTIVE" : "BLOCKED";
    }
}
package com.movieweb.model;

import java.sql.Timestamp;

/* One manager theater request in the admin review list. */
public class AdminTheaterRequestRow {
    private int requestId;
    private int userId;
    private String username;
    private String fullName;
    private String email;
    private int theaterId;
    private String theaterName;
    private String requestType; // ASSIGN or REMOVE
    private String status;      // PENDING, APPROVED, REJECTED, CANCELLED
    private String note;
    private Timestamp createdAt;
    private Timestamp reviewedAt;
    private String reviewerName;
    private String reviewNote;

    public AdminTheaterRequestRow() {
    }

    public int getRequestId() {
        return requestId;
    }
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public int getTheaterId() {
        return theaterId;
    }
    public void setTheaterId(int theaterId) {
        this.theaterId = theaterId;
    }

    public String getTheaterName() {
        return theaterName;
    }
    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getRequestType() {
        return requestType;
    }
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getReviewedAt() {
        return reviewedAt;
    }
    public void setReviewedAt(Timestamp reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewerName() {
        return reviewerName;
    }
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewNote() {
        return reviewNote;
    }
    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
package com.movieweb.model;

import java.sql.Timestamp;

/* A manager's assign/remove request, as shown in the request history. */
public class ManagerTheaterRequest {
    private int requestId;
    private int theaterId;
    private String theaterName;
    private String requestType; // ASSIGN or REMOVE
    private String status;      // PENDING, APPROVED, REJECTED, CANCELLED
    private String note;
    private Timestamp createdAt;
    private Timestamp reviewedAt;
    private String reviewNote;

    public ManagerTheaterRequest() {
    }

    public int getRequestId() {
        return requestId;
    }
    public void setRequestId(int requestId) {
        this.requestId = requestId;
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

    public String getReviewNote() {
        return reviewNote;
    }
    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
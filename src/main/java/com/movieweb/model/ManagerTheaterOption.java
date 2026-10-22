package com.movieweb.model;

/* One theater in the request page: whether the manager is assigned and any pending request. */
public class ManagerTheaterOption {
    private int theaterId;
    private String theaterName;
    private String theaterAddress;
    private boolean active;
    private boolean assigned;
    private String pendingType; // "ASSIGN", "REMOVE" or null

    public ManagerTheaterOption() {
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

    public String getTheaterAddress() {
        return theaterAddress;
    }
    public void setTheaterAddress(String theaterAddress) {
        this.theaterAddress = theaterAddress;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAssigned() {
        return assigned;
    }
    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public String getPendingType() {
        return pendingType;
    }
    public void setPendingType(String pendingType) {
        this.pendingType = pendingType;
    }
}
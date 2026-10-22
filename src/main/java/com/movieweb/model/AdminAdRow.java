package com.movieweb.model;

import java.sql.Timestamp;

/* One advertisement in the admin list. Dates are real timestamps (the user-side Ads model keeps them as strings). */
public class AdminAdRow {
    private int adId;
    private String name;
    private String imagePath;
    private String link;
    private Timestamp startAt; // null = no start limit
    private Timestamp endAt;   // null = no end limit
    private boolean active;

    public AdminAdRow() {
    }

    public int getAdId() {
        return adId;
    }
    public void setAdId(int adId) {
        this.adId = adId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }

    public Timestamp getStartAt() {
        return startAt;
    }
    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }
    public void setEndAt(Timestamp endAt) {
        this.endAt = endAt;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    /* LIVE (shown to customers now), SCHEDULED, EXPIRED or DISABLED. Same rule as AdsDAO.getActiveAdvertisements. */
    public String getState() {
        if (!active) {
            return "DISABLED";
        }
        long now = System.currentTimeMillis();
        if (startAt != null && startAt.getTime() > now) {
            return "SCHEDULED";
        }
        if (endAt != null && endAt.getTime() < now) {
            return "EXPIRED";
        }
        return "LIVE";
    }
}
package com.movieweb.model;

import java.sql.Timestamp;

public class Promotions {
    private int promotion_id;
    private String promotion_code;
    private String description;
    private String price_modify;
    private Timestamp start_at;
    private Timestamp end_at;
    private int usage_limit;
    private boolean isActive;

    public Promotions() {
    }
    public int getPromotion_id() {
        return promotion_id;
    }
    public void setPromotion_id(int promotion_id) {
        this.promotion_id = promotion_id;
    }
    public String getPromotion_code() {
        return promotion_code;
    }
    public void setPromotion_code(String promotion_code) {
        this.promotion_code = promotion_code;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getPrice_modify() {
        return price_modify;
    }
    public void setPrice_modify(String price_modify) {
        this.price_modify = price_modify;
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
    public int getUsage_limit() {
        return usage_limit;
    }
    public void setUsage_limit(int usage_limit) {
        this.usage_limit = usage_limit;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}

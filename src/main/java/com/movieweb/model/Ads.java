package com.movieweb.model;

public class Ads{
    private int ad_id;
    private String ad_name;
    private String image_path;
    private String link;
    private String start_at;
    private String end_at;
    private boolean isActive;
    private String deleted_at;
    //
    public Ads() {
    }
    public int getAd_id() {
        return ad_id;
    }
    public void setAd_id(int ad_id) {
        this.ad_id = ad_id;
    }
    public String getAd_name() {
        return ad_name;
    }
    public void setAd_name(String ad_name) {
        this.ad_name = ad_name;
    }
    public String getImage_path() {
        return image_path;
    }
    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public String getStart_at() {
        return start_at;
    }
    public void setStart_at(String start_at) {
        this.start_at = start_at;
    }
    public String getEnd_at() {
        return end_at;
    }
    public void setEnd_at(String end_at) {
        this.end_at = end_at;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public String getDeleted_at() {
        return deleted_at;
    }
    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }
}

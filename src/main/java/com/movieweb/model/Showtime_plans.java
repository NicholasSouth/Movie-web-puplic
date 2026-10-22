package com.movieweb.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Showtime_plans {
    private int plan_id;
    private int theater_id;
    private int manager_id;
    private int movie_id;
    private String client_name;
    private long budget;
    private int base_price;
    private Date period_start;
    private Date period_end;
    private int gap_minutes;
    private int showtime_count;
    private long total_cost;
    private long profit;
    private Timestamp created_at;
    private Timestamp deleted_at;   // not null = plan deleted together with its showtimes
    // Joined for display
    private String movie_name;
    private String manager_name;
    private int live_count;      // showtimes of this plan that are not soft-deleted

    public Showtime_plans() {}

    public int getPlan_id() { return plan_id; }
    public void setPlan_id(int v) { this.plan_id = v; }
    public int getTheater_id() { return theater_id; }
    public void setTheater_id(int v) { this.theater_id = v; }
    public int getManager_id() { return manager_id; }
    public void setManager_id(int v) { this.manager_id = v; }
    public int getMovie_id() { return movie_id; }
    public void setMovie_id(int v) { this.movie_id = v; }
    public String getClient_name() { return client_name; }
    public void setClient_name(String v) { this.client_name = v; }
    public long getBudget() { return budget; }
    public void setBudget(long v) { this.budget = v; }
    public int getBase_price() { return base_price; }
    public void setBase_price(int v) { this.base_price = v; }
    public Date getPeriod_start() { return period_start; }
    public void setPeriod_start(Date v) { this.period_start = v; }
    public Date getPeriod_end() { return period_end; }
    public void setPeriod_end(Date v) { this.period_end = v; }
    public int getGap_minutes() { return gap_minutes; }
    public void setGap_minutes(int v) { this.gap_minutes = v; }
    public int getShowtime_count() { return showtime_count; }
    public void setShowtime_count(int v) { this.showtime_count = v; }
    public long getTotal_cost() { return total_cost; }
    public void setTotal_cost(long v) { this.total_cost = v; }
    public long getProfit() { return profit; }
    public void setProfit(long v) { this.profit = v; }
    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp v) { this.created_at = v; }
    public Timestamp getDeleted_at() { return deleted_at; }
    public void setDeleted_at(Timestamp v) { this.deleted_at = v; }
    public String getMovie_name() { return movie_name; }
    public void setMovie_name(String v) { this.movie_name = v; }
    public String getManager_name() { return manager_name; }
    public void setManager_name(String v) { this.manager_name = v; }
    public int getLive_count() { return live_count; }
    public void setLive_count(int v) { this.live_count = v; }
}

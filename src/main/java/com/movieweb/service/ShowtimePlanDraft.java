package com.movieweb.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A generated-but-not-saved plan. It lives in the HTTP session until the
 * manager confirms or discards it, so the browser only ever sends back slot
 * ids (never times, rooms or prices).
 */
public class ShowtimePlanDraft implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private int userId;
    private int theaterId;
    private ShowtimePlannerService.Params params;
    private List<ShowtimePlannerService.Slot> slots = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getTheaterId() { return theaterId; }
    public void setTheaterId(int theaterId) { this.theaterId = theaterId; }
    public ShowtimePlannerService.Params getParams() { return params; }
    public void setParams(ShowtimePlannerService.Params params) { this.params = params; }
    public List<ShowtimePlannerService.Slot> getSlots() { return slots; }
    public void setSlots(List<ShowtimePlannerService.Slot> slots) { this.slots = slots; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public int getAutoSelectedCount() {
        int n = 0;
        for (ShowtimePlannerService.Slot s : slots) {
            if (s.isSelected()) n++;
        }
        return n;
    }
}

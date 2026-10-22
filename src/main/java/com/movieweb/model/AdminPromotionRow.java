package com.movieweb.model;

import java.sql.Timestamp;

/* One promotion as shown on the admin promotions page: the Promotions columns plus how many bookings
 * currently use it. Usage is counted exactly like PromotionsDAO.getPromotionUsageCount
 * (bookings that are not deleted and not CANCELLED), so the number matches what the booking flow enforces.
 */
public class AdminPromotionRow {
    private int promotionId;
    private String code;
    private String description;
    private String priceModify;
    private Timestamp startAt;
    private Timestamp endAt;
    /* 0 when the usage_limit column is NULL; PromotionCodeValidate treats such a code as unusable. */
    private int usageLimit;
    private boolean active;
    private int usedCount;

    public AdminPromotionRow() {
    }

    public int getPromotionId() {
        return promotionId;
    }
    public void setPromotionId(int v) {
        this.promotionId = v;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String v) {
        this.code = v;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String v) {
        this.description = v;
    }

    public String getPriceModify() {
        return priceModify;
    }
    public void setPriceModify(String v) {
        this.priceModify = v;
    }

    public Timestamp getStartAt() {
        return startAt;
    }
    public void setStartAt(Timestamp v) {
        this.startAt = v;
    }

    public Timestamp getEndAt() {
        return endAt;
    }
    public void setEndAt(Timestamp v) {
        this.endAt = v;
    }

    public int getUsageLimit() {
        return usageLimit;
    }
    public void setUsageLimit(int v) {
        this.usageLimit = v;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean v) {
        this.active = v;
    }

    public int getUsedCount() {
        return usedCount;
    }
    public void setUsedCount(int v) {
        this.usedCount = v;
    }

    /* What a customer would experience right now, using the same checks as PromotionCodeValidate:
     * DISABLED, SCHEDULED, EXPIRED, NO LIMIT (usage_limit missing), EXHAUSTED or LIVE.
     */
    public String getState() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (!active) {
            return "DISABLED";
        }
        if (startAt == null || endAt == null) {
            return "EXPIRED";
        }
        if (now.before(startAt)) {
            return "SCHEDULED";
        }
        if (now.after(endAt)) {
            return "EXPIRED";
        }
        if (usageLimit <= 0) {
            return "NO LIMIT";
        }
        if (usedCount >= usageLimit) {
            return "EXHAUSTED";
        }
        return "LIVE";
    }
}
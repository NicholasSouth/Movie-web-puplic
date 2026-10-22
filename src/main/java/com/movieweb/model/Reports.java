package com.movieweb.model;

import java.sql.Timestamp;

public class Reports {

    private int report_id;
    private int user_id;
    private int comment_id;
    private Timestamp created_at;

    //
    public Reports() {
    }

    public Reports(
            int report_id,
            int user_id,
            int comment_id,
            Timestamp created_at) {
        this.report_id = report_id;
        this.user_id = user_id;
        this.comment_id = comment_id;
        this.created_at = created_at;
    }
    public int getReportId() {
        return report_id;
    }
    public void setReportId(int report_id) {
        this.report_id = report_id;
    }
    public int getUserId() {
        return user_id;
    }
    public void setUserId(int user_id) {
        this.user_id = user_id;
    }
    public int getCommentId() {
        return comment_id;
    }
    public void setCommentId(int comment_id) {
        this.comment_id = comment_id;
    }
    public Timestamp getCreatedAt() {
        return created_at;
    }
    public void setCreatedAt(Timestamp created_at) {
        this.created_at = created_at;
    }
}
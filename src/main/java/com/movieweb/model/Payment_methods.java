package com.movieweb.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Payment_methods {
	private int payment_method_id;
	private int user_id;
	private String method;
	private String card_number;
	private Date expired_date;
	private Timestamp created_at;
	private boolean isActive;
	
	//
	public Payment_methods() {
	}
	public int getPayment_method_id() {
	    return payment_method_id;
	}
	public void setPayment_method_id(int payment_method_id) {
	    this.payment_method_id = payment_method_id;
	}
	public int getUser_id() {
	    return user_id;
	}
	public void setUser_id(int user_id) {
	    this.user_id = user_id;
	}
	public String getMethod() {
	    return method;
	}
	public void setMethod(String method) {
	    this.method = method;
	}
	public String getCard_number() {
	    return card_number;
	}
	public void setCard_number(String card_number) {
	    this.card_number = card_number;
	}
	public Date getExpired_date() {
	    return expired_date;
	}
	public void setExpired_date(Date expired_date) {
	    this.expired_date = expired_date;
	}
	public Timestamp getCreated_at() {
	    return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
	    this.created_at = created_at;
	}
	public boolean isActive() {
	    return isActive;
	}
	public void setActive(boolean isActive) {
	    this.isActive = isActive;
	}
}
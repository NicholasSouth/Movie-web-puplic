package com.movieweb.model;

public class Payments {
	private int payment_id;
    private int payment_method_id;
    private int booking_id;
    private String method;
    private int amount;
    private String paid_at;
    private String status;
    private String transaction_code;
    //
    public Payments() {
    }
    public int getPayment_id() {
        return payment_id;
    }
    public void setPayment_id(int payment_id) {
        this.payment_id = payment_id;
    }
    public int getPayment_method_id() {
        return payment_method_id;
    }
    public void setPayment_method_id(int payment_method_id) {
        this.payment_method_id = payment_method_id;
    }
    public int getBooking_id() {
        return booking_id;
    }
    public void setBooking_id(int booking_id) {
        this.booking_id = booking_id;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public String getPaid_at() {
        return paid_at;
    }
    public void setPaid_at(String paid_at) {
        this.paid_at = paid_at;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getTransaction_code() {
        return transaction_code;
    }
    public void setTransaction_code(String transaction_code) {
        this.transaction_code = transaction_code;
    }
}

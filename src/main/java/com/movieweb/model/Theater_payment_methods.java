package com.movieweb.model;

public class Theater_payment_methods {
    private int payment_method_id;
    private int theater_id;

    public Theater_payment_methods() {
    }
    
    public Theater_payment_methods(int payment_method_id, int theater_id) {
        this.payment_method_id = payment_method_id;
        this.theater_id = theater_id;
    }
    public int getPayment_method_id() {
        return payment_method_id;
    }
    public void setPayment_method_id(int payment_method_id) {
        this.payment_method_id = payment_method_id;
    }
    public int getTheater_id() {
        return theater_id;
    }
    public void setTheater_id(int theater_id) {
        this.theater_id = theater_id;
    }
}
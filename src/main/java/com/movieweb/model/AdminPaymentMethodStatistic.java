package com.movieweb.model;

/* Successful payments grouped by Payments.method. */
public class AdminPaymentMethodStatistic {
    private final String method;
    private final int payments;
    private final long revenue;

    public AdminPaymentMethodStatistic(String method, int payments, long revenue) {
        this.method = method;
        this.payments = payments;
        this.revenue = revenue;
    }

    public String getMethod() {
        return method;
    }

    public int getPayments() {
        return payments;
    }

    public long getRevenue() {
        return revenue;
    }
}
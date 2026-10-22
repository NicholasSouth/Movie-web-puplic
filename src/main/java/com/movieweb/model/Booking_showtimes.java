package com.movieweb.model;

public class Booking_showtimes {
	private int booking_id;
    private int user_id;
    private int showtime_id;
    private int promotion_id;
    private String book_at;
    private String status;
    private int price;
    private String delete_at;
    public Booking_showtimes() {
    }
    public int getBooking_id() {
        return booking_id;
    }
    public void setBooking_id(int booking_id) {
        this.booking_id = booking_id;
    }
    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    public int getShowtime_id() {
        return showtime_id;
    }
    public void setShowtime_id(int showtime_id) {
        this.showtime_id = showtime_id;
    }
    public int getPromotion_id() {
        return promotion_id;
    }
    public void setPromotion_id(int promotion_id) {
        this.promotion_id = promotion_id;
    }
    public String getBook_at() {
        return book_at;
    }
    public void setBook_at(String book_at) {
        this.book_at = book_at;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public String getDelete_at() {
        return delete_at;
    }
    public void setDelete_at(String delete_at) {
        this.delete_at = delete_at;
    }
}

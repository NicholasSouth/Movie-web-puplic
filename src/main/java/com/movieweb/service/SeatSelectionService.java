package com.movieweb.service;

import java.sql.Timestamp;
import java.util.List;

import com.movieweb.DAO.SeatsDAO;
import com.movieweb.DAO.ShowtimesDAO;
import com.movieweb.model.Seats;
import com.movieweb.model.Showtimes;

public class SeatSelectionService {
    private final ShowtimesDAO showtimesDAO = new ShowtimesDAO();
    private final SeatsDAO seatsDAO = new SeatsDAO();
    private final PricingService pricingService = new PricingService();

    // Returns the showtime only if it exists, is SCHEDULED and has not started yet
    public Showtimes getBookableShowtime(int showtimeId) {
        Showtimes showtime = showtimesDAO.getShowtimeById(showtimeId);
        if (showtime == null) {
            return null;
        }
        if (!"SCHEDULED".equalsIgnoreCase(showtime.getStatus())) {
            return null;
        }
        // || !showtime.getStart_at().after(new Timestamp(System.currentTimeMillis()))
        if (showtime.getStart_at() == null) {
            return null;
        }
        return showtime;
    }

    // Seats with price filled in, or null if pricing data is missing
    public List<Seats> getSeatsWithPrice(Showtimes showtime) {
        List<Seats> seats = seatsDAO.getSeatsWithStatusByShowtime(showtime.getShowtime_id());
        if (!pricingService.fillSeatPrices(showtime, seats)) {
            return null;
        }
        return seats;
    }
}
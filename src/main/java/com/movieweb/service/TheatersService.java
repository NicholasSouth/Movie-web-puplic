package com.movieweb.service;

import java.util.List;

import java.util.ArrayList;
import java.util.List;

import com.movieweb.DAO.TheatersDAO;
import com.movieweb.model.Theaters;

public class TheatersService {
    private TheatersDAO theatersDAO;
    public TheatersService() {theatersDAO = new TheatersDAO();}
    public List<Theaters> getAllTheaters() {return theatersDAO.getAllTheaters();}
    
    /*Search theaters by theater name or address.*/
    public List<Theaters> searchTheaters(
            List<Theaters> theaters,
            String keyword) {
        if (theaters == null) {
            return new ArrayList<>();
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            return theaters;
        }
        keyword = keyword.trim().toLowerCase();
        List<Theaters> results = new ArrayList<>();
        for (Theaters theater : theaters) {
            String theaterName = theater.getTheater_name();
            String theaterAddress = theater.getTheater_address();
            boolean nameMatches =
                    theaterName != null
                    && theaterName
                            .toLowerCase()
                            .contains(keyword);
            boolean addressMatches =
                    theaterAddress != null
                    && theaterAddress
                            .toLowerCase()
                            .contains(keyword);
            if (nameMatches || addressMatches) {
                results.add(theater);
            }
        }
        return results;
    }

    /* Find the theater closest to the user's location.
     * Returns null if no theater has valid coordinates.*/
    public Theaters findNearestTheater(
            List<Theaters> theaters,
            double userLat,
            double userLng) {
        if (theaters == null || theaters.isEmpty()) {
            return null;
        }
        Theaters nearestTheater = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Theaters theater : theaters) {
            double theaterLat = theater.getLatitude();
            double theaterLng = theater.getLongtitude();

            /*Ignore theaters without coordinates.*/
            if (Double.isNaN(theaterLat)
                    || Double.isNaN(theaterLng)) {
                continue;
            }
            double distance = calculateHaversine(
                            userLat,
                            userLng,
                            theaterLat,
                            theaterLng
                    );
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTheater = theater;
            }
        }
        return nearestTheater;
    }

    /* Calculate distance between two latitude/longitude coordinates. Returns kilometers.*/
    public double calculateDistance(
            double userLat,
            double userLng,
            Theaters theater) {
        if (theater == null
                || Double.isNaN(theater.getLatitude())
                || Double.isNaN(theater.getLongtitude())) {
            return Double.NaN;
        }
        return calculateHaversine(
                userLat,
                userLng,
                theater.getLatitude(),
                theater.getLongtitude()
        );
    }
    private double calculateHaversine(
            double lat1,
            double lng1,
            double lat2,
            double lng2) {
        final int EARTH_RADIUS = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a =
                Math.sin(dLat / 2)
                * Math.sin(dLat / 2)
                +
                Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );
        return EARTH_RADIUS * c;
    }
}
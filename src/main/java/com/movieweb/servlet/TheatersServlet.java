package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.model.Theaters;
import com.movieweb.service.TheatersService;

@WebServlet(urlPatterns = {"/theaters", "/Theaters"})
public class TheatersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TheatersService theatersService;
    public TheatersServlet() {
        this(new TheatersService());
    }
    public TheatersServlet(TheatersService theaterService) {
        this.theatersService = Objects.requireNonNull(theaterService);
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {
    	List<Theaters> allTheaters = theatersService.getAllTheaters();       
        /* Search */
        String searchParam = req.getParameter("search");
        List<Theaters> theaters;
        if (searchParam != null
    		&& !searchParam.trim().isEmpty()) {
            theaters = theatersService.searchTheaters(allTheaters, searchParam);
        } 
        else {
            theaters = allTheaters;
        }
        req.setAttribute("theaters", theaters);
        
        /* Nearest theater */
        String latParam = req.getParameter("userLat");
        String lngParam = req.getParameter("userLng");
        if (latParam != null
            && lngParam != null) {
            try {
                double userLat = Double.parseDouble(latParam);
                double userLng = Double.parseDouble(lngParam);
                Theaters nearestTheater = theatersService.findNearestTheater(
                                allTheaters,
                                userLat,
                                userLng
                        );
                if (nearestTheater != null) {
                    double nearestDistance = theatersService.calculateDistance(
                                    userLat,
                                    userLng,
                                    nearestTheater
                            );
                    req.setAttribute("nearestTheater", nearestTheater);
                    req.setAttribute("nearestDistance", nearestDistance);
                }
            } 
            catch (NumberFormatException e) {
            }
        }
        req.getRequestDispatcher("/theaters.jsp").forward(req, resp);
    }
}
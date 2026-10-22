package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.model.Movies;
import com.movieweb.service.Top10Movie;

@WebServlet("/home")
public class Top10MovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Top10Movie homeService;

    @Override
    public void init() throws ServletException {
        homeService = new Top10Movie();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        List<Movies> top10Movies = homeService.getTop10Movies();
        request.setAttribute("movieGridMovies", top10Movies);
        request.setAttribute("currentPage", "home");
        request.getRequestDispatcher("/main.jsp")
               .forward(request, response);
    }
}
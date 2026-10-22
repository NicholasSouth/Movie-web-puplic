package com.movieweb.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.movieweb.model.Movies;
import com.movieweb.service.Top10Movie;

@WebFilter("/main.jsp")
public class Top10Movies implements Filter {
    private Top10Movie homeService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        homeService = new Top10Movie();
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Get Top 10 movies
        List<Movies> top10Movies = homeService.getTop10Movies();

        // Make them available to main.jsp
        httpRequest.setAttribute(
                "movieGridMovies",
                top10Movies
        );

        // Continue to main.jsp
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
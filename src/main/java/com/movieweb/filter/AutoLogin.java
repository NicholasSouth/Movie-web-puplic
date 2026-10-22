package com.movieweb.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.model.Users;
import com.movieweb.service.RememberMeService;

@WebFilter("/*")
public class AutoLogin
        implements Filter
{
    private final RememberMeService rememberMeService = new RememberMeService();

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        //Check log in session
        HttpSession session = httpRequest.getSession(false);
        if (session == null ||
            session.getAttribute("user") == null)
        {
            //No session -> try cookie
            Users user = rememberMeService.autoLogin(httpRequest);
            if (user != null)
            {
                //Auto create session
                HttpSession newSession = httpRequest.getSession(true);
                newSession.setAttribute("user", user);
            }
        }

        //Continue request page
        chain.doFilter(httpRequest, httpResponse);
    }
}
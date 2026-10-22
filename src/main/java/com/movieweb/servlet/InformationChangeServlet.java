package com.movieweb.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.movieweb.model.Users;
import com.movieweb.service.InformationChangeService;

//To receive all kind of data
@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024,
	    maxFileSize = 10 * 1024 * 1024,
	    maxRequestSize = 20 * 1024 * 1024)
@WebServlet("/information-change")
public class InformationChangeServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private InformationChangeService informationChangeService;

    @Override
    public void init()
            throws ServletException
    {
    	ServletContext servletContext = getServletContext();
    	informationChangeService = new InformationChangeService(servletContext);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        request.getRequestDispatcher(
        		"/information_change.jsp")
        		.forward(
                        request,
                        response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        if (session == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        
        /*Get uploaded files.*/
        Part avatarPart = request.getPart("avatar");
        Part bannerPart = request.getPart("banner");
        
        /*Check if file is null*/
        if (avatarPart != null && avatarPart.getSize() == 0)
        {
            avatarPart = null;
        }
        if (bannerPart != null && bannerPart.getSize() == 0)
        {
            bannerPart = null;
        }
        String updateError = informationChangeService.updateInformationOrError(
                        currentUser,
                        fullName,
                        email,
                        phone,
                        avatarPart,
                        bannerPart);
        if (updateError == null)
        {
            session.setAttribute("user", currentUser);
            response.sendRedirect(request.getContextPath() + "/user-profile");
        }
        else
        {
            request.setAttribute(
                    "errorMessage",
                    updateError);
            request.getRequestDispatcher(
                    "/information_change.jsp")
                    .forward(
                            request,
                            response);
        }
    }
}
package com.movieweb.servlet;

import java.io.IOException;
import java.util.regex.Pattern;
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

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024)

@WebServlet("/manager-information-change")
public class ManagerInformationChangeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String FORM_PAGE = "/manager_information_change.jsp";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private InformationChangeService informationChangeService;
    
    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        informationChangeService = new InformationChangeService(servletContext);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getManager(request, response);
        if (currentUser == null) {
            return;
        }
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getManager(request, response);
        if (currentUser == null) {
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String rawPhone = trim(request.getParameter("phone"));
        String phone = normalizePhone(rawPhone);
        String validationError = validate(fullName, email, phone);
        if (validationError != null) {
            forwardWithError(request, response, validationError, fullName, email, rawPhone);
            return;
        }
        Part avatarPart;
        Part bannerPart;
        try {
            avatarPart = request.getPart("avatar");
            bannerPart = request.getPart("banner");
        } catch (IllegalStateException e) {
            forwardWithError(request, response, "An uploaded file is too large. Avatar: 5 MB maximum, banner: 10 MB maximum.", fullName, email, rawPhone);
            return;
        }
        if (avatarPart != null && avatarPart.getSize() == 0) {
            avatarPart = null;
        }
        if (bannerPart != null && bannerPart.getSize() == 0) {
            bannerPart = null;
        }
        String updateError = informationChangeService.updateInformationOrError(currentUser, fullName, email, phone, avatarPart, bannerPart);
        if (updateError == null) {
            request.getSession().setAttribute("user", currentUser);
            response.sendRedirect(request.getContextPath() + "/manager-profile");
        } else {
            forwardWithError(request, response, updateError, fullName, email, rawPhone);
        }
    }
    
    private String validate(String fullName, String email, String phone) {
        if (fullName.isEmpty()) {
            return "Full name is required.";
        }
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address.";
        }
        if (phone.isEmpty()) {
            return "A phone number is required for manager accounts.";
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "Please enter a valid phone number (9 to 15 digits, optional leading +).";
        }
        return null;
    }
    
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message, String fullName, String email, String phone) throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("formFullName", fullName);
        request.setAttribute("formEmail", email);
        request.setAttribute("formPhone", phone);
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }
    
    private Users getManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return null;
        }
        if (user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        return user;
    }
    
    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
    
    private String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("[\\s.\\-()]", "");
    }
}
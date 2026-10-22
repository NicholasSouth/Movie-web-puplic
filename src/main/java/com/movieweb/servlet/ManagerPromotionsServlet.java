package com.movieweb.servlet;
import com.movieweb.DAO.PromotionsDAO;
import com.movieweb.model.Promotions;
import com.movieweb.model.Users;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/manager-promotions")
public class ManagerPromotionsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    private final PromotionsDAO promotionsDAO = new PromotionsDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users manager = getManager(request);
        if (manager == null) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        request.setAttribute("currentManagerPage", "promotions");
        int page = parsePage(request.getParameter("page"));
        try {
            int totalPromotions = promotionsDAO.getPromotionCount();
            int totalPages = Math.max(1, (int) Math.ceil(totalPromotions / (double) PAGE_SIZE));
            page = Math.min(page, totalPages);
            List<Promotions> promotions = promotionsDAO.getPromotions(page, PAGE_SIZE);
            request.setAttribute("promotions", promotions);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalPromotions", totalPromotions);
        } 
        catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("promotions", new ArrayList<Promotions>());
            request.setAttribute("currentPage", 1);
            request.setAttribute("totalPages", 1);
            request.setAttribute("totalPromotions", 0);
            request.setAttribute("errorMessage", "Unable to load promotions. Please try again.");
        }
        HttpSession session = request.getSession();
        Object flashMessage = session.getAttribute("managerPromotionsFlashMessage");
        Object flashType = session.getAttribute("managerPromotionsFlashType");
        if (flashMessage != null) {
            request.setAttribute("successMessage", "success".equals(flashType) ? flashMessage : null);
            request.setAttribute("errorMessage", "error".equals(flashType) ? flashMessage : null);
            session.removeAttribute("managerPromotionsFlashMessage");
            session.removeAttribute("managerPromotionsFlashType");
        }
        request.getRequestDispatcher("/manager_promotions.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users manager = getManager(request);
        if (manager == null) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }
        String action = request.getParameter("action");
        if (!"create".equals(action)) {
            setFlash(request, "error", "Invalid promotion action.");
            redirectToPromotions(request, response);
            return;
        }
        String code = request.getParameter("promotion_code");
        String description = request.getParameter("description");
        String priceModify = request.getParameter("price_modify");
        String startAtInput = request.getParameter("start_at");
        String endAtInput = request.getParameter("end_at");
        String usageLimitInput = request.getParameter("usage_limit");
        code = code == null ? "" : code.trim();
        description = description == null ? "" : description.trim();
        priceModify = priceModify == null ? "" : priceModify.trim();
        if (code.isEmpty() || code.length() > 20) {
            fail(request, response, "Promotion code is required and must not exceed 20 characters.");
            return;
        }
        if (description.isEmpty()) {
            fail(request, response, "Description is required.");
            return;
        }
        if (priceModify.isEmpty() || priceModify.length() > 5 || !priceModify.matches("[+-]?\\d+(\\.\\d+)?%?")) {
            fail(request, response, "Invalid price modifier. Use a number such as -20 or a percentage such as -10%.");
            return;
        }
        Timestamp startAt;
        Timestamp endAt;
        try {
            if (startAtInput == null || startAtInput.trim().isEmpty() || endAtInput == null || endAtInput.trim().isEmpty()) {
                throw new DateTimeParseException("Missing date", "", 0);
            }
            startAt = Timestamp.valueOf(LocalDateTime.parse(startAtInput.trim()));
            endAt = Timestamp.valueOf(LocalDateTime.parse(endAtInput.trim()));
        } 
        catch (DateTimeParseException e) {
            fail(request, response, "Please enter valid promotion start and end dates.");
            return;
        }
        if (!endAt.after(startAt)) {
            fail(request, response, "Promotion end time must be later than its start time.");
            return;
        }
        int usageLimit;
        try {
            usageLimit = Integer.parseInt(usageLimitInput == null ? "" : usageLimitInput.trim());
            if (usageLimit <= 0) {
                fail(request, response, "Usage limit must be greater than zero.");
                return;
            }
        } 
        catch (NumberFormatException e) {
            fail(request, response, "Please enter a valid positive usage limit.");
            return;
        }
        try {
            if (promotionsDAO.promotionCodeExists(code)) {
                fail(request, response, "This promotion code already exists.");
                return;
            }
            Promotions promotion = new Promotions();
            promotion.setPromotion_code(code);
            promotion.setDescription(description);
            promotion.setPrice_modify(priceModify);
            promotion.setStart_at(startAt);
            promotion.setEnd_at(endAt);
            promotion.setUsage_limit(usageLimit);
            boolean active = "true".equalsIgnoreCase(request.getParameter("isActive"));
            promotion.setActive(active);
            int promotionId = promotionsDAO.createPromotion(promotion);
            if (promotionId > 0) {
                setFlash(request, "success", "Promotion created successfully.");
            } 
            else {
                setFlash(request, "error", "Promotion could not be created.");
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
            setFlash(request, "error", "A database error occurred while creating the promotion.");
        }
        redirectToPromotions(request, response);
    }
    
    private Users getManager(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userObject = session.getAttribute("user");
        if (!(userObject instanceof Users)) {
            return null;
        }
        Users user = (Users) userObject;
        if (user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return user;
    }
    
    private int parsePage(String value) {
        try {
            int page = Integer.parseInt(value);
            return Math.max(page, 1);
        } 
        catch (NumberFormatException e) {
            return 1;
        }
    }
    
    private void setFlash(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("managerPromotionsFlashType", type);
        session.setAttribute("managerPromotionsFlashMessage", message);
    }
    
    private void fail(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        setFlash(request, "error", message);
        redirectToPromotions(request, response);
    }
    
    private void redirectToPromotions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/manager-promotions");
    }
}
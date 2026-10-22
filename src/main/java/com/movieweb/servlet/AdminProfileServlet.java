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

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;
import com.movieweb.service.InformationChangeService;
import com.movieweb.service.OTPValidation;
import com.movieweb.service.UpdatePassword;

/* One page for the administrator's own account: profile, information change and password change
 * (this replaces separate profile / settings / information-change / change-password pages).
 * Authorization is enforced by AdminFilter and re-checked here. There is deliberately no
 * "delete account" action: administrator accounts cannot be deleted.
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024)
@WebServlet("/admin-profile")
public class AdminProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String PAGE = "/admin_profile.jsp";
    static final String PASSWORD_PURPOSE = "admin_change_password";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsersDAO usersDAO = new UsersDAO();
    private final UpdatePassword updatePassword = new UpdatePassword();
    private final OTPValidation otp = new OTPValidation();
    private InformationChangeService informationChangeService;

    @Override
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        informationChangeService = new InformationChangeService(ctx);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireAdmin(request, response) == null) {
            return;
        }
        render(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users sessionUser = requireAdmin(request, response);
        if (sessionUser == null) {
            return;
        }
        String action = request.getParameter("action");
        if ("information".equals(action)) {
            changeInformation(request, response, sessionUser);
        } else if ("password".equals(action)) {
            changePassword(request, response, sessionUser);
        } else {
            request.setAttribute("errorSection", "information");
            request.setAttribute("errorMessage", "Unknown action.");
            render(request, response);
        }
    }

    private void changeInformation(HttpServletRequest request, HttpServletResponse response, Users sessionUser)
            throws ServletException, IOException {
        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String rawPhone = trim(request.getParameter("phone"));
        String phone = rawPhone.replaceAll("[\\s.\\-()]", "");

        String error = null;
        if (fullName.isEmpty()) {
            error = "Full name is required.";
        } else if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            error = "Please enter a valid email address.";
        } else if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            error = "Please enter a valid phone number (9 to 15 digits, optional leading +).";
        }
        if (error == null) {
            Part avatar;
            Part banner;
            try {
                avatar = request.getPart("avatar");
                banner = request.getPart("banner");
            } catch (IllegalStateException e) {
                avatar = null;
                banner = null;
                error = "An uploaded file is too large. Avatar: 5 MB maximum, banner: 10 MB maximum.";
            }
            if (error == null) {
                if (avatar != null && avatar.getSize() == 0) {
                    avatar = null;
                }
                if (banner != null && banner.getSize() == 0) {
                    banner = null;
                }
                error = informationChangeService.updateInformationOrError(sessionUser, fullName, email, phone, avatar, banner);
            }
        }
        if (error != null) {
            request.setAttribute("errorSection", "information");
            request.setAttribute("errorMessage", error);
            request.setAttribute("formFullName", fullName);
            request.setAttribute("formEmail", email);
            request.setAttribute("formPhone", rawPhone);
            render(request, response);
            return;
        }
        request.getSession().setAttribute("user", sessionUser);
        request.getSession().setAttribute("adminProfileFlash", "Your information has been updated.");
        response.sendRedirect(request.getContextPath() + "/admin-profile");
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response, Users sessionUser)
            throws ServletException, IOException {
        String current = request.getParameter("currentPassword");
        String next = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");
        String code = request.getParameter("verification_code");
        Users fresh = usersDAO.getUserById(sessionUser.getUserId());

        // Everything that can fail is checked BEFORE the emailed code is consumed,
        // so a typo in the form does not force the admin to request a new code.
        String error = null;
        if (fresh == null) {
            error = "Unable to load your account. Please try again.";
        } else if (isEmpty(current) || isEmpty(next) || isEmpty(confirm)) {
            error = "Please fill in all password fields.";
        } else if (!next.equals(confirm)) {
            error = "New password and confirmation do not match.";
        } else if (next.length() < 8) {
            error = "Password must contain at least 8 characters.";
        } else if (current.equals(next)) {
            error = "Your new password must be different from your current password.";
        } else if (fresh.getPassword() == null || !current.equals(fresh.getPassword())) {
            error = "Your current password is incorrect.";
        } else if (isEmpty(sessionUser.getEmail())
                || !otp.verifyCode(request.getSession(), sessionUser.getEmail().trim().toLowerCase(),
                        PASSWORD_PURPOSE, code)) {
            error = "The verification code is incorrect or has expired. Please request a new code.";
        }
        if (error == null) {
            error = updatePassword.changePassword(fresh, current, next, confirm);
        }
        if (error != null) {
            request.setAttribute("errorSection", "password");
            request.setAttribute("errorMessage", error);
            render(request, response);
            return;
        }
        sessionUser.setPassword(next);
        request.getSession().setAttribute("adminProfileFlash", "Your password has been changed successfully.");
        response.sendRedirect(request.getContextPath() + "/admin-profile");
    }

    private void render(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users sessionUser = (Users) session.getAttribute("user");
        Users profileUser = usersDAO.getUserById(sessionUser.getUserId());
        if (profileUser == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to load your profile. Please try again.");
            return;
        }
        request.setAttribute("profileUser", profileUser);
        request.setAttribute("flash", session.getAttribute("adminProfileFlash"));
        request.setAttribute("flashError", session.getAttribute("adminProfileFlashError"));
        session.removeAttribute("adminProfileFlash");
        session.removeAttribute("adminProfileFlashError");
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher(PAGE).forward(request, response);
    }

    private Users requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return null;
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole()) || !user.isActive()) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return null;
        }
        return user;
    }

    private String trim(String v) {
        return v == null ? "" : v.trim();
    }

    private boolean isEmpty(String v) {
        return v == null || v.trim().isEmpty();
    }
}

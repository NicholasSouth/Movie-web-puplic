package com.movieweb.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.DAO.Remember_meDAO;
import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Remember_me;
import com.movieweb.model.Users;

public class RememberMeService
{
    // Cookie name
    private static final String COOKIE_NAME = "remember_me";

    // Token expires after 6 months
    // 180 days
    private static final long TOKEN_LIFETIME = 180L * 24 * 60 * 60 * 1000;

    // Random token generator
    private final SecureRandom random = new SecureRandom();
    private final Remember_meDAO rememberDAO;
    private final UsersDAO usersDAO;

    // Constructor
    public RememberMeService()
    {
        rememberDAO = new Remember_meDAO();
        usersDAO = new UsersDAO();
    }

    // Create remember-me token
    public void createRememberMe(Users user, HttpServletResponse response)
    {
        if (user == null)
        {
            return;
        }

        // Generate random token
        String rawToken = generateToken();

        // Hash token before storing it
        String tokenHash = hashToken(rawToken);

        // Calculate expiration date
        long expirationTime = System.currentTimeMillis() + TOKEN_LIFETIME;
        Timestamp expiresAt =new Timestamp(expirationTime);

        // Create database object
        Remember_me rememberMe = new Remember_me();
        rememberMe.setUserId(user.getUserId());
        rememberMe.setTokenHash(tokenHash);
        rememberMe.setExpiresAt(expiresAt);

        // Save token to database
        boolean inserted = rememberDAO.insertToken(rememberMe);
        if (!inserted)
        {
            return;
        }

        // Create browser cookie
        Cookie cookie = new Cookie(COOKIE_NAME, rawToken);

        // Cookie lasts 6 months
        cookie.setMaxAge((int)(TOKEN_LIFETIME / 1000));

        // Cookie is available to the whole website
        cookie.setPath("/");

        // JavaScript cannot access this cookie
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    // Automatically log in using remember-me cookie
    public Users autoLogin(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
        {
            return null;
        }
        for (Cookie cookie : cookies)
        {
            if (!COOKIE_NAME.equals(cookie.getName()))
            {
                continue;
            }
            String rawToken = cookie.getValue();
            if (rawToken == null ||
                rawToken.isEmpty())
            {
                return null;
            }

            // Hash cookie token
            String tokenHash = hashToken(rawToken);

            // Find token in database
            Remember_me rememberMe = rememberDAO.getValidToken(tokenHash);
            if (rememberMe == null)
            {
                return null;
            }

            // Find user using user_id
            Users user = usersDAO.getUserById(rememberMe.getUserId());
            if (user == null)
            {
                return null;
            }
            // Do not auto-login disabled accounts
            if (!user.isActive())
            {
                return null;
            }
            return user;
        }
        return null;
    }

    // Remove remember-me token and cookie
    public void removeRememberMe(HttpServletRequest request, HttpServletResponse response)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
        {
            return;
        }
        for (Cookie cookie : cookies)
        {
            if (!COOKIE_NAME.equals(
                    cookie.getName()))
            {
                continue;
            }
            String rawToken = cookie.getValue();
            if (rawToken != null &&
                !rawToken.isEmpty())
            {
                String tokenHash = hashToken(rawToken);

                // Delete token from database
                rememberDAO.deleteToken(tokenHash);
            }

            // Delete browser cookie
            Cookie deleteCookie = new Cookie(COOKIE_NAME, "");
            deleteCookie.setMaxAge(0);
            deleteCookie.setPath("/");
            deleteCookie.setHttpOnly(true);
            deleteCookie.setSecure(false);
            response.addCookie(deleteCookie);
            return;
        }
    }

    // Generate random token
    private String generateToken()
    {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 hash
    private String hashToken(String token)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash)
            {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-256 algorithm is not available.", e);
        }
    }
}
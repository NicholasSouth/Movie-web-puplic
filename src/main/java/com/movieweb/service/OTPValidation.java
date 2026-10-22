package com.movieweb.service;

import java.security.SecureRandom;
import javax.servlet.http.HttpSession;

public class OTPValidation
{
    private static final int OTP_LENGTH = 6;
    
    // OTP expires after 5 minutes
    private static final long OTP_EXPIRATION_TIME = 5 * 60 * 1000;
    private static final String OTP_KEY = "verification_otp";
    private static final String EMAIL_KEY = "verification_email";
    private static final String PURPOSE_KEY = "verification_purpose";
    private static final String EXPIRATION_KEY = "verification_expiration";
    private final SecureRandom random = new SecureRandom();

    // Generate 6-digit OTP
    public String generateCode()
    {
        int number = random.nextInt(1_000_000);
        return String.format("%06d",number);
    }

    // Store OTP in session
    public void storeCode(
            HttpSession session,
            String email,
            String purpose,
            String code)
    {
        long expiration = System.currentTimeMillis() + OTP_EXPIRATION_TIME;
        session.setAttribute(OTP_KEY,code);
        session.setAttribute(EMAIL_KEY,email);
        session.setAttribute(PURPOSE_KEY,purpose);
        session.setAttribute(EXPIRATION_KEY,expiration);
    }

    // Verify OTP
    public boolean verifyCode(
            HttpSession session,
            String email,
            String purpose,
            String enteredCode)
    {
        if (enteredCode == null ||
            enteredCode.trim().isEmpty())
        {
            return false;
        }
        String storedCode = (String) session.getAttribute(OTP_KEY);
        String storedEmail = (String) session.getAttribute(EMAIL_KEY);
        String storedPurpose = (String) session.getAttribute(PURPOSE_KEY);
        Long expiration = (Long) session.getAttribute(EXPIRATION_KEY);

        // No OTP stored
        if (storedCode == null ||
            storedEmail == null ||
            storedPurpose == null ||
            expiration == null)
        {
            return false;
        }

        // OTP expired
        if (System.currentTimeMillis() > expiration)
        {
            clearCode(session);
            return false;
        }

        // Email must match
        if (!storedEmail.equalsIgnoreCase(email))
        {
            return false;
        }

        // Purpose must match
        if (!storedPurpose.equals(purpose))
        {
            return false;
        }

        // OTP must match
        if (!storedCode.equals(
                enteredCode.trim()))
        {
            return false;
        }

        // OTP successfully used       
        clearCode(session);
        return true;
    }

    // Clear OTP
    public void clearCode(
            HttpSession session)
    {
        session.removeAttribute(OTP_KEY);
        session.removeAttribute(EMAIL_KEY);
        session.removeAttribute(PURPOSE_KEY);
        session.removeAttribute(EXPIRATION_KEY);
    }
}
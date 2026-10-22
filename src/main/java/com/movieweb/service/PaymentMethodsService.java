package com.movieweb.service;

import java.sql.Date;
import java.time.YearMonth;
import java.util.List;
import com.movieweb.DAO.Payment_methodsDAO;
import com.movieweb.model.Payment_methods;
import com.movieweb.DAO.Theater_payment_methodsDAO;
import com.movieweb.model.Theater_payment_methods;
import com.movieweb.DAO.ManagersDAO;

public class PaymentMethodsService {
    private final Payment_methodsDAO payment_methodsDAO;
    private final Theater_payment_methodsDAO theater_payment_methodsDAO;
    private final ManagersDAO managersDAO;
    public PaymentMethodsService() {
        payment_methodsDAO = new Payment_methodsDAO();
        managersDAO = new ManagersDAO();
        theater_payment_methodsDAO = new Theater_payment_methodsDAO();
    }

    // Get all payment methods belonging to a user
    public List<Payment_methods> getPaymentMethodsByUserId(int user_id) {
        return payment_methodsDAO.getPaymentMethodsByUserId(user_id);
    }

    // Add a simulated VISA card
    public String addVisaCard(int user_id, String card_number, String expired_date) {
        // Remove spaces from the card number.
        card_number = card_number.replaceAll("\\s+", "");

        // Basic card number validation.
        if (!isValidVisaCardNumber(card_number)) {
            return "Invalid VISA card number.";
        }

        // Validate expiry date.
        Date expiryDate = parseExpiryDate(expired_date);
        if (expiryDate == null) {
            return "Invalid expiry date. Please use MM/YY.";
        }

        // Check whether the card has already expired.
        YearMonth currentMonth = YearMonth.now();
        YearMonth expiryMonth = YearMonth.of(
            expiryDate.toLocalDate().getYear(),
            expiryDate.toLocalDate().getMonthValue()
        );
        if (expiryMonth.isBefore(currentMonth)) {
            return "The VISA card has already expired.";
        }

        // Create payment method object.
        Payment_methods payment_method = new Payment_methods();
        payment_method.setUser_id(user_id);
        payment_method.setMethod("VISA");
        payment_method.setCard_number(card_number);
        payment_method.setExpired_date(expiryDate);
        payment_method.setActive(true);

        // Save to database.
        boolean added = payment_methodsDAO.addPaymentMethod(payment_method);
        if (!added) {
            return "Failed to add the VISA card.";
        }
        return null;
    }

    // Deactivate a payment method belonging to the user.
    public String deactivatePaymentMethod(int payment_method_id, int user_id) {
        Payment_methods payment_method = payment_methodsDAO.getPaymentMethodById(payment_method_id);
        if (payment_method == null) {
            return "Payment method not found.";
        }

        // Prevent users from deactivating another user's card.
        if (payment_method.getUser_id() != user_id) {
            return "You cannot deactivate this payment method.";
        }
        if (!payment_method.isActive()) {
            return "This payment method is already inactive.";
        }
        boolean deactivated = payment_methodsDAO.deactivatePaymentMethod(payment_method_id);
        if (!deactivated) {
            return "Failed to deactivate the payment method.";
        }
        return null;
    }

    // Validate a VISA card number.
    private boolean isValidVisaCardNumber(String card_number) {
        // VISA cards normally begin with 4.
        if (card_number == null 
    		|| !card_number.matches("\\d{16}") 
    		|| !card_number.startsWith("4")) {
            return false;
        }
        return passesLuhnCheck(card_number);
    }

    // Luhn algorithm used for basic card-number validation.
    private boolean passesLuhnCheck(String card_number) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = card_number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(card_number.charAt(i));
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    // Convert MM/YYYY into java.sql.Date.
    private Date parseExpiryDate(String expired_date) {
        if (!expired_date.matches("(0[1-9]|1[0-2])/\\d{4}")) {
            return null;
        }
        try {
            String[] parts = expired_date.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]);
            YearMonth expiry = YearMonth.of(year, month);
            YearMonth current = YearMonth.now();
            if (expiry.isBefore(current)) {
                return null;
            }
            return Date.valueOf(expiry.atDay(1));
        } 
        catch (Exception e) {
            return null;
        }
    }

    // Get VISA cards belonging to the manager and assigned to the selected theater.
    public List<Payment_methods> getPaymentMethodsByManagerAndTheater(int manager_id, int theater_id) {
    	if (!managersDAO.isManagerAssignedToTheater(manager_id, theater_id)) {
            return new java.util.ArrayList<>();
        }
        return payment_methodsDAO.getPaymentMethodsByUserAndTheater(manager_id, theater_id);
    }

    // Add a VISA card and register it for a specific theater.
    public String addVisaCardForTheater(
            int manager_id,
            int theater_id,
            String card_number,
            String expired_date) {
    	
        // Make sure this manager actually manages this theater.
        if (!managersDAO.isManagerAssignedToTheater(manager_id, theater_id)) {
            return "You are not assigned to this theater.";
        }

        // Remove spaces from the card number.
        card_number = card_number.replaceAll("\\s+", "");

        // Basic card number validation.
        if (!isValidVisaCardNumber(card_number)) {
            return "Invalid VISA card number.";
        }

        // Validate expiry date.
        Date expiryDate = parseExpiryDate(expired_date);
        if (expiryDate == null) {
            return "Invalid expiry date. Please use MM/YYYY.";
        }

        // Check whether the card has already expired.
        YearMonth currentMonth = YearMonth.now();
        YearMonth expiryMonth = YearMonth.of(
            expiryDate.toLocalDate().getYear(),
            expiryDate.toLocalDate().getMonthValue());
        if (expiryMonth.isBefore(currentMonth)) {
            return "The VISA card has already expired.";
        }

        // Create payment method.
        Payment_methods payment_method = new Payment_methods();
        payment_method.setUser_id(manager_id);
        payment_method.setMethod("VISA");
        payment_method.setCard_number(card_number);
        payment_method.setExpired_date(expiryDate);
        payment_method.setActive(true);

        // Save the VISA card.
        boolean added = payment_methodsDAO.addPaymentMethod(payment_method);
        if (!added) {
            return "Failed to add the VISA card.";
        }

        // payment_method_id was populated by Payment_methodsDAO.
        int payment_method_id = payment_method.getPayment_method_id();

        // Register this card for the selected theater.
        Theater_payment_methods theater_payment_method = new Theater_payment_methods(payment_method_id, theater_id);
        boolean mapped = theater_payment_methodsDAO.addTheaterPaymentMethod(theater_payment_method);
        if (!mapped) {
            return "Failed to register the VISA card for this theater.";
        }
        return null;
    }
    
    // Deactivate a VISA card belonging to the manager and registered for this theater.
    public String deactivatePaymentMethodForTheater(
            int payment_method_id,
            int manager_id,
            int theater_id) {

        // Make sure the manager actually manages this theater.
        if (!managersDAO.isManagerAssignedToTheater(manager_id, theater_id)) {
            return "You are not assigned to this theater.";
        }

        // Find the payment method.
        Payment_methods payment_method = payment_methodsDAO.getPaymentMethodById(payment_method_id);
        if (payment_method == null) {
            return "Payment method not found.";
        }

        // Make sure the card belongs to this manager.
        if (payment_method.getUser_id() != manager_id) {
            return "You cannot deactivate this payment method.";
        }

        // Make sure this card is registered for this theater.
        if (!theater_payment_methodsDAO.exists(
                payment_method_id,
                theater_id)) {
            return "This payment method is not registered for this theater.";
        }
        if (!payment_method.isActive()) {
            return "This payment method is already inactive.";
        }
        boolean deactivated = payment_methodsDAO.deactivatePaymentMethod(payment_method_id);
        if (!deactivated) {
            return "Failed to deactivate the payment method.";
        }
        return null;
    }
}
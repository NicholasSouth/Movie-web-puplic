package com.movieweb.service;

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;

public class UpdatePassword
{
    private final UsersDAO usersDAO;
    public UpdatePassword()
    {
        this.usersDAO = new UsersDAO();
    }
    public String changePassword(
            Users user,
            String currentPassword,
            String newPassword,
            String confirmPassword)
    {
        if (user == null)
        {
            return "You must be logged in.";
        }
        if (currentPassword == null
            || currentPassword.trim().isEmpty())
        {
            return "Please enter your current password.";
        }
        if (newPassword == null
            || newPassword.trim().isEmpty())
        {
            return "Please enter a new password.";
        }
        if (confirmPassword == null
            || confirmPassword.trim().isEmpty())
        {
            return "Please confirm your new password.";
        }
        if (!newPassword.equals(confirmPassword))
        {
            return "New password and confirmation do not match.";
        }
        if (currentPassword.equals(newPassword))
        {
            return "Your new password must be different from your current password.";
        }
        if (newPassword.length() < 8)
        {
            return "Password must contain at least 8 characters.";
        }

        // Verify the current password
        boolean currentPasswordCorrect = verifyCurrentPassword(user, currentPassword);
        if (!currentPasswordCorrect)
        {
            return "Your current password is incorrect.";
        }

        // Update password using the same plain-text approach
        boolean updated = usersDAO.updatePassword(user.getUserId(), newPassword
        );
        if (!updated)
        {
            return "Unable to change your password. Please try again.";
        }

        // Keep the session user's password synchronized
        user.setPassword(newPassword);
        return null;
    }

    private boolean verifyCurrentPassword(
            Users user,
            String currentPassword)
    {
        if (user.getPassword() == null)
        {
            return false;
        }
        return currentPassword.equals(user.getPassword());
    }
}
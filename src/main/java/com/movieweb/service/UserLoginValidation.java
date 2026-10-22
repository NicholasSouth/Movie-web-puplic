package com.movieweb.service;

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;

public class UserLoginValidation
{
    private final UsersDAO usersDAO;
    public UserLoginValidation()
    {
        usersDAO = new UsersDAO();
    }

    // Login
    public Users login(String login, String password)
    {
        if (login == null 
        	|| login.trim().isEmpty() 
        	|| password == null 
            || password.isEmpty())
        {
            return null;
        }
    
        //Check if user input email to find account
        Users user = getUserByLogin(login);
        
        //If no user found
        if (user == null)
        {
            return null;
        }
        
        //If account blocked
        if (!user.isActive())
        {
            return null;
        }

        //Add password hash later
        if (!password.equals(user.getPassword()))
        {
            return null;
        }
        return user;
    }

    //Register
    public String registerUser(
            String username,
            String fullName,
            String email,
            String phone,
            String password,
            String confirmPassword,
            String role)
    {
        // Basic validation
        if (username == null 
        	|| username.trim().isEmpty())
        {
            return "Username is required.";
        }
        if (fullName == null 
        	|| fullName.trim().isEmpty())
        {
            return "Full name is required.";
        }
        if (email == null 
        	|| email.trim().isEmpty())
        {
            return "Email is required.";
        }
        if (password == null 
        	|| password.isEmpty())
        {
            return "Password is required.";
        }
        if (confirmPassword == null 
        	|| confirmPassword.isEmpty())
        {
            return "Please confirm your password.";
        }

        // Clean input
        username = username.trim();
        fullName = fullName.trim();
        email = email.trim().toLowerCase();
        if (phone != null)
        {
            phone = phone.trim();
            if (phone.isEmpty())
            {
                phone = null;
            }
        }

        // Password confirmation
        if (!password.equals(confirmPassword))
        {
            return "Passwords do not match.";
        }

        // Email validation
        if (!isValidEmail(email))
        {
            return "Please enter a valid email address.";
        }

        // Username validation
        if (!isValidUsername(username))
        {
            return "Username must contain only letters, " + "numbers, and underscores.";
        }

        // Check active duplicate username
        if (usersDAO.usernameExists(username))
        {
            return "Username is already taken.";
        }

        // Check active duplicate email
        if (usersDAO.emailExists(email))
        {
            return "Email is already registered.";
        }

        // Check active duplicate phone
        if (phone != null &&
            usersDAO.phoneExists(phone))
        {
            return "Phone number is already registered.";
        }

        // Check whether an old soft-deleted account can be reused.
        Users reusableAccount = usersDAO.getDeletedUserByUsername(username);
        if (reusableAccount == null)
        {
            reusableAccount = usersDAO.getDeletedUserByEmail(email);
        }
        
        // Validate role       
        if (role == null ||
            role.trim().isEmpty())
        {
            role = "USER";
        }
        else
        {
            role = role.trim().toUpperCase();
        }

        //Only these roles are currently allowed through normal registration.
        if (!role.equals("USER") &&
            !role.equals("MANAGER"))
        {
            return "Invalid account role.";
        }

        // Create user
        Users user = new Users();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        
        // New users do not have an avatar/banner yet.
        user.setAvtPath(null);
        user.setBannerPath(null);
        user.setRole(role);

        // USER: active immediately.
		// MANAGER: starts inactive because admin must approve the account.
        if (role.equals("MANAGER"))
        {
            user.setActive(false);
        }
        else
        {
            user.setActive(true);
        }

        // Save account
        boolean saved;
        if (reusableAccount != null)
        {
            // Reuse the old soft-deleted account.
            user.setUserId(reusableAccount.getUserId());
            saved = usersDAO.reuseDeletedAccount(user);
        }
        else
        {
            // Create a completely new account.
            saved = usersDAO.insertUser(user);
        }

        if (!saved)
        {
            return "Unable to create account. Please try again.";
        }
        return null;
    }

    // Reset password
    public String resetPassword(
            String email,
            String newPassword,
            String confirmPassword)
    {
        if (email == null ||
            email.trim().isEmpty())
        {
            return "Email is required.";
        }
        if (newPassword == null ||
            newPassword.isEmpty())
        {
            return "New password is required.";
        }
        if (confirmPassword == null ||
            confirmPassword.isEmpty())
        {
            return "Please confirm your new password.";
        }
        email = email.trim().toLowerCase();
        if (!newPassword.equals(confirmPassword))
        {
            return "Passwords do not match.";
        }
        if (!isValidEmail(email))
        {
            return "Please enter a valid email address.";
        }
        Users user = usersDAO.getUserByEmail(email);
        if (user == null)
        {
            return "No account was found with this email.";
        }
        if (!user.isActive())
        {
            return "This account is currently disabled.";
        }
        boolean updated = usersDAO.updatePassword(user.getUserId(), newPassword);
        if (!updated)
        {
            return "Unable to reset password. Please try again.";
        }
        return null;
    }

    // Get user by login  
    public Users getUserByLogin(String login)
    {
        if (login == null ||
            login.trim().isEmpty())
        {
            return null;
        }
        login = login.trim();
        if (login.contains("@"))
        {
            return usersDAO.getUserByEmail(login);
        }
        Users user = usersDAO.getUserByUsername(login);
        if (user == null)
        {
            user = usersDAO.getUserByPhone(login);
        }
        return user;
    }

    // Email validation
    private boolean isValidEmail(String email)
    {
        String emailPattern = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        return email.matches(emailPattern);
    }
   
    // Username validation
    private boolean isValidUsername(String username)
    {       
        return username.matches("^[a-zA-Z0-9_]+$");
    }
}
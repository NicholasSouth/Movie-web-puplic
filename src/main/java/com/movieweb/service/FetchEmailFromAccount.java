package com.movieweb.service;

import com.movieweb.model.Users;

public class FetchEmailFromAccount
{
    private final UserLoginValidation userService;
    public FetchEmailFromAccount()
    {
        userService = new UserLoginValidation();
    }

    //Find an account using: username, email, phone. 
    //Then return the email belonging to that account.
    public String getAccountEmail(String account)
    {
        if (account == null ||
            account.trim().isEmpty())
        {
            return null;
        }
        account = account.trim();
        Users user = userService.getUserByLogin(account);
        if (user == null)
        {
            return null;
        }
        if (!user.isActive())
        {
            return null;
        }
        String email = user.getEmail();
        if (email == null ||
            email.trim().isEmpty())
        {
            return null;
        }
        return email.trim().toLowerCase();
    }

    //Find the user. Useful later when resetting the password.
    public Users getUser(String account)
    {
        if (account == null ||
            account.trim().isEmpty())
        {
            return null;
        }
        Users user = userService.getUserByLogin(account.trim());
        if (user == null ||
            !user.isActive())
        {
            return null;
        }
        return user;
    }
}
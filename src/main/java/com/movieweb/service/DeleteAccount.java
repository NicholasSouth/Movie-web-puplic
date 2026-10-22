package com.movieweb.service;

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;

public class DeleteAccount
{
    private final UsersDAO usersDAO;
    public DeleteAccount()
    {
        this.usersDAO = new UsersDAO();
    }
    public boolean deleteAccount(Users user)
    {
        if (user == null)
        {
            return false;
        }
        return usersDAO.softDeleteAccount(user.getUserId());
    }
}
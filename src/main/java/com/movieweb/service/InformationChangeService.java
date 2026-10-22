package com.movieweb.service;

import javax.servlet.http.Part;
import javax.servlet.ServletContext;

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Users;

public class InformationChangeService {
    private UsersDAO usersDAO;
    private ImageService imageService;
    
    public InformationChangeService(ServletContext servletContext) {
        usersDAO = new UsersDAO();
        imageService = new ImageService(servletContext);
    }
    
    public boolean updateInformation(Users currentUser, String fullName, String email, String phone, Part avatarPart, Part bannerPart) {
        return updateInformationOrError(currentUser, fullName, email, phone, avatarPart, bannerPart) == null;
    }
    
    public String updateInformationOrError(Users currentUser, String fullName, String email, String phone, Part avatarPart, Part bannerPart) {
        if (currentUser == null) {
            return "You must be logged in to change your information.";
        }
        fullName = fullName == null ? "" : fullName.trim();
        email = email == null ? "" : email.trim();
        phone = phone == null ? "" : phone.trim();
        if (fullName.isEmpty()) {
            return "Full name is required.";
        }
        if (email.isEmpty()) {
            return "Email is required.";
        }
        Users emailUser = usersDAO.getUserByEmail(email);
        if (emailUser != null && emailUser.getUserId() != currentUser.getUserId()) {
            return "This email address is already used by another account.";
        }
        if (!phone.isEmpty()) {
            Users phoneUser = usersDAO.getUserByPhone(phone);
            if (phoneUser != null && phoneUser.getUserId() != currentUser.getUserId()) {
                return "This phone number is already used by another account.";
            }
        }
        String avatarPath = imageService.saveAvatar(currentUser, avatarPart);
        if (avatarPart != null && avatarPath == null) {
            return "The avatar could not be saved. Check the file type and size.";
        }
        String bannerPath = imageService.saveBanner(currentUser, bannerPart);
        if (bannerPart != null && bannerPath == null) {
            return "The banner could not be saved. Check the file type and size.";
        }
        boolean updated = usersDAO.updateUserInformation(currentUser.getUserId(), fullName, email, phone, avatarPath, bannerPath);
        if (!updated) {
            return "The update could not be saved. Please try again.";
        }
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setPhone(phone.isEmpty() ? null : phone);
        if (avatarPath != null) {
            currentUser.setAvtPath(avatarPath);
        }
        if (bannerPath != null) {
            currentUser.setBannerPath(bannerPath);
        }
        return null;
    }
}
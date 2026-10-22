package com.movieweb.model;
import java.sql.Timestamp;
public class Users 
{
	private int user_id;
    private String username;
    private String full_name;
    private String password;
    private String email;
    private String phone;
    private String avt_path;
    private String banner_path;
    private boolean favourite_movies_visibility;
    private String role;
    private boolean isActive;
    private Timestamp created_at;
    private Timestamp deleted_at;
    //
    public Users() {
    }
    public Users(
            int user_id,
            String username,
            String full_name,
            String password,
            String email,
            String phone,
            String avt_path,
            String banner_path,
            String role,
            boolean favourite_movies_visibility,
            boolean isActive,
            Timestamp created_at,
            Timestamp deleted_at)
    {
        this.user_id = user_id;
        this.username = username;
        this.full_name = full_name;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.avt_path = avt_path;
        this.banner_path = banner_path;
        this.favourite_movies_visibility = favourite_movies_visibility;
        this.role = role;
        this.isActive = isActive;
        this.created_at = created_at;
        this.deleted_at = deleted_at;
    }
    public int getUserId() {
        return user_id;
    }
    public void setUserId(int userId) {
        this.user_id = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getFullName() {
        return full_name;
    }
    public void setFullName(String fullName) {
        this.full_name = fullName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getAvtPath() {
        return avt_path;
    }
    public void setAvtPath(String avtPath) {
        this.avt_path = avtPath;
    }
    public String getBannerPath() {
        return banner_path;
    }
    public void setBannerPath(String bannerPath) {
        this.banner_path = bannerPath;
    }
    public boolean getFavouriteMoviesVisibility() { 
    	return favourite_movies_visibility; 
    } 
    public void setFavouriteMoviesVisibility(boolean favourite_movies_visibility) { 
    	this.favourite_movies_visibility = favourite_movies_visibility; 
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        this.isActive = active;
    }
    public Timestamp getCreatedAt() {
        return created_at;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.created_at = createdAt;
    }
    public Timestamp getDeletedAt() {
        return deleted_at;
    }
    public void setDeletedAt(Timestamp deletedAt) {
        this.deleted_at = deletedAt;
    }
}

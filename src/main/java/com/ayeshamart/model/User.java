package com.ayeshamart.model;

/**
 * Represents a registered user of Ayesha Mart.
 * Roles: buyer, seller, admin.
 */
public class User {

    public static final String ROLE_BUYER = "buyer";
    public static final String ROLE_SELLER = "seller";
    public static final String ROLE_ADMIN = "admin";

    private String userId;
    private String name;
    private String email;
    private String password; // stored hashed, never plaintext
    private String phone;
    private String address;
    private String role;
    private String createdAt;

    public User() {
    }

    public User(String userId, String name, String email, String password,
                String phone, String address, String role, String createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', name='" + name + "', email='" + email
                + "', role='" + role + "', createdAt='" + createdAt + "'}";
    }
}
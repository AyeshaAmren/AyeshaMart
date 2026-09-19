package com.ayeshamart.service;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.User;
import com.ayeshamart.util.PasswordUtil;

/**
 * Registration workflow: validates input, rejects admin registration,
 * hashes the password and persists the user via UserDAO.
 */
public class UserService {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_PATTERN = "^\\+?[0-9][0-9\\s\\-()]{6,14}$";
    private static final int PASSWORD_MIN_LENGTH = 6;

    private final UserDAO userDao = new UserDAO();

    /**
     * Validates and registers a new buyer or seller.
     * Throws UserValidationException with field-level messages when input is invalid.
     *
     * @return the saved user (with generated userId and createdAt)
     */
    public User register(User user, String confirmPassword) throws UserValidationException {
        UserValidationException errors = new UserValidationException();

        String name = trimToNull(user.getName());
        String email = trimToNull(user.getEmail());
        String phone = trimToNull(user.getPhone());
        String address = trimToNull(user.getAddress());
        String password = user.getPassword();
        String role = trimToNull(user.getRole());

        // Full name
        if (name == null) {
            errors.addError("fullName", "Full name is required.");
        } else if (name.length() < 2) {
            errors.addError("fullName", "Name must be at least 2 characters long.");
        }

        // Email
        if (email == null) {
            errors.addError("email", "Email address is required.");
        } else if (!email.matches(EMAIL_PATTERN)) {
            errors.addError("email", "Please enter a valid email address.");
        } else if (userDao.emailExists(email)) {
            errors.addError("email", "An account with this email already exists. Please login instead.");
        }

        // Phone
        if (phone == null) {
            errors.addError("phone", "Phone number is required.");
        } else if (!phone.matches(PHONE_PATTERN)) {
            errors.addError("phone", "Please enter a valid phone number.");
        }

        // Address
        if (address == null) {
            errors.addError("address", "Address is required.");
        }

        // Password + confirmation
        if (password == null || password.isEmpty()) {
            errors.addError("password", "Password is required.");
        } else if (password.length() < PASSWORD_MIN_LENGTH) {
            errors.addError("password", "Password must be at least " + PASSWORD_MIN_LENGTH + " characters long.");
        } else if (!password.equals(confirmPassword)) {
            errors.addError("confirmPassword", "Passwords do not match.");
        }

        // Role: public registration allows buyers and sellers only - never admin.
        if (role == null || !(role.equals(User.ROLE_BUYER) || role.equals(User.ROLE_SELLER))) {
            if (User.ROLE_ADMIN.equals(role)) {
                errors.addError("role", "Admin accounts cannot be created through public registration.");
            } else {
                errors.addError("role", "Please select a valid account role.");
            }
        }

        if (errors.hasErrors()) {
            throw errors;
        }

        // Normalize and persist
        user.setName(name);
        user.setEmail(email.toLowerCase());
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole(role);
        user.setPassword(PasswordUtil.hashPassword(password));

        return userDao.save(user);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
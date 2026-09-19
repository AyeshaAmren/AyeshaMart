package com.ayeshamart.dao;

import com.ayeshamart.model.User;
import com.ayeshamart.util.ExcelUtil;
import com.ayeshamart.util.PasswordUtil;

import java.util.List;
import java.util.Locale;

/**
 * Data-access layer over the Excel store. All mutations are synchronized so
 * concurrent requests do not corrupt the .xlsx file.
 */
public class UserDAO {

    /** Returns true when a user with the given email already exists. */
    public boolean isEmailRegistered(String email) {
        return findByEmail(email) != null;
    }

    /** Finds a user by email (case-insensitive), or null when not found. */
    public synchronized User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        for (User user : ExcelUtil.readUsers()) {
            if (normalized.equals(user.getEmail().toLowerCase(Locale.ROOT))) {
                return user;
            }
        }
        return null;
    }

    /**
     * Registers a new user. Assigns the next user id and timestamp, appends a
     * row to users.xlsx. Returns false when the email is already registered.
     */
    public synchronized boolean register(User user) {
        if (user == null || user.getEmail() == null || user.getPasswordHash() == null) {
            return false;
        }
        if (isEmailRegistered(user.getEmail())) {
            return false;
        }
        user.setId(ExcelUtil.nextUserId());
        user.setEmail(user.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setCreatedAt(ExcelUtil.nowTimestamp());
        ExcelUtil.appendUser(user);
        return true;
    }

    /** Verifies email + password against the stored PBKDF2 hash. */
    public boolean authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            return false;
        }
        return PasswordUtil.verifyPassword(password, user.getPasswordHash());
    }

    /** All users (useful for debugging / later modules). */
    public List<User> findAll() {
        return ExcelUtil.readUsers();
    }
}

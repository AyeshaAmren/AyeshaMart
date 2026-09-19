package com.ayeshamart.service;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.util.PasswordUtil;

/**
 * Authentication workflow for Ayesha Mart.
 *
 * Verifies an email/password pair against the stored users and, on success,
 * returns a {@link SessionUser} that is safe to place in the HttpSession
 * (it never contains the password hash).
 */
public class AuthenticationService {

    private final UserDAO userDao = new UserDAO();

    /**
     * Authenticates a user by email and plaintext password.
     *
     * @return a session-safe user when the credentials are valid
     * @throws AuthenticationException with a user-friendly message when login fails
     */
    public SessionUser authenticate(String email, String password) throws AuthenticationException {
        String normalizedEmail = trimToNull(email);

        if (normalizedEmail == null) {
            throw new AuthenticationException("Please enter your email address.");
        }
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Please enter your password.");
        }

        User user = userDao.findByEmail(normalizedEmail);
        if (user == null) {
            throw new AuthenticationException("No account found with this email address.");
        }

        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new AuthenticationException("Incorrect password. Please try again.");
        }

        String role = trimToNull(user.getRole());
        if (role == null) {
            throw new AuthenticationException("This account has no assigned role. Please contact support.");
        }

        return SessionUser.from(user);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

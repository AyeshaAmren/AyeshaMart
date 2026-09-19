package com.ayeshamart.model;

import java.io.Serializable;

/**
 * Lightweight, serializable representation of the authenticated user stored in the
 * HttpSession. It intentionally excludes the password (or any credential) so that
 * sensitive data is never kept in the session.
 */
public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String name;
    private final String email;
    private final String role;

    public SessionUser(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    /**
     * Builds a session-safe user from a persisted {@link User}.
     */
    public static SessionUser from(User user) {
        return new SessionUser(user.getUserId(), user.getName(), user.getEmail(), user.getRole());
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isBuyer() {
        return User.ROLE_BUYER.equalsIgnoreCase(role);
    }

    public boolean isSeller() {
        return User.ROLE_SELLER.equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return User.ROLE_ADMIN.equalsIgnoreCase(role);
    }

    /**
     * Human friendly role label (e.g. "buyer" -> "Buyer").
     */
    public String getRoleLabel() {
        if (role == null || role.isEmpty()) {
            return "User";
        }
        return Character.toUpperCase(role.charAt(0)) + role.substring(1).toLowerCase();
    }

    @Override
    public String toString() {
        return "SessionUser{userId='" + userId + "', email='" + email + "', role='" + role + "'}";
    }
}

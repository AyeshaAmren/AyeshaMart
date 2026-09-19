package com.ayeshamart.util;

import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Central helpers for session based authentication and authorization.
 *
 * The authenticated user is stored under the {@link #AUTH_USER} session key as a
 * {@link SessionUser} (never the password). Keeping this logic in one place means the
 * servlets, filter and JSPs all agree on how a logged-in user is represented.
 */
public final class SessionUtil {

    /** Session attribute holding the logged-in {@link SessionUser}. */
    public static final String AUTH_USER = "authUser";

    /** Remember-me sessions last 7 days; normal sessions 30 minutes. */
    private static final int NORMAL_SESSION_SECONDS = 30 * 60;
    private static final int REMEMBERED_SESSION_SECONDS = 7 * 24 * 60 * 60;

    private SessionUtil() {
    }

    /**
     * Returns the logged-in user, or {@code null} when the request is anonymous.
     */
    public static SessionUser getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(AUTH_USER);
        return (value instanceof SessionUser) ? (SessionUser) value : null;
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    /**
     * True when the current user is authenticated and holds the given role.
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        SessionUser user = getCurrentUser(request);
        return user != null && role != null && role.equalsIgnoreCase(user.getRole());
    }

    /**
     * Ensures the current user holds the required role. When not, the response is
     * redirected (to login or to the user's own dashboard) and {@code false} is returned
     * so the caller can stop processing.
     */
    public static boolean requireRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws IOException {
        SessionUser user = getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=loginRequired");
            return false;
        }
        if (role != null && !role.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + dashboardPath(user.getRole()) + "?error=forbidden");
            return false;
        }
        return true;
    }

    /**
     * Context-relative landing page for a role. Unknown roles fall back to the buyer dashboard.
     */
    public static String dashboardPath(String role) {
        if (User.ROLE_SELLER.equalsIgnoreCase(role)) {
            return "/seller-dashboard.jsp";
        }
        if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
            return "/admin-dashboard.jsp";
        }
        return "/buyer-dashboard.jsp";
    }

    /**
     * Starts an authenticated session, creating a brand new session id so a prior
     * (possibly attacker-influenced) session cannot be reused (session fixation defence).
     */
    public static void login(HttpServletRequest request, SessionUser user, boolean remember) {
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute(AUTH_USER, user);
        session.setMaxInactiveInterval(remember ? REMEMBERED_SESSION_SECONDS : NORMAL_SESSION_SECONDS);
    }

    /**
     * Ends the authenticated session and removes all access.
     */
    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}

package com.ayeshamart.controller;

import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Server-side access control for every protected dashboard page.
 *
 * - Anonymous visitors are sent to the login page.
 * - Authenticated users who request a dashboard that does not belong to their role
 *   are redirected to their own dashboard with a clear "unauthorized" message.
 *
 * Because this runs on the server for every direct URL request, simply typing a
 * dashboard URL in the browser cannot bypass the checks.
 */
@WebFilter(filterName = "AuthorizationFilter",
        urlPatterns = {"/buyer-dashboard.jsp", "/seller-dashboard.jsp", "/admin-dashboard.jsp",
                "/seller-orders.jsp", "/admin-orders.jsp"})
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Never let a browser cache protected pages (so Back after logout shows nothing).
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String contextPath = request.getContextPath();
        SessionUser user = SessionUtil.getCurrentUser(request);

        if (user == null) {
            response.sendRedirect(contextPath + "/login?error=loginRequired");
            return;
        }

        String requiredRole = requiredRoleFor(request.getServletPath());
        if (requiredRole != null && !requiredRole.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(contextPath + SessionUtil.dashboardPath(user.getRole()) + "?error=forbidden");
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Returns the role required for a protected path, or null if the path is public.
     */
    private String requiredRoleFor(String servletPath) {
        if ("/admin-dashboard.jsp".equals(servletPath) || "/admin-orders.jsp".equals(servletPath)) {
            return User.ROLE_ADMIN;
        }
        if ("/seller-dashboard.jsp".equals(servletPath) || "/seller-orders.jsp".equals(servletPath)) {
            return User.ROLE_SELLER;
        }
        if ("/buyer-dashboard.jsp".equals(servletPath)) {
            return User.ROLE_BUYER;
        }
        return null;
    }
}

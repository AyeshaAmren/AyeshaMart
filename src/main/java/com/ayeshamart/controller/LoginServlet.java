package com.ayeshamart.controller;

import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.AuthenticationException;
import com.ayeshamart.service.AuthenticationService;
import com.ayeshamart.service.CartService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles authentication for Ayesha Mart.
 * GET  /login -> show the login form (or redirect an already logged-in user to their dashboard)
 * POST /login -> authenticate, start the session and redirect by role
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthenticationService authenticationService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SessionUser currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser != null) {
            response.sendRedirect(request.getContextPath() + SessionUtil.dashboardPath(currentUser.getRole()));
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        boolean remember = "on".equalsIgnoreCase(request.getParameter("rememberMe"))
                || "true".equalsIgnoreCase(request.getParameter("rememberMe"));

        try {
            SessionUser user = authenticationService.authenticate(email, password);
            SessionUtil.login(request, user, remember);
            if (User.ROLE_BUYER.equalsIgnoreCase(user.getRole())) {
                request.getSession(true).setAttribute("cartCount",
                        new CartService().getCartCount(user.getUserId()));
            }
            response.sendRedirect(request.getContextPath() + SessionUtil.dashboardPath(user.getRole()));
        } catch (AuthenticationException e) {
            request.setAttribute("loginError", e.getMessage());
            request.setAttribute("loginEmail", email);
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}

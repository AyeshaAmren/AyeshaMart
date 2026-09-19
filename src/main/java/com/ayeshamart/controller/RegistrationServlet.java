package com.ayeshamart.controller;

import com.ayeshamart.model.User;
import com.ayeshamart.service.UserService;
import com.ayeshamart.service.UserValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles public user registration for Ayesha Mart.
 * GET  /register -> registration form
 * POST /register -> validate + persist a new buyer/seller
 */
@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = new User();
        user.setName(request.getParameter("fullName"));
        user.setEmail(request.getParameter("email"));
        user.setPassword(request.getParameter("password"));
        user.setPhone(request.getParameter("phone"));
        user.setAddress(request.getParameter("address"));
        user.setRole(request.getParameter("role"));

        String confirmPassword = request.getParameter("confirmPassword");

        try {
            userService.register(user, confirmPassword);
            request.setAttribute("registrationSuccess", true);
            request.setAttribute("registeredEmail", user.getEmail());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (UserValidationException e) {
            request.setAttribute("validationErrors", e.getErrors());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }
}
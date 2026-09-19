package com.ayeshamart.controller;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.User;
import com.ayeshamart.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email"));
        String phone = trim(req.getParameter("phone"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String address = trim(req.getParameter("address"));

        Map<String, String> errors = validate(name, email, phone, password, confirmPassword, address);

        if (errors.isEmpty() && new UserDAO().isEmailRegistered(email)) {
            errors.put("email", "This email is already registered. Please login instead.");
        }

        if (!errors.isEmpty()) {
            forwardBack(req, resp, errors, name, email, phone, address);
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setPasswordHash(PasswordUtil.hashPassword(password));

        if (new UserDAO().register(user)) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?registered=1");
        } else {
            errors.put("general", "Registration failed. Please try again.");
            forwardBack(req, resp, errors, name, email, phone, address);
        }
    }

    private Map<String, String> validate(String name, String email, String phone,
                                         String password, String confirmPassword, String address) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (name.isEmpty()) {
            errors.put("name", "Name is required.");
        } else if (name.length() < 3) {
            errors.put("name", "Name must be at least 3 characters.");
        }

        if (email.isEmpty()) {
            errors.put("email", "Email is required.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Enter a valid email address.");
        }

        if (phone.isEmpty()) {
            errors.put("phone", "Phone number is required.");
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.put("phone", "Enter a valid 10-digit phone number.");
        }

        if (password == null || password.isEmpty()) {
            errors.put("password", "Password is required.");
        } else if (password.length() < 8) {
            errors.put("password", "Password must be at least 8 characters.");
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            errors.put("confirmPassword", "Please confirm your password.");
        } else if (password != null && !password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match.");
        }

        if (address.isEmpty()) {
            errors.put("address", "Address is required.");
        }

        return errors;
    }

    private void forwardBack(HttpServletRequest req, HttpServletResponse resp, Map<String, String> errors,
                             String name, String email, String phone, String address)
            throws ServletException, IOException {
        req.setAttribute("errors", errors);
        req.setAttribute("name", name);
        req.setAttribute("email", email);
        req.setAttribute("phone", phone);
        req.setAttribute("address", address);
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

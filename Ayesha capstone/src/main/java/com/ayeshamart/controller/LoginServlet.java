package com.ayeshamart.controller;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email") == null ? "" : req.getParameter("email").trim();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");

        if (email.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "Email and password are required.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        UserDAO dao = new UserDAO();
        User user = dao.findByEmail(email);

        if (user != null && dao.authenticate(email, password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("user_id", user.getId());
            session.setAttribute("name", user.getName());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("phone", user.getPhone());
            session.setAttribute("address", user.getAddress());
            resp.sendRedirect(req.getContextPath() + "/home.jsp");
        } else {
            req.setAttribute("error", "Invalid email or password.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}

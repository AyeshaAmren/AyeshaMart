package com.ayeshamart.controller;

import com.ayeshamart.dao.ProductDAO;
import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public homepage.
 * GET /home -> category chips plus a "featured products" row from the live catalog.
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final ProductDAO productDao = new ProductDAO();
    private final UserDAO userDao = new UserDAO();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Product> products = productDao.findAll();
        List<Product> featured = new ArrayList<>();
        for (Product product : products) {
            if (product.isActive() && product.isInStock()) {
                featured.add(product);
            }
        }
        featured.sort(Comparator
                .comparingInt((Product p) -> p.getRatingCount()).reversed()
                .thenComparing(Comparator.comparing(Product::getCreatedAt).reversed()));
        if (featured.size() > 8) {
            featured = featured.subList(0, 8);
        }

        Map<String, String> sellerNames = new HashMap<>();
        for (User user : userDao.findAll()) {
            sellerNames.put(user.getUserId(), user.getName());
        }

        request.setAttribute("featuredProducts", featured);
        request.setAttribute("sellerNames", sellerNames);
        request.setAttribute("homeCategories", categoryService.getNames());

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
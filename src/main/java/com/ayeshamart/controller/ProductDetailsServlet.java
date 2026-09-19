package com.ayeshamart.controller;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Public product details page.
 * GET /product?id=... -> full product information.
 */
@WebServlet("/product")
public class ProductDetailsServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String productId = request.getParameter("id");
        SessionUser currentUser = SessionUtil.getCurrentUser(request);
        String viewerId = currentUser == null ? null : currentUser.getUserId();

        Product product = productService.getStorefrontProduct(productId, viewerId);
        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/products?error=notfound");
            return;
        }

        User seller = userDao.findById(product.getSellerId());
        request.setAttribute("product", product);
        request.setAttribute("sellerName", seller != null ? seller.getName() : "Ayesha Mart Seller");

        request.getRequestDispatcher("/product-details.jsp").forward(request, response);
    }
}

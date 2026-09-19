package com.ayeshamart.controller;

import com.ayeshamart.model.Product;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CategoryService;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Seller's own product management list.
 * GET /seller/products -> products belonging to the signed-in seller plus dashboard stats.
 */
@WebServlet("/seller/products")
public class SellerProductsServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_SELLER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);

        List<Product> products = productService.getBySeller(user.getUserId());

        int activeCount = 0;
        int outOfStockCount = 0;
        int totalUnits = 0;
        double inventoryValue = 0d;
        for (Product product : products) {
            if (product.isActive()) {
                activeCount++;
            }
            if (!product.isInStock()) {
                outOfStockCount++;
            }
            totalUnits += product.getStock();
            inventoryValue += product.getPrice() * product.getStock();
        }

        request.setAttribute("sellerProducts", products);
        request.setAttribute("productCount", products.size());
        request.setAttribute("activeCount", activeCount);
        request.setAttribute("outOfStockCount", outOfStockCount);
        request.setAttribute("totalUnits", totalUnits);
        request.setAttribute("inventoryValue", inventoryValue);
        request.setAttribute("categories", categoryService.getNames());

        request.getRequestDispatcher("/seller-dashboard.jsp").forward(request, response);
    }
}

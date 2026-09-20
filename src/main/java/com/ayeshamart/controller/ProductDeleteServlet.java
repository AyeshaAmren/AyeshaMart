package com.ayeshamart.controller;

import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.ProductAccessException;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.util.ProductImageStore;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Deletes a seller's own product.
 * POST /seller/product/delete -> ownership is verified server-side before deletion.
 */
@WebServlet("/seller/product/delete")
public class ProductDeleteServlet extends HttpServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_SELLER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        String id = request.getParameter("id");

        try {
            productService.delete(id, user.getUserId());
            ProductImageStore.delete(id);
            response.sendRedirect(request.getContextPath() + "/seller/products?msg=deleted");
        } catch (ProductAccessException e) {
            response.sendRedirect(request.getContextPath() + "/seller/products?error=forbidden");
        }
    }
}

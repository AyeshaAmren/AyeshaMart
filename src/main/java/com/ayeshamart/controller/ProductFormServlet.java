package com.ayeshamart.controller;

import com.ayeshamart.model.Product;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CategoryService;
import com.ayeshamart.service.ProductAccessException;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.service.ProductValidationException;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Seller add/edit product form.
 * GET  /seller/product          -> blank form
 * GET  /seller/product?id=...   -> form pre-filled with the seller's own product
 * POST /seller/product          -> create (no id) or update (with id)
 */
@WebServlet("/seller/product")
public class ProductFormServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_SELLER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);
        String id = request.getParameter("id");

        if (id != null && !id.trim().isEmpty()) {
            try {
                Product product = productService.getOwned(id, user.getUserId());
                request.setAttribute("product", product);
                request.setAttribute("priceText", String.valueOf(product.getPrice()));
                request.setAttribute("stockText", String.valueOf(product.getStock()));
                request.setAttribute("formMode", "edit");
            } catch (ProductAccessException e) {
                response.sendRedirect(request.getContextPath() + "/seller/products?error=forbidden");
                return;
            }
        } else {
            request.setAttribute("formMode", "create");
        }

        request.setAttribute("categories", categoryService.getNames());
        request.getRequestDispatcher("/product-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_SELLER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);

        String id = request.getParameter("productId");
        String priceText = request.getParameter("price");
        String stockText = request.getParameter("stock");

        Product input = new Product();
        input.setProductId(id == null || id.trim().isEmpty() ? null : id.trim());
        input.setName(request.getParameter("name"));
        input.setDescription(request.getParameter("description"));
        input.setCategory(request.getParameter("category"));
        input.setImage(request.getParameter("image"));
        input.setStatus(request.getParameter("status"));

        boolean creating = input.getProductId() == null;

        try {
            if (creating) {
                productService.create(input, priceText, stockText, user.getUserId());
            } else {
                productService.update(input, priceText, stockText, user.getUserId());
            }
            response.sendRedirect(request.getContextPath()
                    + "/seller/products?msg=" + (creating ? "created" : "updated"));
        } catch (ProductValidationException e) {
            request.setAttribute("validationErrors", e.getErrors());
            request.setAttribute("product", input);
            request.setAttribute("priceText", priceText);
            request.setAttribute("stockText", stockText);
            request.setAttribute("categories", categoryService.getNames());
            request.setAttribute("formMode", creating ? "create" : "edit");
            request.getRequestDispatcher("/product-form.jsp").forward(request, response);
        } catch (ProductAccessException e) {
            response.sendRedirect(request.getContextPath() + "/seller/products?error=forbidden");
        }
    }
}

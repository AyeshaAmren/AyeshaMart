package com.ayeshamart.controller;

import com.ayeshamart.dao.UserDAO;
import com.ayeshamart.model.Product;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CategoryService;
import com.ayeshamart.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public storefront catalogue.
 * GET /products -> active products with optional keyword, category and sort filters.
 */
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final UserDAO userDao = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("q");
        String category = request.getParameter("category");
        String subCategory = request.getParameter("subCategory");
        String sort = request.getParameter("sort");

        List<Product> products = productService.getBuyerCatalog(keyword, category, subCategory, sort);

        Map<String, String> sellerNames = new HashMap<>();
        for (User user : userDao.findAll()) {
            sellerNames.put(user.getUserId(), user.getName());
        }

        request.setAttribute("products", products);
        request.setAttribute("categories", categoryService.getNames());
        request.setAttribute("subCategories", categoryService.getSubCategories(category));
        request.setAttribute("sellerNames", sellerNames);
        request.setAttribute("filterQ", keyword == null ? "" : keyword);
        request.setAttribute("filterCategory", category == null ? "" : category);
        request.setAttribute("filterSubCategory", subCategory == null ? "" : subCategory);
        request.setAttribute("filterSort", sort == null ? "" : sort);

        request.getRequestDispatcher("/products.jsp").forward(request, response);
    }
}

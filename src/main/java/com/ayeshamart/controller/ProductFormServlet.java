package com.ayeshamart.controller;

import com.ayeshamart.model.Product;
import com.ayeshamart.model.SessionUser;
import com.ayeshamart.model.User;
import com.ayeshamart.service.CategoryService;
import com.ayeshamart.service.ProductAccessException;
import com.ayeshamart.service.ProductService;
import com.ayeshamart.service.ProductValidationException;
import com.ayeshamart.util.ProductImageStore;
import com.ayeshamart.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Seller add/edit product form.
 * GET  /seller/product          -> blank form
 * GET  /seller/product?id=...   -> form pre-filled with the seller's own product
 * POST /seller/product          -> create (no id) or update (with id)
 *
 * The form may optionally carry an uploaded photo (multipart); when present it is
 * saved into the data directory and the product's image now points at the local
 * servlet path (product-image?id=...), same as every bundled/seeded product picture.
 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 8 * 1024 * 1024)
@WebServlet("/seller/product")
public class ProductFormServlet extends HttpServlet {

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

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
        request.setAttribute("allSubCategories", categoryService.getAllSubCategories());
        request.getRequestDispatcher("/product-form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireRole(request, response, User.ROLE_SELLER)) {
            return;
        }
        SessionUser user = SessionUtil.getCurrentUser(request);

        boolean multipart = request.getContentType() != null
                && request.getContentType().toLowerCase().startsWith("multipart/");
        Part photo = multipart ? request.getPart("productImage") : null;
        boolean removePhoto = multipart && "true".equals(request.getParameter("removeImage"));

        String id = request.getParameter("productId");
        String priceText = request.getParameter("price");
        String stockText = request.getParameter("stock");

        Product input = new Product();
        input.setProductId(id == null || id.trim().isEmpty() ? null : id.trim());
        input.setName(request.getParameter("name"));
        input.setDescription(request.getParameter("description"));
        input.setCategory(request.getParameter("category"));
        input.setSubCategory(request.getParameter("subCategory"));
        input.setImage(request.getParameter("image"));
        input.setStatus(request.getParameter("status"));

        String photoError = photoError(photo);
        if (photoError != null) {
            ProductValidationException errors = new ProductValidationException();
            errors.addError("image", photoError);
            renderWithErrors(request, response, input, priceText, stockText, errors.getErrors());
            return;
        }

        boolean creating = input.getProductId() == null;

        try {
            Product saved;
            if (creating) {
                saved = productService.create(input, priceText, stockText, user.getUserId());
            } else {
                productService.update(input, priceText, stockText, user.getUserId());
                saved = productService.getOwned(input.getProductId(), user.getUserId());
            }

            if (removePhoto && !creating) {
                ProductImageStore.delete(saved.getProductId());
                productService.assignImage(saved.getProductId(), "", user.getUserId());
            } else if (photoPresent(photo)) {
                String extension = extensionOf(photo);
                try (InputStream in = photo.getInputStream()) {
                    ProductImageStore.save(saved.getProductId(), extension, in);
                }
                productService.assignImage(saved.getProductId(),
                        "product-image?id=" + saved.getProductId(), user.getUserId());
            }

            response.sendRedirect(request.getContextPath()
                    + "/seller/products?msg=" + (creating ? "created" : "updated"));
        } catch (ProductValidationException e) {
            renderWithErrors(request, response, input, priceText, stockText, e.getErrors());
        } catch (ProductAccessException e) {
            response.sendRedirect(request.getContextPath() + "/seller/products?error=forbidden");
        } catch (IOException e) {
            ProductValidationException errors = new ProductValidationException();
            errors.addError("image", "Could not save the uploaded photo. Please try again.");
            renderWithErrors(request, response, input, priceText, stockText, errors.getErrors());
        }
    }

    private void renderWithErrors(HttpServletRequest request, HttpServletResponse response,
                                  Product input, String priceText, String stockText,
                                  Map<String, String> validationErrors)
            throws ServletException, IOException {
        request.setAttribute("validationErrors", validationErrors);
        request.setAttribute("product", input);
        request.setAttribute("priceText", priceText);
        request.setAttribute("stockText", stockText);
        request.setAttribute("categories", categoryService.getNames());
        request.setAttribute("allSubCategories", categoryService.getAllSubCategories());
        request.setAttribute("formMode", input.getProductId() == null ? "create" : "edit");
        request.getRequestDispatcher("/product-form.jsp").forward(request, response);
    }

    private static boolean photoPresent(Part photo) {
        return photo != null && photo.getSize() > 0;
    }

    /**
     * Returns an error message for an invalid upload, or null when the upload is OK
     * (including the "no file chosen" case, which is fine because the photo is optional).
     */
    private static String photoError(Part photo) {
        if (!photoPresent(photo)) {
            return null;
        }
        String type = photo.getContentType();
        for (String allowed : ALLOWED_TYPES) {
            if (allowed.equalsIgnoreCase(type)) {
                return null;
            }
        }
        return "Upload a JPG, PNG, GIF or WebP photo.";
    }

    private static String extensionOf(Part photo) {
        String type = photo.getContentType();
        if ("image/png".equalsIgnoreCase(type)) {
            return "png";
        }
        if ("image/gif".equalsIgnoreCase(type)) {
            return "gif";
        }
        if ("image/webp".equalsIgnoreCase(type)) {
            return "webp";
        }
        return "jpg";
    }
}
